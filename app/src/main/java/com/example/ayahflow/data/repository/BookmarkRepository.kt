package com.example.ayahflow.data.repository

import com.example.ayahflow.data.local.BookmarkDao
import com.example.ayahflow.data.model.BookmarkEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BookmarkRepository(
    private val bookmarkDao: BookmarkDao
) {
    fun getAllBookmarks(): Flow<List<BookmarkEntity>> {
        return bookmarkDao.getAllBookmarks()
    }

    fun getBookmarkedAyahs(): Flow<List<com.example.ayahflow.data.model.BookmarkedAyah>> {
        return bookmarkDao.getBookmarkedAyahs()
    }

    fun isBookmarked(globalIndex: Int): Flow<Boolean> {
        return bookmarkDao.isBookmarked(globalIndex)
    }

    suspend fun toggleBookmark(globalIndex: Int) = withContext(Dispatchers.IO) {
        val exists = bookmarkDao.isBookmarkedSync(globalIndex)
        if (exists) {
            bookmarkDao.deleteBookmark(BookmarkEntity(globalIndex, 0))
        } else {
            bookmarkDao.insertBookmark(BookmarkEntity(globalIndex, System.currentTimeMillis()))
        }
    }
    
    suspend fun addBookmark(globalIndex: Int) = withContext(Dispatchers.IO) {
        bookmarkDao.insertBookmark(BookmarkEntity(globalIndex, System.currentTimeMillis()))
    }
    
    suspend fun removeBookmark(globalIndex: Int) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmark(BookmarkEntity(globalIndex, 0)) // only ID matters for delete
    }
}
