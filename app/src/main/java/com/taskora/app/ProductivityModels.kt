package com.taskora.app

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "focus_sessions", indices = [Index(value = ["taskId"]), Index(value = ["startedAt"])])
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long? = null,
    val taskTitle: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    @ColumnInfo(defaultValue = "0") val durationSeconds: Long = 0,
    @ColumnInfo(defaultValue = "'RUNNING'") val status: String = "RUNNING"
)

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC") fun observeAll(): Flow<List<FocusSession>>
    @Query("SELECT * FROM focus_sessions WHERE status = 'RUNNING' ORDER BY startedAt DESC LIMIT 1") suspend fun running(): FocusSession?
    @Insert suspend fun insert(item: FocusSession): Long
    @Query("UPDATE focus_sessions SET endedAt = :endedAt, durationSeconds = :duration, status = :status WHERE id = :id AND status = 'RUNNING'") suspend fun finish(id: Long, endedAt: Long, duration: Long, status: String): Int
    @Query("DELETE FROM focus_sessions") suspend fun clearAll()
    @Insert suspend fun insertAll(items: List<FocusSession>)
}

@Entity(tableName = "achievement_unlocks", indices = [Index(value = ["achievementId"], unique = true)])
data class AchievementUnlock(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val achievementId: String,
    val title: String,
    val description: String,
    val icon: String,
    val unlockedAt: Long,
    @ColumnInfo(defaultValue = "0") val rewardXp: Int = 0
)

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievement_unlocks ORDER BY unlockedAt DESC") fun observeAll(): Flow<List<AchievementUnlock>>
    @Query("SELECT EXISTS(SELECT 1 FROM achievement_unlocks WHERE achievementId = :key)") suspend fun isUnlocked(key: String): Boolean
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun unlock(item: AchievementUnlock): Long
    @Insert suspend fun insertAll(items: List<AchievementUnlock>)
    @Query("DELETE FROM achievement_unlocks") suspend fun clearAll()
}
