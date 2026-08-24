package com.example.ayahflow.di

import android.content.Context
import com.example.ayahflow.data.local.QuranDatabase
import com.example.ayahflow.data.repository.BookmarkRepository
import com.example.ayahflow.data.repository.ProgressRepository
import com.example.ayahflow.data.repository.QuranRepository
import com.example.ayahflow.domain.usecase.GetAyahUseCase
import com.example.ayahflow.domain.usecase.GetProgressUseCase
import com.example.ayahflow.domain.usecase.UpdateProgressUseCase

interface AppContainer {
    val quranRepository: QuranRepository
    val progressRepository: ProgressRepository
    val bookmarkRepository: BookmarkRepository
    
    val getAyahUseCase: GetAyahUseCase
    val getProgressUseCase: GetProgressUseCase
    val updateProgressUseCase: UpdateProgressUseCase
    val syncManager: com.example.ayahflow.data.sync.QuranSyncManager
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    
    private val database: QuranDatabase by lazy {
        QuranDatabase.getDatabase(context)
    }

    override val syncManager: com.example.ayahflow.data.sync.QuranSyncManager by lazy {
        com.example.ayahflow.data.sync.QuranSyncManager(database.ayahDao())
    }

    override val quranRepository: QuranRepository by lazy {
        QuranRepository(database.ayahDao())
    }

    override val progressRepository: ProgressRepository by lazy {
        ProgressRepository(database.progressDao())
    }

    override val bookmarkRepository: BookmarkRepository by lazy {
        BookmarkRepository(database.bookmarkDao())
    }

    override val getAyahUseCase: GetAyahUseCase by lazy {
        GetAyahUseCase(quranRepository)
    }

    override val getProgressUseCase: GetProgressUseCase by lazy {
        GetProgressUseCase(progressRepository)
    }

    override val updateProgressUseCase: UpdateProgressUseCase by lazy {
        UpdateProgressUseCase(progressRepository)
    }
}
