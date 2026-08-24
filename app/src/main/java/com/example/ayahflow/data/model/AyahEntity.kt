package com.example.ayahflow.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ayahs")
data class AyahEntity(
    @PrimaryKey
    val globalIndex: Int,
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val juzNumber: Int,
    val arabicText: String,
    val translation: String,
    val audioUrl: String? = null,
    val source: String? = null
)

@Entity(tableName = "reading_progress")
data class ProgressEntity(
    @PrimaryKey
    val id: Int = 1, // Only one row for current progress
    val currentGlobalIndex: Int,
    val lastOpenedAt: Long,
    val totalAyahsRead: Int
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey
    val globalIndex: Int,
    val createdAt: Long
)
