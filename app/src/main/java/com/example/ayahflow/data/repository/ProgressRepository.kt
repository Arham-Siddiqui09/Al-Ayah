package com.example.ayahflow.data.repository

import com.example.ayahflow.data.local.ProgressDao
import com.example.ayahflow.data.model.ProgressEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ProgressRepository(
    private val progressDao: ProgressDao
) {
    fun getProgressFlow(): Flow<ProgressEntity?> {
        return progressDao.getProgress()
    }

    suspend fun getProgressSync(): ProgressEntity? = withContext(Dispatchers.IO) {
        progressDao.getProgressSync()
    }

    suspend fun updateProgress(globalIndex: Int, totalAyahsReadIncrement: Int = 1) = withContext(Dispatchers.IO) {
        val current = progressDao.getProgressSync()
        val newTotal = (current?.totalAyahsRead ?: 0) + totalAyahsReadIncrement
        val progress = ProgressEntity(
            currentGlobalIndex = globalIndex,
            lastOpenedAt = System.currentTimeMillis(),
            totalAyahsRead = newTotal
        )
        progressDao.updateProgress(progress)
    }

    suspend fun resetProgress() = withContext(Dispatchers.IO) {
        val progress = ProgressEntity(
            currentGlobalIndex = 1,
            lastOpenedAt = System.currentTimeMillis(),
            totalAyahsRead = 0
        )
        progressDao.updateProgress(progress)
    }
}
