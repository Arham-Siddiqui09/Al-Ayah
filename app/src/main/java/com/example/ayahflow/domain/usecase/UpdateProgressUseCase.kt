package com.example.ayahflow.domain.usecase

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.example.ayahflow.data.repository.ProgressRepository
import com.example.ayahflow.ui.widget.AyahWidget
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UpdateProgressUseCase(
    private val context: Context,
    private val progressRepository: ProgressRepository
) {
    @OptIn(DelicateCoroutinesApi::class)
    suspend operator fun invoke(globalIndex: Int) {
        progressRepository.updateProgress(globalIndex)
        // Also update the widget so it reflects the new Ayah immediately
        GlobalScope.launch {
            try {
                AyahWidget().updateAll(context)
            } catch (e: Exception) {
                // Ignore widget update errors
            }
        }
    }
}
