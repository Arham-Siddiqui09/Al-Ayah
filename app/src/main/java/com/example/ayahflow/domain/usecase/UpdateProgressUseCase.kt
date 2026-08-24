package com.example.ayahflow.domain.usecase

import com.example.ayahflow.data.repository.ProgressRepository

class UpdateProgressUseCase(
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(globalIndex: Int) {
        progressRepository.updateProgress(globalIndex)
    }
}
