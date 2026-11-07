package com.taskora.app

import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream

data class TaskoraBackupData(
    val tasks: List<TaskEntity>,
    val xpHistory: List<XpHistory> = emptyList(),
    val recurringTemplates: List<RecurringTemplate> = emptyList(),
    val hiddenQuests: List<HiddenQuest> = emptyList(),
    val settings: Map<String, String> = emptyMap(),
    val avatarBytes: ByteArray? = null,
    val focusSessions: List<FocusSession> = emptyList(),
    val achievementUnlocks: List<AchievementUnlock> = emptyList()
)

object TaskBackup {
    fun write(
        tasks: List<TaskEntity>, output: OutputStream,
        xpHistory: List<XpHistory> = emptyList(),
        recurringTemplates: List<RecurringTemplate> = emptyList(),
        hiddenQuests: List<HiddenQuest> = emptyList(),
        settings: Map<String, String> = emptyMap(),
        avatarBytes: ByteArray? = null,
        focusSessions: List<FocusSession> = emptyList(),
        achievementUnlocks: List<AchievementUnlock> = emptyList()
    ) {
        val root = JSONObject().put("format", "taskora-backup").put("version", 4)
        root.put("settings", JSONObject().apply { settings.forEach { (key, value) -> put(key, value) } })
        if (avatarBytes != null) root.put("avatarBase64", android.util.Base64.encodeToString(avatarBytes, android.util.Base64.NO_WRAP))
        root.put("tasks", JSONArray().apply { tasks.forEach { task -> put(JSONObject().apply {
            put("id", task.id); put("title", task.title); put("description", task.description); put("category", task.category)
            put("deadline", task.deadline ?: JSONObject.NULL); put("startAt", task.startAt); put("deadlineAt", task.deadlineAt ?: JSONObject.NULL)
            put("priority", task.priority); put("done", task.done); put("createdAt", task.createdAt); put("completedAt", task.completedAt ?: JSONObject.NULL)
            put("difficulty", task.difficulty); put("actualStartedAt", task.actualStartedAt ?: JSONObject.NULL); put("missedPenaltyApplied", task.missedPenaltyApplied)
        }) } })
        root.put("xpHistory", JSONArray().apply { xpHistory.forEach { entry -> put(JSONObject().apply {
            put("id", entry.id); put("taskId", entry.taskId); put("taskTitle", entry.taskTitle); put("amount", entry.amount); put("earnedAt", entry.earnedAt); put("eventType", entry.eventType)
        }) } })
        root.put("recurringTemplates", JSONArray().apply { recurringTemplates.forEach { item -> put(JSONObject().apply {
            put("id", item.id); put("title", item.title); put("description", item.description); put("category", item.category); put("priority", item.priority)
            put("difficulty", item.difficulty); put("weekdays", item.weekdays); put("startHour", item.startHour); put("startMinute", item.startMinute)
            put("deadlineHour", item.deadlineHour); put("deadlineMinute", item.deadlineMinute); put("enabled", item.enabled); put("lastGeneratedWeek", item.lastGeneratedWeek ?: JSONObject.NULL)
        }) } })
        root.put("hiddenQuests", JSONArray().apply { hiddenQuests.forEach { quest -> put(JSONObject().apply {
            put("id", quest.id); put("title", quest.title); put("description", quest.description); put("appearsAt", quest.appearsAt); put("deadlineAt", quest.deadlineAt)
            put("rewardXp", quest.rewardXp); put("status", quest.status); put("completedAt", quest.completedAt ?: JSONObject.NULL)
        }) } })
        root.put("focusSessions", JSONArray().apply { focusSessions.forEach { session -> put(JSONObject().apply {
            put("id", session.id); put("taskId", session.taskId ?: JSONObject.NULL); put("taskTitle", session.taskTitle)
            put("startedAt", session.startedAt); put("endedAt", session.endedAt ?: JSONObject.NULL); put("durationSeconds", session.durationSeconds); put("status", session.status)
        }) } })
        root.put("achievementUnlocks", JSONArray().apply { achievementUnlocks.forEach { item -> put(JSONObject().apply {
            put("id", item.id); put("achievementId", item.achievementId); put("title", item.title); put("description", item.description)
            put("icon", item.icon); put("unlockedAt", item.unlockedAt); put("rewardXp", item.rewardXp)
        }) } })
        output.bufferedWriter(Charsets.UTF_8).use { it.write(root.toString(2)) }
    }

    fun read(input: InputStream): TaskoraBackupData {
        val text = input.bufferedReader(Charsets.UTF_8).use { it.readText() }
        require(text.length <= 20_000_000) { "Backup file is too large." }
        val root = JSONObject(text)
        require(root.optString("format") == "taskora-backup") { "This is not a Taskora backup." }
        val version = root.optInt("version", -1)
        require(version in 1..4) { "Unsupported backup version." }
        val tasks = buildList {
            val array = root.getJSONArray("tasks")
            require(array.length() <= 10_000) { "Too many tasks in backup." }
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i); val title = item.optString("title").trim()
                if (title.isBlank()) continue
                val startAt = item.optLong("startAt", System.currentTimeMillis()).coerceAtLeast(0L)
                val deadlineAt = if (item.isNull("deadlineAt")) null else item.optLong("deadlineAt").takeIf { it > 0L }
                if (deadlineAt != null && deadlineAt <= startAt) continue
                add(TaskEntity(
                    id = item.optLong("id", 0L).coerceAtLeast(0L), title = title.take(200), description = item.optString("description").take(4000),
                    category = item.optString("category", "Personal").let { if (it in listOf("Personal", "Work", "Learn")) it else "Personal" }, deadline = item.optString("deadline").takeUnless { it == "null" || it.isBlank() },
                    startAt = startAt, deadlineAt = deadlineAt,
                    priority = item.optString("priority", "Medium").let { if (it in listOf("Low", "Medium", "High")) it else "Medium" },
                    done = item.optBoolean("done", false), createdAt = item.optLong("createdAt", System.currentTimeMillis()).coerceAtLeast(0L),
                    completedAt = if (item.isNull("completedAt")) null else item.optLong("completedAt").takeIf { it > 0L },
                    difficulty = item.optString("difficulty", "Medium").let { if (it in listOf("Easy", "Medium", "Hard", "Expert")) it else "Medium" },
                    actualStartedAt = if (item.isNull("actualStartedAt")) null else item.optLong("actualStartedAt").takeIf { it > 0L },
                    missedPenaltyApplied = item.optBoolean("missedPenaltyApplied", false)
                ))
            }
        }
        val xp = buildList {
            val array = root.optJSONArray("xpHistory") ?: return@buildList
            for (i in 0 until array.length()) { val item = array.getJSONObject(i); add(XpHistory(
                id = item.optLong("id", 0), taskId = item.optLong("taskId"), taskTitle = item.optString("taskTitle"),
                amount = item.optInt("amount"), earnedAt = item.optLong("earnedAt", System.currentTimeMillis()), eventType = item.optString("eventType", "COMPLETION")
            )) }
        }
        val recurring = buildList {
            val array = root.optJSONArray("recurringTemplates") ?: return@buildList
            for (i in 0 until array.length()) { val item = array.getJSONObject(i); add(RecurringTemplate(
                id = item.optLong("id", 0), title = item.optString("title"), description = item.optString("description"), category = item.optString("category", "Personal").let { if (it in listOf("Personal", "Work", "Learn")) it else "Personal" },
                priority = item.optString("priority", "Medium"), difficulty = item.optString("difficulty", "Medium"), weekdays = item.optString("weekdays", "1,3,5"),
                startHour = item.optInt("startHour", 9), startMinute = item.optInt("startMinute", 0), deadlineHour = item.optInt("deadlineHour", 17),
                deadlineMinute = item.optInt("deadlineMinute", 0), enabled = item.optBoolean("enabled", true),
                lastGeneratedWeek = item.optString("lastGeneratedWeek").takeUnless { it.isBlank() || it == "null" }
            )) }
        }
        val quests = buildList {
            val array = root.optJSONArray("hiddenQuests") ?: return@buildList
            for (i in 0 until array.length()) { val item = array.getJSONObject(i); add(HiddenQuest(
                id = item.optLong("id", 0), title = item.optString("title"), description = item.optString("description"), appearsAt = item.optLong("appearsAt"),
                deadlineAt = item.optLong("deadlineAt"), rewardXp = item.optInt("rewardXp"), status = item.optString("status", "PENDING"),
                completedAt = if (item.isNull("completedAt")) null else item.optLong("completedAt")
            )) }
        }
        val focusSessions = buildList {
            val array = root.optJSONArray("focusSessions") ?: return@buildList
            for (i in 0 until array.length()) { val item = array.getJSONObject(i); add(FocusSession(
                id = item.optLong("id", 0), taskId = if (item.isNull("taskId")) null else item.optLong("taskId"),
                taskTitle = item.optString("taskTitle", "Focus Session"), startedAt = item.optLong("startedAt"),
                endedAt = if (item.isNull("endedAt")) null else item.optLong("endedAt"), durationSeconds = item.optLong("durationSeconds").coerceAtLeast(0),
                status = item.optString("status", "COMPLETED").let { if (it in listOf("COMPLETED", "PAUSED")) it else "PAUSED" }
            )) }
        }
        val achievementUnlocks = buildList {
            val array = root.optJSONArray("achievementUnlocks") ?: return@buildList
            for (i in 0 until array.length()) { val item = array.getJSONObject(i); add(AchievementUnlock(
                id = item.optLong("id", 0), achievementId = item.optString("achievementId"), title = item.optString("title"),
                description = item.optString("description"), icon = item.optString("icon", "🏆"), unlockedAt = item.optLong("unlockedAt"), rewardXp = item.optInt("rewardXp")
            )) }
        }
        val settings = buildMap {
            val objectValue = root.optJSONObject("settings") ?: return@buildMap
            val keys = objectValue.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                if (key in setOf("display_name", "avatar", "appearance", "date_format", "time_format", "first_day", "language", "accent_hex", "dashboard_challenges", "dashboard_upcoming", "dashboard_focus_tip", "quiet_hours_enabled", "reminder_lead_minutes", "quiet_start_minute", "quiet_end_minute")) {
                    put(key, objectValue.optString(key).take(128))
                }
            }
        }
        val avatarBytes = root.optString("avatarBase64").takeIf { it.isNotBlank() }?.let {
            runCatching { android.util.Base64.decode(it, android.util.Base64.DEFAULT) }.getOrNull()?.takeIf { bytes -> bytes.size <= 5_000_000 }
        }
        return TaskoraBackupData(tasks, xp, recurring, quests, settings, avatarBytes, focusSessions, achievementUnlocks)
    }
}
