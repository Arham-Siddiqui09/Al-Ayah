package com.example.ayahflow.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ayahflow.data.model.ProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Query("SELECT * FROM reading_progress WHERE id = 1")
    fun getProgress(): Flow<ProgressEntity?>

    @Query("SELECT * FROM reading_progress WHERE id = 1")
    suspend fun getProgressSync(): ProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateProgress(progress: ProgressEntity)
}
