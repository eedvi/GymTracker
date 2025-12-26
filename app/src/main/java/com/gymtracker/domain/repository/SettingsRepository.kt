package com.gymtracker.domain.repository

import com.gymtracker.domain.model.AppSettings
import com.gymtracker.domain.model.Language
import com.gymtracker.domain.model.WeightUnit
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun setLanguage(language: Language)
    suspend fun setWeightUnit(unit: WeightUnit)
}
