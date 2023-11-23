package com.example.dailytaskscheduler.util

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(task : Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * from task_table order by id ASC")
    fun getAllTasks(): LiveData<List<Task>>

    @Update
    suspend fun update(task: Task)
}