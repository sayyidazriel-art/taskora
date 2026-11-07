package com.taskora.app

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    @ColumnInfo(defaultValue = "''") val description: String = "",
    val category: String = "Personal",
    val deadline: String? = null,
    @ColumnInfo(defaultValue = "0") val startAt: Long = System.currentTimeMillis(),
    val deadlineAt: Long? = null,
    val priority: String = "Medium",
    val done: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    @ColumnInfo(defaultValue = "'Medium'") val difficulty: String = "Medium",
    val actualStartedAt: Long? = null,
    @ColumnInfo(defaultValue = "0") val missedPenaltyApplied: Boolean = false
)
