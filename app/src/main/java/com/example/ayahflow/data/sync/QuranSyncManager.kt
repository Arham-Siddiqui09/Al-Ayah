package com.example.ayahflow.data.sync

import android.util.Log
import com.example.ayahflow.data.local.AyahDao
import com.example.ayahflow.data.model.AyahEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

enum class SyncState {
    NOT_STARTED, DOWNLOADING, VALIDATING, COMPLETED, FAILED
}

class QuranSyncManager(private val ayahDao: AyahDao) {
    private val _syncState = MutableStateFlow(SyncState.NOT_STARTED)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _syncProgress = MutableStateFlow(0)
    val syncProgress: StateFlow<Int> = _syncProgress.asStateFlow() // 0 to 114
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    suspend fun startSyncIfNeeded() {
        if (ayahDao.getAyahCount() == 6236) {
            _syncState.value = SyncState.COMPLETED
            return
        }
        
        _syncState.value = SyncState.DOWNLOADING
        _errorMessage.value = null
        
        val allAyahs = mutableListOf<AyahEntity>()
        var globalIndexCounter = 1
        
        try {
            for (surahNum in 1..114) {
                var success = false
                var retries = 0
                while (!success && retries < 3) {
                    try {
                        val surahAyahs = fetchSurah(surahNum, globalIndexCounter)
                        allAyahs.addAll(surahAyahs)
                        globalIndexCounter += surahAyahs.size
                        success = true
                        _syncProgress.value = surahNum
                    } catch (e: Exception) {
                        retries++
                        if (retries >= 3) {
                            throw Exception("Failed to fetch surah $surahNum after 3 retries: ${e.message}")
                        }
                        delay(1000)
                    }
                }
            }
            
            _syncState.value = SyncState.VALIDATING
            
            // Validate
            if (allAyahs.size != 6236) {
                throw Exception("Validation failed: Expected 6236 ayahs, got ${allAyahs.size}")
            }
            
            val uniqueIndices = allAyahs.map { it.globalIndex }.toSet()
            if (uniqueIndices.size != 6236) {
                throw Exception("Validation failed: Duplicate global indices found")
            }
            
            if (allAyahs.any { it.arabicText.isBlank() || it.translation.isBlank() }) {
                throw Exception("Validation failed: Empty Arabic text or translation found")
            }
            
            val surahNumbers = allAyahs.map { it.surahNumber }.toSet()
            if (surahNumbers.size != 114 || !surahNumbers.containsAll((1..114).toList())) {
                throw Exception("Validation failed: Missing or invalid Surah numbers")
            }
            
            // Insert inside a transaction
            ayahDao.replaceAllAyahs(allAyahs)
            
            _syncState.value = SyncState.COMPLETED
            
        } catch (e: Exception) {
            Log.e("QuranSyncManager", "Sync failed", e)
            _errorMessage.value = e.message ?: "Unknown error"
            _syncState.value = SyncState.FAILED
        }
    }

    private suspend fun fetchSurah(surahNum: Int, startIndex: Int): List<AyahEntity> = withContext(Dispatchers.IO) {
        val url = URL("https://ummahapi.com/api/quran/surah/$surahNum")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            throw Exception("HTTP Error ${connection.responseCode}")
        }
        
        val responseStr = connection.inputStream.bufferedReader().use { it.readText() }
        val root = JSONObject(responseStr)
        if (!root.optBoolean("success", false)) {
            throw Exception("API returned success=false")
        }
        
        val data = root.getJSONObject("data")
        val surah = data.getJSONObject("surah")
        val surahName = surah.getString("name_english")
        
        val verses = data.getJSONArray("verses")
        val ayahs = mutableListOf<AyahEntity>()
        
        for (i in 0 until verses.length()) {
            val v = verses.getJSONObject(i)
            val ayahNum = v.getInt("ayah")
            val arabic = v.getString("arabic")
            val translation = v.getJSONObject("translations").optString("sahih_international", "")
            
            var audioUrl: String? = null
            if (v.has("audio")) {
                val audioObj = v.getJSONObject("audio")
                audioUrl = audioObj.optString("ayah_audio", null)
            }
            
            ayahs.add(
                AyahEntity(
                    globalIndex = startIndex + i,
                    surahNumber = surahNum,
                    surahName = surahName,
                    ayahNumber = ayahNum,
                    juzNumber = 0,
                    arabicText = arabic,
                    translation = translation.ifBlank { "No translation available" },
                    audioUrl = audioUrl,
                    source = "UmmahAPI"
                )
            )
        }
        
        ayahs
    }
}
