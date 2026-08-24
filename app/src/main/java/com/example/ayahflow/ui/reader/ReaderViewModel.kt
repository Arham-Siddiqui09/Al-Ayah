package com.example.ayahflow.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ayahflow.data.model.AyahEntity
import com.example.ayahflow.domain.usecase.GetAyahUseCase
import com.example.ayahflow.domain.usecase.GetProgressUseCase
import com.example.ayahflow.domain.usecase.UpdateProgressUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ReaderViewModel(
    private val getAyahUseCase: GetAyahUseCase,
    private val getProgressUseCase: GetProgressUseCase,
    private val updateProgressUseCase: UpdateProgressUseCase,
    private val syncManager: com.example.ayahflow.data.sync.QuranSyncManager
) : ViewModel() {

    private val _currentAyah = MutableStateFlow<AyahEntity?>(null)
    val currentAyah: StateFlow<AyahEntity?> = _currentAyah.asStateFlow()

    private val _currentIndex = MutableStateFlow(1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    val syncState: StateFlow<com.example.ayahflow.data.sync.SyncState> = syncManager.syncState
    val syncProgress: StateFlow<Int> = syncManager.syncProgress
    val errorMessage: StateFlow<String?> = syncManager.errorMessage

    init {
        viewModelScope.launch {
            syncManager.startSyncIfNeeded()
        }

        viewModelScope.launch {
            getProgressUseCase().collectLatest { progress ->
                val index = progress?.currentGlobalIndex ?: 1
                _currentIndex.value = index
                loadAyah(index)
            }
        }
        
        viewModelScope.launch {
            syncState.collectLatest { state ->
                if (state == com.example.ayahflow.data.sync.SyncState.COMPLETED) {
                    loadAyah(_currentIndex.value)
                }
            }
        }
    }

    fun retrySync() {
        viewModelScope.launch {
            syncManager.startSyncIfNeeded()
        }
    }

    private suspend fun loadAyah(index: Int) {
        val ayah = getAyahUseCase(index)
        if (ayah != null) {
            _currentAyah.value = ayah
        }
    }

    fun nextAyah() {
        viewModelScope.launch {
            val nextIndex = _currentIndex.value + 1
            val ayah = getAyahUseCase(nextIndex)
            if (ayah != null) {
                updateProgressUseCase(nextIndex)
            }
        }
    }

    fun previousAyah() {
        viewModelScope.launch {
            val prevIndex = _currentIndex.value - 1
            if (prevIndex > 0) {
                val ayah = getAyahUseCase(prevIndex)
                if (ayah != null) {
                    updateProgressUseCase(prevIndex)
                }
            }
        }
    }

    companion object {
        fun provideFactory(
            getAyahUseCase: GetAyahUseCase,
            getProgressUseCase: GetProgressUseCase,
            updateProgressUseCase: UpdateProgressUseCase,
            syncManager: com.example.ayahflow.data.sync.QuranSyncManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ReaderViewModel(getAyahUseCase, getProgressUseCase, updateProgressUseCase, syncManager) as T
            }
        }
    }
}
