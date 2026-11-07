package com.taskora.app

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.Instant
import java.time.ZoneId

class TaskRepository(private val database: TaskDatabase) {
    private val dao = database.taskDao()
    val tasks: Flow<List<TaskEntity>> = dao.observeAll()

    suspend fun add(title: String, description: String, category: String, startAt: Long, deadlineAt: Long, priority: String, difficulty: String = "Medium") {
        val deadlineDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(deadlineAt))
        val safeCategory = if (category in listOf("Personal", "Work", "Learn")) category else "Personal"
        dao.insert(TaskEntity(title = title, description = description, category = safeCategory,
            startAt = startAt, deadlineAt = deadlineAt, deadline = deadlineDate, priority = priority, difficulty = difficulty))
    }

    suspend fun update(task: TaskEntity) {
        val current = dao.getById(task.id) ?: return
        if (current.done || isPastDeadlineDate(current)) return
        val safeCategory = if (task.category in listOf("Personal", "Work", "Learn")) task.category else "Personal"
        dao.update(task.copy(category = safeCategory, done = current.done, completedAt = current.completedAt,
            missedPenaltyApplied = current.missedPenaltyApplied, actualStartedAt = current.actualStartedAt))
    }

    suspend fun delete(task: TaskEntity) {
        val current = dao.getById(task.id) ?: return
        if (!current.done && !isPastDeadlineDate(current)) dao.delete(current)
    }

    suspend fun replaceAll(tasks: List<TaskEntity>) = database.withTransaction {
        dao.clearAll()
        dao.insertAll(tasks)
    }

    suspend fun start(taskId: Long, now: Long = System.currentTimeMillis()) {
        val task = dao.getById(taskId) ?: return
        if (!task.done && !isPastDeadlineDate(task)) dao.startOnce(taskId, now)
    }

    suspend fun generateRecurringWeek(template: RecurringTemplate, monday: java.time.LocalDate) = database.withTransaction {
        val stored = database.recurringTemplateDao().let { dao ->
            // Re-read through a dedicated lookup to protect against a double tap.
            dao.getById(template.id)
        } ?: return@withTransaction
        if (!stored.enabled || stored.lastGeneratedWeek == monday.toString()) return@withTransaction
        val existingTasks = dao.getAllOnce()
        stored.weekdays.split(",").mapNotNull { it.toIntOrNull() }.forEach { isoDay ->
            val date = monday.plusDays((isoDay - 1).toLong())
            val start = date.atTime(stored.startHour, stored.startMinute).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            val deadline = date.atTime(stored.deadlineHour, stored.deadlineMinute).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            val duplicate = existingTasks.any { task ->
                task.title.equals(stored.title, ignoreCase = true) && task.startAt == start && task.effectiveDeadlineMillis() == deadline &&
                    task.category == stored.category && task.priority == stored.priority && task.difficulty == stored.difficulty
            }
            if (deadline > start && !duplicate) add(stored.title, stored.description, stored.category, start, deadline, stored.priority, stored.difficulty)
        }
        database.recurringTemplateDao().update(stored.copy(lastGeneratedWeek = monday.toString()))
    }

    suspend fun complete(taskId: Long, xpDao: XpHistoryDao, now: Long = System.currentTimeMillis()): Boolean = database.withTransaction {
        val task = dao.getById(taskId) ?: return@withTransaction false
        if (task.done || isPastDeadlineDate(task)) return@withTransaction false
        if (task.actualStartedAt == null) dao.startOnce(taskId, now)
        val changed = dao.completeOnce(taskId, now)
        if (changed == 0) return@withTransaction false
        if (!xpDao.hasEvent(taskId, "COMPLETION")) {
            val deadline = task.effectiveDeadlineMillis()
            val base = difficultyXp(task.difficulty)
            val late = deadline != null && now > deadline
            val amount = if (late) (base * 0.5).toInt().coerceAtLeast(1) else base
            xpDao.insert(XpHistory(taskId = task.id, taskTitle = task.title, amount = amount,
                earnedAt = now, eventType = if (late) "LATE_COMPLETION" else "COMPLETION"))
        }
        true
    }

    suspend fun applyMissedPenalties(xpDao: XpHistoryDao, now: Long = System.currentTimeMillis()) = database.withTransaction {
        val today = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate()
        dao.getAllOnce().filter { task ->
            !task.done && !task.missedPenaltyApplied && task.effectiveDeadlineMillis()?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().isBefore(today)
            } == true
        }.forEach { task ->
            if (dao.markMissedPenalty(task.id) == 1 && !xpDao.hasEvent(task.id, "MISSED_PENALTY")) {
                val penalty = (difficultyXp(task.difficulty) * 0.5).toInt().coerceAtLeast(1)
                xpDao.insert(XpHistory(taskId = task.id, taskTitle = task.title, amount = -penalty,
                    earnedAt = now, eventType = "MISSED_PENALTY"))
            }
        }
    }

    private fun isPastDeadlineDate(task: TaskEntity): Boolean {
        val deadline = task.effectiveDeadlineMillis() ?: return false
        val dueDate = Instant.ofEpochMilli(deadline).atZone(ZoneId.systemDefault()).toLocalDate()
        return dueDate.isBefore(java.time.LocalDate.now(ZoneId.systemDefault()))
    }
}

fun difficultyXp(difficulty: String): Int = when (difficulty.lowercase(Locale.ROOT)) {
    "easy" -> 10
    "hard" -> 35
    "expert" -> 55
    else -> 20
}

/** Cumulative XP thresholds. Level 1 starts at 0 XP; progression accelerates non-linearly. */
private val LEVEL_THRESHOLDS = listOf(0, 100, 250, 450, 700, 1000, 1400, 1900, 2500, 3200)

fun xpBeforeLevel(level: Int): Int {
    val safeLevel = level.coerceAtLeast(1)
    if (safeLevel <= LEVEL_THRESHOLDS.size) return LEVEL_THRESHOLDS[safeLevel - 1]
    var total = LEVEL_THRESHOLDS.last()
    var increment = 700 // level 10 -> 11, then increase by 100 each level
    repeat(safeLevel - LEVEL_THRESHOLDS.size) {
        total += increment
        increment += 100
    }
    return total
}

fun levelForXp(totalXp: Int): Int {
    val xp = totalXp.coerceAtLeast(0)
    var level = 1
    while (level < 100 && xp >= xpBeforeLevel(level + 1)) level++
    return level
}
