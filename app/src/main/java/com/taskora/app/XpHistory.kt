package com.taskora.app

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "xp_history")
data class XpHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val taskTitle: String,
    @ColumnInfo(defaultValue = "10") val amount: Int = 10,
    val earnedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "'COMPLETION'") val eventType: String = "COMPLETION"
)

@Dao
interface XpHistoryDao {
    @Query("SELECT * FROM xp_history ORDER BY earnedAt DESC") fun observeAll(): Flow<List<XpHistory>>
    @Query("SELECT COALESCE(SUM(amount), 0) FROM xp_history") fun observeTotal(): Flow<Int>
    @Query("SELECT * FROM xp_history ORDER BY earnedAt DESC LIMIT 100") fun observeRecent(): Flow<List<XpHistory>>
    @Insert suspend fun insert(entry: XpHistory)
    @Insert suspend fun insertAll(entries: List<XpHistory>)
    @Query("DELETE FROM xp_history WHERE taskId = :taskId") suspend fun deleteForTask(taskId: Long)
    @Query("SELECT EXISTS(SELECT 1 FROM xp_history WHERE taskId = :taskId AND eventType = :eventType)") suspend fun hasEvent(taskId: Long, eventType: String): Boolean
    @Query("DELETE FROM xp_history") suspend fun clearAll()
}
