package com.example.ayahflow.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ayahflow.data.model.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT b.globalIndex, a.surahName, a.surahNumber, a.ayahNumber, a.arabicText, b.createdAt FROM bookmarks b INNER JOIN ayahs a ON b.globalIndex = a.globalIndex ORDER BY b.createdAt DESC")
    fun getBookmarkedAyahs(): Flow<List<com.example.ayahflow.data.model.BookmarkedAyah>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE globalIndex = :globalIndex)")
    fun isBookmarked(globalIndex: Int): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE globalIndex = :globalIndex)")
    suspend fun isBookmarkedSync(globalIndex: Int): Boolean
}
