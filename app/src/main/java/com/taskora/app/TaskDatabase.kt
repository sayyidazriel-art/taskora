package com.taskora.app

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [TaskEntity::class, XpHistory::class, RecurringTemplate::class, HiddenQuest::class, FocusSession::class, AchievementUnlock::class], version = 5, exportSchema = false)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun xpHistoryDao(): XpHistoryDao
    abstract fun recurringTemplateDao(): RecurringTemplateDao
    abstract fun hiddenQuestDao(): HiddenQuestDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN description TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE tasks ADD COLUMN startAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tasks ADD COLUMN deadlineAt INTEGER")
                db.execSQL("UPDATE tasks SET startAt = createdAt WHERE startAt = 0")
                db.execSQL("UPDATE tasks SET deadlineAt = CAST(strftime('%s', deadline) AS INTEGER) * 1000 WHERE deadline IS NOT NULL AND deadline != ''")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS xp_history (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, taskId INTEGER NOT NULL, taskTitle TEXT NOT NULL, amount INTEGER NOT NULL DEFAULT 10, earnedAt INTEGER NOT NULL)")
                // Preserve progress already earned before this upgrade.
                db.execSQL("INSERT INTO xp_history (taskId, taskTitle, amount, earnedAt) SELECT id, title, 10, COALESCE(completedAt, createdAt) FROM tasks WHERE done = 1")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE tasks SET category = 'Learn' WHERE category IN ('Learning', 'Study')")
                db.execSQL("UPDATE tasks SET category = 'Personal' WHERE category NOT IN ('Personal', 'Work', 'Learn')")
                db.execSQL("ALTER TABLE tasks ADD COLUMN difficulty TEXT NOT NULL DEFAULT 'Medium'")
                db.execSQL("ALTER TABLE tasks ADD COLUMN actualStartedAt INTEGER")
                db.execSQL("ALTER TABLE tasks ADD COLUMN missedPenaltyApplied INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE xp_history ADD COLUMN eventType TEXT NOT NULL DEFAULT 'COMPLETION'")
                db.execSQL("CREATE TABLE IF NOT EXISTS recurring_templates (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, description TEXT NOT NULL, category TEXT NOT NULL, priority TEXT NOT NULL, difficulty TEXT NOT NULL, weekdays TEXT NOT NULL, startHour INTEGER NOT NULL, startMinute INTEGER NOT NULL, deadlineHour INTEGER NOT NULL, deadlineMinute INTEGER NOT NULL, enabled INTEGER NOT NULL, lastGeneratedWeek TEXT)")
                db.execSQL("CREATE TABLE IF NOT EXISTS hidden_quests (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, description TEXT NOT NULL, appearsAt INTEGER NOT NULL, deadlineAt INTEGER NOT NULL, rewardXp INTEGER NOT NULL, status TEXT NOT NULL, completedAt INTEGER)")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS focus_sessions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, taskId INTEGER, taskTitle TEXT NOT NULL, startedAt INTEGER NOT NULL, endedAt INTEGER, durationSeconds INTEGER NOT NULL DEFAULT 0, status TEXT NOT NULL DEFAULT 'RUNNING')")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_focus_sessions_taskId ON focus_sessions(taskId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_focus_sessions_startedAt ON focus_sessions(startedAt)")
                db.execSQL("CREATE TABLE IF NOT EXISTS achievement_unlocks (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, achievementId TEXT NOT NULL, title TEXT NOT NULL, description TEXT NOT NULL, icon TEXT NOT NULL, unlockedAt INTEGER NOT NULL, rewardXp INTEGER NOT NULL DEFAULT 0)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_achievement_unlocks_achievementId ON achievement_unlocks(achievementId)")
            }
        }

        @Volatile private var INSTANCE: TaskDatabase? = null

        fun getInstance(context: Context): TaskDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, TaskDatabase::class.java, "taskora_local.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build().also { INSTANCE = it }
        }
    }
}
