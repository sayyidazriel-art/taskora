package com.taskora.app

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val CHANNEL_ID = "taskora_task_reminders"
private const val ACTION_START = "com.taskora.app.TASK_START"
private const val ACTION_DEADLINE = "com.taskora.app.TASK_DEADLINE"
private const val EXTRA_TASK_ID = "task_id"
private const val EXTRA_KIND = "kind"
private fun deadlineLeadMillis(context: Context): Long =
    context.getSharedPreferences("taskora_preferences", Context.MODE_PRIVATE)
        .getInt("reminder_lead_minutes", 10).coerceIn(1, 180) * 60_000L

object TaskAlarmScheduler {
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val isIndonesian = context.getSharedPreferences("taskora_preferences", Context.MODE_PRIVATE).getString("language", "en") == "id"
            val channel = NotificationChannel(CHANNEL_ID, if (isIndonesian) "Pengingat tugas" else "Task reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                description = if (isIndonesian) "Pengingat waktu mulai dan tenggat tugas Taskora" else "Taskora task start-time and deadline reminders"
                enableVibration(true)
            }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun scheduleAll(context: Context, tasks: List<TaskEntity>) {
        createChannel(context)
        tasks.forEach { schedule(context, it) }
    }

    fun schedule(context: Context, task: TaskEntity) {
        cancel(context, task.id)
        if (task.done) return
        val now = System.currentTimeMillis()
        if (task.startAt >= now - 60_000L) setAlarm(context, task.id, "start", maxOf(task.startAt, now + 1_000L), ACTION_START)
        val deadline = task.effectiveDeadlineMillis()
        val reminderAt = deadline?.minus(deadlineLeadMillis(context))
        if (reminderAt != null && reminderAt > now && deadline > task.startAt) {
            setAlarm(context, task.id, "deadline", reminderAt, ACTION_DEADLINE)
        }
    }

    private fun setAlarm(context: Context, taskId: Long, kind: String, at: Long, action: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = pendingIntent(context, taskId, kind, action)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
            } else {
                // Fallback tetap bekerja tanpa akses exact-alarm, tetapi Android dapat menundanya.
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
        }
    }

    fun deferUntil(context: Context, taskId: Long, kind: String, at: Long) {
        val action = if (kind == "deadline") ACTION_DEADLINE else ACTION_START
        setAlarm(context, taskId, kind, at, action)
    }

    fun cancel(context: Context, taskId: Long) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        listOf("start" to ACTION_START, "deadline" to ACTION_DEADLINE).forEach { (kind, action) ->
            val pending = pendingIntent(context, taskId, kind, action)
            manager.cancel(pending)
            pending.cancel()
        }
    }

    private fun pendingIntent(context: Context, id: Long, kind: String, action: String): PendingIntent {
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            this.action = action
            data = Uri.parse("taskora://reminder/$id/$kind")
            putExtra(EXTRA_TASK_ID, id)
            putExtra(EXTRA_KIND, kind)
        }
        return PendingIntent.getBroadcast(
            context, (id * 2 + if (kind == "deadline") 1 else 0).toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun rescheduleAll(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val tasks = TaskDatabase.getInstance(context).taskDao().getAllOnce()
            scheduleAll(context, tasks)
        }
    }
}

class TaskAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val kind = intent.getStringExtra(EXTRA_KIND) ?: return
        if (id <= 0L) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = TaskDatabase.getInstance(context).taskDao().getById(id)
                if (task != null && !task.done) {
                    val now = System.currentTimeMillis()
                    val shouldNotify = when (kind) {
                        "start" -> task.startAt <= now && task.startAt > 0L && now - task.startAt <= 24L * 60L * 60L * 1000L
                        "deadline" -> {
                            val deadline = task.effectiveDeadlineMillis()
                            deadline != null && deadline > now && deadline - now <= maxOf(deadlineLeadMillis(context) + 60_000L, 12L * 60L * 60L * 1000L)
                        }
                        else -> false
                    }
                    if (shouldNotify) {
                        val prefs = context.getSharedPreferences("taskora_preferences", Context.MODE_PRIVATE)
                        val quietEnabled = prefs.getBoolean("quiet_hours_enabled", false)
                        if (quietEnabled) {
                            val nowLocal = java.time.LocalTime.now()
                            val startMinute = prefs.getInt("quiet_start_minute", 22 * 60).coerceIn(0, 1439)
                            val endMinute = prefs.getInt("quiet_end_minute", 7 * 60).coerceIn(0, 1439)
                            val nowMinute = nowLocal.hour * 60 + nowLocal.minute
                            val isQuiet = if (startMinute == endMinute) true else if (startMinute < endMinute)
                                nowMinute in startMinute until endMinute else nowMinute >= startMinute || nowMinute < endMinute
                            if (isQuiet) {
                                val today = java.time.LocalDate.now(java.time.ZoneId.systemDefault())
                                var endDate = today
                                if (startMinute > endMinute && nowMinute >= startMinute) endDate = today.plusDays(1)
                                val endAt = endDate.atTime(endMinute / 60, endMinute % 60)
                                    .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                                TaskAlarmScheduler.deferUntil(context, id, kind, maxOf(endAt, now + 60_000L))
                                return@launch
                            }
                        }
                        showNotification(context, task, kind)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, task: TaskEntity, kind: String) {
        TaskAlarmScheduler.createChannel(context)
        val title: String
        val body: String
        val isIndonesian = context.getSharedPreferences("taskora_preferences", Context.MODE_PRIVATE)
            .getString("language", "en") == "id"
        val categoryLabel = if (isIndonesian) when (task.category) {
            "Personal" -> "Pribadi"; "Work" -> "Pekerjaan"; "Learn" -> "Belajar"; else -> task.category
        } else task.category
        if (kind == "start") {
            title = if (isIndonesian) "Waktunya mulai: ${task.title}" else "Time to start: ${task.title}"
            body = if (isIndonesian) "Tugas $categoryLabel sudah memasuki waktu mulai. Yuk mulai sekarang!" else "Your $categoryLabel task is scheduled to start now. Let's get started!"
        } else {
            title = if (isIndonesian) "Tenggat 10 menit lagi" else "Deadline in 10 minutes"
            body = if (isIndonesian) "${task.title} belum selesai. Selesaikan sebelum tenggat." else "${task.title} is not finished yet. Complete it before the deadline."
        }
        val openApp = PendingIntent.getActivity(
            context, task.id.toInt(), Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .build()
        if (Build.VERSION.SDK_INT < 33 || context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            runCatching { NotificationManagerCompat.from(context).notify((task.id * 2 + if (kind == "deadline") 1 else 0).toInt(), notification) }
        }
    }
}

class TaskBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_TIME_CHANGED || intent.action == Intent.ACTION_TIMEZONE_CHANGED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = TaskDatabase.getInstance(context)
                    val tasks = database.taskDao().getAllOnce()
                    TaskAlarmScheduler.scheduleAll(context, tasks)
                    TaskDeadlineSweepScheduler.schedule(context)
                    TaskRepository(database).applyMissedPenalties(database.xpHistoryDao())
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}

object TaskDeadlineSweepScheduler {
    private const val REQUEST_CODE = 7302
    fun schedule(context: Context) {
        val manager = context.getSystemService(AlarmManager::class.java)
        val next = java.time.LocalDate.now(java.time.ZoneId.systemDefault()).plusDays(1)
            .atStartOfDay(java.time.ZoneId.systemDefault()).plusSeconds(5).toInstant().toEpochMilli()
        val intent = Intent(context, TaskDeadlineSweepReceiver::class.java).setAction("com.taskora.app.DEADLINE_SWEEP")
        val pending = PendingIntent.getBroadcast(context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && manager.canScheduleExactAlarms()) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pending)
        } else {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pending)
        }
    }
}

class TaskDeadlineSweepReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = TaskDatabase.getInstance(context)
                TaskRepository(database).applyMissedPenalties(database.xpHistoryDao())
            } finally {
                TaskDeadlineSweepScheduler.schedule(context)
                pending.finish()
            }
        }
    }
}
