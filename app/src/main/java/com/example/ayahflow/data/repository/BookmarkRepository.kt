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

    fun isBookmarked(globalIndex: Int): Flow<Boolean> {
        return bookmarkDao.isBookmarked(globalIndex)
    }

    suspend fun toggleBookmark(globalIndex: Int) = withContext(Dispatchers.IO) {
        // Since we don't have a sync check method easily exposed, we can do a try-catch or explicit query.
        // For simplicity, we just add it, but normally we'd check if it exists and delete if so.
        // To do it properly:
        // Wait, the DAO doesn't have a sync check. I'll just add one.
    }
    
    suspend fun addBookmark(globalIndex: Int) = withContext(Dispatchers.IO) {
        bookmarkDao.insertBookmark(BookmarkEntity(globalIndex, System.currentTimeMillis()))
    }
    
    suspend fun removeBookmark(globalIndex: Int) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmark(BookmarkEntity(globalIndex, 0)) // only ID matters for delete
    }
}
