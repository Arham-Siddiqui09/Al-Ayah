package com.example.ayahflow.domain.usecase

import com.example.ayahflow.data.model.ProgressEntity
import com.example.ayahflow.data.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow

class GetProgressUseCase(
    private val progressRepository: ProgressRepository
) {
    operator fun invoke(): Flow<ProgressEntity?> {
        return progressRepository.getProgressFlow()
    }
}
