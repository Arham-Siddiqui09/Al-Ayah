package com.example.ayahflow.data.repository

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.example.ayahflow.data.local.BookmarkDao
import com.example.ayahflow.data.model.BookmarkEntity
import com.example.ayahflow.ui.widget.AyahWidget
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookmarkRepository(
    private val context: Context,
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

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun toggleBookmark(globalIndex: Int) = withContext(Dispatchers.IO) {
        val exists = bookmarkDao.isBookmarkedSync(globalIndex)
        if (exists) {
            bookmarkDao.deleteBookmark(BookmarkEntity(globalIndex, 0))
        } else {
            bookmarkDao.insertBookmark(BookmarkEntity(globalIndex, System.currentTimeMillis()))
        }
        GlobalScope.launch {
            try {
                AyahWidget().updateAll(context)
            } catch (e: Exception) {}
        }
    }
    
    suspend fun addBookmark(globalIndex: Int) = withContext(Dispatchers.IO) {
        bookmarkDao.insertBookmark(BookmarkEntity(globalIndex, System.currentTimeMillis()))
    }
    
    suspend fun removeBookmark(globalIndex: Int) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmark(BookmarkEntity(globalIndex, 0)) // only ID matters for delete
    }
}
