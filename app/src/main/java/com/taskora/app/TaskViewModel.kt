package com.taskora.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import java.time.*
import kotlin.random.Random

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TaskDatabase.getInstance(application)
    private val repository = TaskRepository(database)
    private val xpDao = database.xpHistoryDao()
    private val hiddenDao = database.hiddenQuestDao()
    private val recurringDao = database.recurringTemplateDao()
    private val focusDao = database.focusSessionDao()
    private val achievementDao = database.achievementDao()

    val focusSessions = focusDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val achievements = achievementDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val xpHistory = xpDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val totalXp = xpDao.observeTotal().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    val tasks = repository.tasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val hiddenQuests = hiddenDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val recurringTemplates = recurringDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            repository.tasks.collect { current ->
                TaskAlarmScheduler.scheduleAll(getApplication(), current)
                repository.applyMissedPenalties(xpDao)
                evaluateAchievements(current)
                expireAndScheduleHiddenQuest()
            }
        }
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(60_000L)
                repository.applyMissedPenalties(xpDao)
                expireAndScheduleHiddenQuest()
            }
        }
        viewModelScope.launch { expireAndScheduleHiddenQuest() }
        viewModelScope.launch {
            focusDao.observeAll().collect { evaluateAchievements(tasks.value, it) }
        }
        viewModelScope.launch {
            xpDao.observeTotal().collect { evaluateAchievements(tasks.value, focusSessions.value, it) }
        }
        viewModelScope.launch {
            hiddenDao.observeAll().collect { evaluateAchievements(tasks.value, focusSessions.value) }
        }
    }

    fun addTask(title: String, description: String, category: String, startAt: Long, deadlineAt: Long, priority: String, difficulty: String = "Medium") {
        viewModelScope.launch { repository.add(title.trim(), description.trim(), category, startAt, deadlineAt, priority, difficulty) }
    }
    fun updateTask(task: TaskEntity) { viewModelScope.launch { repository.update(task) } }
    fun deleteTask(task: TaskEntity) { viewModelScope.launch { repository.delete(task); TaskAlarmScheduler.cancel(getApplication(), task.id) } }
    fun restoreBackup(backup: TaskoraBackupData) { viewModelScope.launch {
        database.withTransaction {
            database.taskDao().clearAll()
            database.taskDao().insertAll(backup.tasks)
            xpDao.clearAll()
            if (backup.xpHistory.isNotEmpty()) xpDao.insertAll(backup.xpHistory)
            else backup.tasks.filter { it.done }.forEach { task ->
                xpDao.insert(XpHistory(taskId = task.id, taskTitle = task.title, amount = difficultyXp(task.difficulty), earnedAt = task.completedAt ?: task.createdAt))
            }
            recurringDao.clearAll()
            if (backup.recurringTemplates.isNotEmpty()) recurringDao.insertAll(backup.recurringTemplates)
            hiddenDao.clearAll()
            if (backup.hiddenQuests.isNotEmpty()) hiddenDao.insertAll(backup.hiddenQuests)
            focusDao.clearAll()
            if (backup.focusSessions.isNotEmpty()) focusDao.insertAll(backup.focusSessions)
            achievementDao.clearAll()
            if (backup.achievementUnlocks.isNotEmpty()) achievementDao.insertAll(backup.achievementUnlocks)
        }
    } }
    fun startTask(id: Long) { viewModelScope.launch { repository.start(id) } }
    fun toggleTask(id: Long) {
        val task = tasks.value.firstOrNull { it.id == id } ?: return
        if (task.done) return
        viewModelScope.launch {
            val completed = repository.complete(id, xpDao)
            if (completed) TaskAlarmScheduler.cancel(getApplication(), id)
        }
    }
    fun completeHiddenQuest(id: Long) {
        viewModelScope.launch {
            val quest = hiddenQuests.value.firstOrNull { it.id == id } ?: return@launch
            val completed = database.withTransaction {
                if (hiddenDao.finish(id, "COMPLETED", System.currentTimeMillis()) == 1) {
                    xpDao.insert(XpHistory(taskId = -quest.id, taskTitle = "Hidden Quest: ${quest.title}", amount = quest.rewardXp, eventType = "HIDDEN_QUEST"))
                    true
                } else false
            }
            if (completed) scheduleNextHiddenQuest()
        }
    }

    private suspend fun expireAndScheduleHiddenQuest() {
        val now = System.currentTimeMillis()
        hiddenDao.getExpired(now).forEach { hiddenDao.finish(it.id, "EXPIRED", null) }
        val pending = hiddenDao.getPending()
        if (pending == null) scheduleNextHiddenQuest()
    }

    private suspend fun scheduleNextHiddenQuest() {
        if (hiddenDao.getPending() != null) return
        val now = ZonedDateTime.now()
        val delayDays = Random.nextLong(1, 4)
        val date = now.toLocalDate().plusDays(delayDays)
        val appearMinute = Random.nextInt(5 * 60, 21 * 60 + 1)
        val deadlineMinute = Random.nextInt(appearMinute + 30, 22 * 60 + 1)
        val appear = date.atTime(appearMinute / 60, appearMinute % 60).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val deadline = date.atTime(deadlineMinute / 60, deadlineMinute % 60).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val missions = listOf(
            "Move your body" to "Lakukan 15 menit aktivitas fisik ringan.",
            "Quick strength" to "Selesaikan 10 push-up atau alternatif gerakan ringan.",
            "Read a little" to "Baca buku atau artikel selama 15 menit.",
            "Learn something" to "Belajar bahasa atau keterampilan baru selama 15 menit.",
            "Take a walk" to "Berjalan santai selama 10–15 menit.",
            "Reset your space" to "Rapikan meja atau area kerja selama 10 menit."
        )
        val mission = missions.random()
        database.withTransaction {
            if (hiddenDao.getPending() == null) {
                hiddenDao.insert(HiddenQuest(title = mission.first, description = mission.second, appearsAt = appear,
                    deadlineAt = deadline, rewardXp = 2 * 20))
            }
        }
    }

    fun startFocusSession(taskId: Long? = null, title: String = "Deep Focus") {
        viewModelScope.launch {
            val active = focusDao.running()
            if (active != null) finishFocusSession(active.id)
            if (taskId != null) repository.start(taskId)
            val taskTitle = taskId?.let { id -> tasks.value.firstOrNull { it.id == id }?.title } ?: title
            focusDao.insert(FocusSession(taskId = taskId, taskTitle = taskTitle, startedAt = System.currentTimeMillis()))
        }
    }

    fun pauseFocusSession() {
        viewModelScope.launch { focusDao.running()?.let { finishFocusSession(it.id, "PAUSED") } }
    }

    fun resumeFocusSession(taskId: Long? = null, title: String = "Deep Focus") = startFocusSession(taskId, title)

    fun stopFocusSession() {
        viewModelScope.launch { focusDao.running()?.let { finishFocusSession(it.id, "COMPLETED") } }
    }

    private suspend fun finishFocusSession(id: Long, status: String = "COMPLETED") {
        val active = focusDao.running()?.takeIf { it.id == id } ?: return
        val now = System.currentTimeMillis()
        focusDao.finish(id, now, ((now - active.startedAt) / 1000L).coerceAtLeast(0), status)
        evaluateAchievements(tasks.value)
    }

    private suspend fun evaluateAchievements(current: List<TaskEntity>, focusItems: List<FocusSession> = focusSessions.value, currentXp: Int = totalXp.value) {
        val completed = current.count { it.done }
        val xp = currentXp
        val completionDays = xpHistory.value.filter { it.eventType == "COMPLETION" || it.eventType == "LATE_COMPLETION" }
            .map { Instant.ofEpochMilli(it.earnedAt).atZone(ZoneId.systemDefault()).toLocalDate() }.distinct().sorted()
        var streak = 0
        var longestStreak = 0
        var previousDay: LocalDate? = null
        completionDays.forEach { day ->
            streak = if (previousDay != null && day == previousDay!!.plusDays(1)) streak + 1 else 1
            longestStreak = maxOf(longestStreak, streak)
            previousDay = day
        }
        val definitions = listOf(
            listOf("first_task", "First Step", "Complete your first task", "🏁") to (completed >= 1),
            listOf("five_tasks", "Getting Started", "Complete 5 tasks", "🎯") to (completed >= 5),
            listOf("twenty_five_tasks", "Task Master", "Complete 25 tasks", "🏆") to (completed >= 25),
            listOf("fifty_tasks", "Half Century", "Complete 50 tasks", "🥇") to (completed >= 50),
            listOf("hundred_tasks", "Century", "Complete 100 tasks", "🏅") to (completed >= 100),
            listOf("expert_finisher", "Expert Finisher", "Complete an Expert task", "🧠") to current.any { it.done && it.difficulty == "Expert" },
            listOf("hard_worker", "Hard Worker", "Complete a Hard task", "💪") to current.any { it.done && it.difficulty == "Hard" },
            listOf("one_hundred_xp", "XP Collector", "Earn 100 XP", "⚡") to (xp >= 100),
            listOf("five_hundred_xp", "XP Veteran", "Earn 500 XP", "💎") to (xp >= 500),
            listOf("thousand_xp", "XP Legend", "Earn 1,000 XP", "👑") to (xp >= 1000),
            listOf("first_focus", "Focus Begins", "Complete a focus session", "⏱️") to focusItems.any { it.status == "COMPLETED" },
            listOf("five_focus", "Deep Worker", "Complete 5 focus sessions", "🧘") to (focusItems.count { it.status == "COMPLETED" } >= 5),
            listOf("seven_day_streak", "Consistent Week", "Complete tasks on 7 consecutive days", "🔥") to (longestStreak >= 7),
            listOf("thirty_day_streak", "Unstoppable", "Complete tasks on 30 consecutive days", "🌟") to (longestStreak >= 30),
            listOf("level_five", "Rising Star", "Reach level 5", "✨") to (levelForXp(xp) >= 5),
            listOf("level_ten", "Taskora Legend", "Reach level 10", "👑") to (levelForXp(xp) >= 10),
            listOf("first_hidden_quest", "Quest Seeker", "Complete a Hidden Quest", "🗝️") to hiddenQuests.value.any { it.status == "COMPLETED" },
            listOf("ten_focus_hours", "Focus Marathon", "Track 10 hours of focus", "⌛") to (focusItems.filter { it.status == "COMPLETED" }.sumOf { it.durationSeconds } >= 36_000L),
            listOf("twenty_five_focus", "Focus Habit", "Complete 25 focus sessions", "🎧") to (focusItems.count { it.status == "COMPLETED" } >= 25),
            listOf("twenty_five_hard", "Challenge Accepted", "Complete 25 Hard or Expert tasks", "🛡️") to (current.count { it.done && it.difficulty in listOf("Hard", "Expert") } >= 25),
            listOf("two_thousand_xp", "XP Champion", "Earn 2,000 XP", "🏵️") to (xp >= 2000),
            listOf("fifty_day_streak", "Daily Discipline", "Complete tasks on 50 consecutive days", "🌞") to (longestStreak >= 50),
            listOf("ten_hidden_quests", "Quest Champion", "Complete 10 Hidden Quests", "🗺️") to (hiddenQuests.value.count { it.status == "COMPLETED" } >= 10)
        )
        definitions.forEach { (definition, unlocked) ->
            val key = definition[0]
            if (unlocked && !achievementDao.isUnlocked(key)) achievementDao.unlock(
                AchievementUnlock(achievementId = key, title = definition[1], description = definition[2], icon = definition[3], unlockedAt = System.currentTimeMillis())
            )
        }
    }

    fun claimChallenge(key: String, title: String, rewardXp: Int) {
        viewModelScope.launch {
            val eventKey = challengeLedgerId(key)
            database.withTransaction {
                if (!xpDao.hasEvent(eventKey, "CHALLENGE_REWARD")) {
                    xpDao.insert(XpHistory(taskId = eventKey, taskTitle = "Challenge: $title", amount = rewardXp.coerceAtLeast(1), eventType = "CHALLENGE_REWARD"))
                }
            }
        }
    }

    fun addRecurringTemplate(item: RecurringTemplate) {
        viewModelScope.launch {
            val duplicate = recurringTemplates.value.any { saved ->
                saved.title.trim().equals(item.title.trim(), true) && saved.description.trim() == item.description.trim() &&
                    saved.category == item.category && saved.priority == item.priority && saved.difficulty == item.difficulty &&
                    saved.weekdays == item.weekdays && saved.startHour == item.startHour && saved.startMinute == item.startMinute &&
                    saved.deadlineHour == item.deadlineHour && saved.deadlineMinute == item.deadlineMinute
            }
            if (!duplicate) recurringDao.insert(item)
        }
    }
    fun updateRecurringTemplate(item: RecurringTemplate) { viewModelScope.launch { recurringDao.update(item.copy(enabled = true)) } }
    fun deleteRecurringTemplate(item: RecurringTemplate) { viewModelScope.launch { recurringDao.delete(item) } }
    fun generateNextWeek(item: RecurringTemplate) {
        viewModelScope.launch {
            val stored = recurringDao.getById(item.id) ?: return@launch
            if (!stored.enabled) recurringDao.update(stored.copy(enabled = true))
            val today = LocalDate.now()
            val nextMonday = today.with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.MONDAY))
            repository.generateRecurringWeek(stored.copy(enabled = true), nextMonday)
        }
    }
}

fun challengeLedgerId(key: String): Long = -(kotlin.math.abs(key.hashCode().toLong()) + 1_000_000L)
