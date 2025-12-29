package com.gymtracker.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.domain.model.AppSettings
import com.gymtracker.domain.model.Language
import com.gymtracker.domain.model.WeightUnit
import com.gymtracker.domain.repository.SettingsRepository
import com.gymtracker.util.LocaleHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    fun setLanguage(language: Language) {
        viewModelScope.launch {
            settingsRepository.setLanguage(language)
            LocaleHelper.setLocale(language)
        }
    }

    fun setWeightUnit(unit: WeightUnit) {
        viewModelScope.launch {
            settingsRepository.setWeightUnit(unit)
        }
    }
}
