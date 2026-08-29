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

/**
 * Downloads all 6,236 Ayahs from the Al-Quran Cloud API (alquran.cloud).
 * Free, no API key required. Uses Uthmani Arabic script + Sahih International English.
 *
 * API: GET https://api.alquran.cloud/v1/surah/{n}/editions/quran-uthmani,en.sahih
 */
class QuranSyncManager(private val ayahDao: AyahDao) {

    private val _syncState    = MutableStateFlow(SyncState.NOT_STARTED)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _syncProgress = MutableStateFlow(0)
    val syncProgress: StateFlow<Int> = _syncProgress.asStateFlow() // 0–114

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    suspend fun startSyncIfNeeded() {
        if (ayahDao.getAyahCount() == 6236) {
            _syncState.value = SyncState.COMPLETED
            return
        }

        _syncState.value  = SyncState.DOWNLOADING
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
                        Log.w("QuranSyncManager", "Retry $retries for surah $surahNum: ${e.message}")
                        if (retries >= 3) {
                            throw Exception("Failed to fetch Surah $surahNum after 3 retries: ${e.message}")
                        }
                        delay(1500L * retries) // back-off
                    }
                }
            }

            // ── Validate ──────────────────────────────────────────────────────
            _syncState.value = SyncState.VALIDATING

            if (allAyahs.size != 6236) {
                throw Exception("Validation failed: Expected 6236 ayahs, got ${allAyahs.size}")
            }
            if (allAyahs.map { it.globalIndex }.toSet().size != 6236) {
                throw Exception("Validation failed: Duplicate global indices found")
            }
            if (allAyahs.any { it.arabicText.isBlank() || it.translation.isBlank() }) {
                throw Exception("Validation failed: Empty Arabic text or translation found")
            }
            val surahNumbers = allAyahs.map { it.surahNumber }.toSet()
            if (surahNumbers.size != 114 || !surahNumbers.containsAll((1..114).toList())) {
                throw Exception("Validation failed: Missing or invalid Surah numbers")
            }

            // ── Persist ───────────────────────────────────────────────────────
            ayahDao.replaceAllAyahs(allAyahs)
            _syncState.value = SyncState.COMPLETED

        } catch (e: Exception) {
            Log.e("QuranSyncManager", "Sync failed", e)
            _errorMessage.value = e.message ?: "Unknown error"
            _syncState.value    = SyncState.FAILED
        }
    }

    // ── Al-Quran Cloud API ────────────────────────────────────────────────────
    // Response structure (editions array):
    //   data[0] = quran-uthmani  → Arabic ayahs
    //   data[1] = en.sahih       → English ayahs (Sahih International)
    //   Each edition has: englishName, number (surah #), ayahs[ { numberInSurah, text } ]
    private suspend fun fetchSurah(surahNum: Int, startIndex: Int): List<AyahEntity> =
        withContext(Dispatchers.IO) {
            val endpoint =
                "https://api.alquran.cloud/v1/surah/$surahNum/editions/quran-uthmani,en.sahih"
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod   = "GET"
                connectTimeout  = 15_000
                readTimeout     = 15_000
                setRequestProperty("Accept-Encoding", "gzip")
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("HTTP $responseCode for Surah $surahNum")
            }

            // Handle gzip transparently
            val inputStream = if (connection.contentEncoding == "gzip")
                java.util.zip.GZIPInputStream(connection.inputStream)
            else connection.inputStream

            val responseStr = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val root        = JSONObject(responseStr)

            // API returns { "code": 200, "status": "OK", "data": [...] }
            val apiCode = root.optInt("code", -1)
            if (apiCode != 200) {
                throw Exception("API error for Surah $surahNum (code=$apiCode): ${root.optString("status")}")
            }

            val dataArray = root.getJSONArray("data")
            val arabicEdition  = dataArray.getJSONObject(0) // quran-uthmani
            val englishEdition = dataArray.getJSONObject(1) // en.sahih

            val surahName     = arabicEdition.optString("englishName", "Surah $surahNum")
            val arabicAyahs   = arabicEdition.getJSONArray("ayahs")
            val englishAyahs  = englishEdition.getJSONArray("ayahs")
            val juzNumber     = arabicAyahs.getJSONObject(0).optInt("juz", 0)

            val ayahs = mutableListOf<AyahEntity>()

            for (i in 0 until arabicAyahs.length()) {
                val arabicObj  = arabicAyahs.getJSONObject(i)
                val englishObj = englishAyahs.getJSONObject(i)

                val ayahNum    = arabicObj.getInt("numberInSurah")
                val arabicText = arabicObj.getString("text").trim()
                val translation = englishObj.optString("text", "").trim()
                    .ifBlank { "Translation not available" }

                ayahs.add(
                    AyahEntity(
                        globalIndex = startIndex + i,
                        surahNumber = surahNum,
                        surahName   = surahName,
                        ayahNumber  = ayahNum,
                        juzNumber   = juzNumber,
                        arabicText  = arabicText,
                        translation = translation,
                        audioUrl    = null,
                        source      = "alquran.cloud"
                    )
                )
            }

            ayahs
        }
}
