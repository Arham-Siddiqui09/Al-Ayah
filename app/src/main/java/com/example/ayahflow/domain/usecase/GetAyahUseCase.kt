package com.example.ayahflow.domain.usecase

import com.example.ayahflow.data.model.AyahEntity
import com.example.ayahflow.data.repository.QuranRepository

class GetAyahUseCase(
    private val quranRepository: QuranRepository
) {
    suspend operator fun invoke(globalIndex: Int): AyahEntity? {
        return quranRepository.getAyahByGlobalIndex(globalIndex)
    }
}
