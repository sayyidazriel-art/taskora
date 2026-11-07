package com.taskora.app

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "recurring_templates")
data class RecurringTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Personal",
    val priority: String = "Medium",
    val difficulty: String = "Medium",
    val weekdays: String,
    val startHour: Int = 9,
    val startMinute: Int = 0,
    val deadlineHour: Int = 17,
    val deadlineMinute: Int = 0,
    val enabled: Boolean = true,
    val lastGeneratedWeek: String? = null
)

@Dao
interface RecurringTemplateDao {
    @Query("SELECT * FROM recurring_templates ORDER BY title") fun observeAll(): Flow<List<RecurringTemplate>>
    @Query("SELECT * FROM recurring_templates WHERE id = :id LIMIT 1") suspend fun getById(id: Long): RecurringTemplate?
    @Insert suspend fun insert(item: RecurringTemplate): Long
    @Insert suspend fun insertAll(items: List<RecurringTemplate>)
    @Query("DELETE FROM recurring_templates") suspend fun clearAll()
    @Update suspend fun update(item: RecurringTemplate)
    @Delete suspend fun delete(item: RecurringTemplate)
}

@Entity(tableName = "hidden_quests")
data class HiddenQuest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val appearsAt: Long,
    val deadlineAt: Long,
    val rewardXp: Int,
    val status: String = "PENDING", // PENDING, COMPLETED, EXPIRED
    val completedAt: Long? = null
)

@Dao
interface HiddenQuestDao {
    @Query("SELECT * FROM hidden_quests ORDER BY appearsAt DESC") fun observeAll(): Flow<List<HiddenQuest>>
    @Query("SELECT * FROM hidden_quests WHERE status = 'PENDING' AND appearsAt <= :now AND deadlineAt >= :now LIMIT 1") suspend fun getActive(now: Long): HiddenQuest?
    @Query("SELECT * FROM hidden_quests WHERE status = 'PENDING' ORDER BY appearsAt DESC LIMIT 1") suspend fun getPending(): HiddenQuest?
    @Query("SELECT * FROM hidden_quests WHERE status = 'PENDING' AND deadlineAt < :now") suspend fun getExpired(now: Long): List<HiddenQuest>
    @Insert suspend fun insert(quest: HiddenQuest): Long
    @Insert suspend fun insertAll(items: List<HiddenQuest>)
    @Query("DELETE FROM hidden_quests") suspend fun clearAll()
    @Query("UPDATE hidden_quests SET status = :status, completedAt = :completedAt WHERE id = :id AND status = 'PENDING'") suspend fun finish(id: Long, status: String, completedAt: Long?): Int
}
