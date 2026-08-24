package com.example.ayahflow.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ayahflow.data.model.AyahEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AyahDao {
    @Query("SELECT * FROM ayahs WHERE globalIndex = :globalIndex")
    suspend fun getAyahByGlobalIndex(globalIndex: Int): AyahEntity?

    @Query("SELECT * FROM ayahs ORDER BY globalIndex ASC LIMIT :limit OFFSET :offset")
    suspend fun getAyahs(limit: Int, offset: Int): List<AyahEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<AyahEntity>)

    @Query("SELECT COUNT(*) FROM ayahs")
    suspend fun getAyahCount(): Int

    @Query("DELETE FROM ayahs")
    suspend fun clearAll()

    @androidx.room.Transaction
    suspend fun replaceAllAyahs(ayahs: List<AyahEntity>) {
        clearAll()
        insertAyahs(ayahs)
    }
}
