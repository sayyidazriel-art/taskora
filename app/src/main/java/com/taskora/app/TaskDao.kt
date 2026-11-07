package com.taskora.app

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY done ASC, CASE WHEN deadline IS NULL THEN 1 ELSE 0 END, deadline ASC, createdAt DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks")
    suspend fun getAllOnce(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Query("UPDATE tasks SET done = 1, completedAt = :completedAt WHERE id = :id AND done = 0")
    suspend fun completeOnce(id: Long, completedAt: Long): Int

    @Query("UPDATE tasks SET actualStartedAt = :startedAt WHERE id = :id AND done = 0 AND actualStartedAt IS NULL")
    suspend fun startOnce(id: Long, startedAt: Long): Int

    @Query("UPDATE tasks SET missedPenaltyApplied = 1 WHERE id = :id AND done = 0 AND missedPenaltyApplied = 0")
    suspend fun markMissedPenalty(id: Long): Int
}
