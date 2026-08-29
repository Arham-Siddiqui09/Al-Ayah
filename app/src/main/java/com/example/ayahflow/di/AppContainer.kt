package com.example.ayahflow.di

import android.content.Context
import com.example.ayahflow.data.local.QuranDatabase
import com.example.ayahflow.data.repository.BookmarkRepository
import com.example.ayahflow.data.repository.HistoryRepository
import com.example.ayahflow.data.repository.ProgressRepository
import com.example.ayahflow.data.repository.QuranRepository
import com.example.ayahflow.data.repository.UserPreferencesRepository
import com.example.ayahflow.domain.usecase.GetAyahUseCase
import com.example.ayahflow.domain.usecase.GetProgressUseCase
import com.example.ayahflow.domain.usecase.UpdateProgressUseCase

interface AppContainer {
    val quranRepository: QuranRepository
    val progressRepository: ProgressRepository
    val bookmarkRepository: BookmarkRepository
    val historyRepository: HistoryRepository
    val userPreferencesRepository: UserPreferencesRepository

    val getAyahUseCase: GetAyahUseCase
    val getProgressUseCase: GetProgressUseCase
    val updateProgressUseCase: UpdateProgressUseCase
    val syncManager: com.example.ayahflow.data.sync.QuranSyncManager
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database: QuranDatabase by lazy { QuranDatabase.getDatabase(context) }

    override val syncManager by lazy {
        com.example.ayahflow.data.sync.QuranSyncManager(database.ayahDao())
    }

    override val quranRepository: QuranRepository by lazy {
        QuranRepository(database.ayahDao())
    }

    override val historyRepository: HistoryRepository by lazy {
        HistoryRepository(database.dailyHistoryDao())
    }

    override val progressRepository: ProgressRepository by lazy {
        ProgressRepository(database.progressDao(), historyRepository)
    }

    override val bookmarkRepository: BookmarkRepository by lazy {
        BookmarkRepository(context, database.bookmarkDao())
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context)
    }

    override val getAyahUseCase: GetAyahUseCase by lazy { GetAyahUseCase(quranRepository) }
    override val getProgressUseCase: GetProgressUseCase by lazy { GetProgressUseCase(progressRepository) }
    override val updateProgressUseCase: UpdateProgressUseCase by lazy { UpdateProgressUseCase(context, progressRepository) }
}
