package com.gymtracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gymtracker.domain.model.AppSettings
import com.gymtracker.domain.model.Language
import com.gymtracker.domain.model.WeightUnit
import com.gymtracker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        private val WEIGHT_UNIT_KEY = stringPreferencesKey("weight_unit")
    }

    override fun getSettings(): Flow<AppSettings> {
        return dataStore.data.map { preferences ->
            val languageCode = preferences[LANGUAGE_KEY] ?: Language.ENGLISH.code
            val weightUnitSymbol = preferences[WEIGHT_UNIT_KEY] ?: WeightUnit.KG.symbol

            AppSettings(
                language = Language.entries.find { it.code == languageCode } ?: Language.ENGLISH,
                weightUnit = WeightUnit.entries.find { it.symbol == weightUnitSymbol } ?: WeightUnit.KG
            )
        }
    }

    override suspend fun setLanguage(language: Language) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language.code
        }
    }

    override suspend fun setWeightUnit(unit: WeightUnit) {
        dataStore.edit { preferences ->
            preferences[WEIGHT_UNIT_KEY] = unit.symbol
        }
    }
}
