package com.gymtracker.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymtracker.domain.model.Language
import com.gymtracker.domain.model.WeightUnit
import com.gymtracker.ui.theme.Background
import com.gymtracker.ui.theme.CardBackground
import com.gymtracker.ui.theme.CardBorder
import com.gymtracker.ui.theme.TextPrimary
import com.gymtracker.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CardBackground,
                    modifier = Modifier
                        .size(44.dp)
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .clickable(onClick = onNavigateBack)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = "Settings",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Settings Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Language Setting
                SettingRow(
                    label = "Language",
                    options = listOf(
                        OptionItem("EN", Language.ENGLISH),
                        OptionItem("ESP", Language.SPANISH)
                    ),
                    selectedOption = settings.language,
                    onOptionSelected = { viewModel.setLanguage(it) }
                )

                // Weight Unit Setting
                SettingRow(
                    label = "Weight Unit",
                    options = listOf(
                        OptionItem("kg", WeightUnit.KG),
                        OptionItem("lb", WeightUnit.LBS)
                    ),
                    selectedOption = settings.weightUnit,
                    onOptionSelected = { viewModel.setWeightUnit(it) }
                )
            }
        }
    }
}

data class OptionItem<T>(
    val label: String,
    val value: T
)

@Composable
private fun <T> SettingRow(
    label: String,
    options: List<OptionItem<T>>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit
) {
    val cardShape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(CardBackground)
            .border(1.dp, CardBorder, cardShape)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    OptionPill(
                        label = option.label,
                        isSelected = option.value == selectedOption,
                        onClick = { onOptionSelected(option.value) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    val backgroundColor = if (isSelected) CardBorder else CardBackground
    val borderColor = if (isSelected) TextSecondary else CardBorder

    Surface(
        onClick = onClick,
        shape = shape,
        color = backgroundColor,
        modifier = Modifier.border(1.dp, borderColor, shape)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) TextPrimary else TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}
