package com.gymtracker.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    // Extra small - chips, small badges
    extraSmall = RoundedCornerShape(8.dp),

    // Small - buttons, text fields
    small = RoundedCornerShape(12.dp),

    // Medium - cards, dialogs
    medium = RoundedCornerShape(16.dp),

    // Large - bottom sheets, large cards
    large = RoundedCornerShape(24.dp),

    // Extra large - full screen dialogs
    extraLarge = RoundedCornerShape(32.dp)
)
