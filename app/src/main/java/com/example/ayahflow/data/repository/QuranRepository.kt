package com.example.ayahflow.data.repository

import com.example.ayahflow.data.local.AyahDao
import com.example.ayahflow.data.model.AyahEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuranRepository(
    private val ayahDao: AyahDao
) {
    suspend fun getAyahByGlobalIndex(index: Int): AyahEntity? = withContext(Dispatchers.IO) {
        ayahDao.getAyahByGlobalIndex(index)
    }

    suspend fun getAyahs(limit: Int, offset: Int): List<AyahEntity> = withContext(Dispatchers.IO) {
        ayahDao.getAyahs(limit, offset)
    }

    suspend fun getAyahCount(): Int = withContext(Dispatchers.IO) {
        ayahDao.getAyahCount()
    }
    
}
