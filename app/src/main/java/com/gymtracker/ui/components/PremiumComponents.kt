package com.gymtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymtracker.ui.theme.GlowPurple
import com.gymtracker.ui.theme.Primary

/**
 * Premium glass-style card with gradient border
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardShape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .clip(cardShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E1A3D),
                        Color(0xFF151030)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4A4570).copy(alpha = 0.6f),
                        Color(0xFF2A2550).copy(alpha = 0.3f)
                    )
                ),
                shape = cardShape
            )
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(20.dp)
    ) {
        content()
    }
}

/**
 * Gradient card for featured/hero sections
 */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(
        Color(0xFF6C5CE7),
        Color(0xFF8E7CF3)
    ),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardShape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .clip(cardShape)
            .background(
                brush = Brush.linearGradient(colors = gradientColors)
            )
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(24.dp)
    ) {
        content()
    }
}

/**
 * Circular icon button with background
 */
@Composable
fun CircleIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF2A2555),
    iconColor: Color = Color.White,
    size: Dp = 56.dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Surface(
            shape = CircleShape,
            color = backgroundColor,
            modifier = Modifier.size(size)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(size * 0.45f)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

/**
 * Large stat display with label
 */
@Composable
fun StatDisplay(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.White,
    suffix: String? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            if (suffix != null) {
                Text(
                    text = suffix,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = valueColor.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

/**
 * Horizontal stat row for workout stats
 */
@Composable
fun StatsRow(
    stats: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        stats.forEach { (value, label) ->
            StatDisplay(value = value, label = label)
        }
    }
}

/**
 * Pill-shaped segmented control
 */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1A1535),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(4.dp)
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = index == selectedIndex
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Primary else Color.Transparent,
                    modifier = Modifier
                        .clickable { onOptionSelected(index) }
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Premium list item card
 */
@Composable
fun PremiumListItem(
    title: String,
    subtitle: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF1E1A3D),
                        Color(0xFF1A1535)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color(0xFF3A3560).copy(alpha = 0.5f),
                shape = cardShape
            )
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(modifier = Modifier.width(16.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }

        if (trailingContent != null) {
            trailingContent()
        }
    }
}

/**
 * Icon badge for list items
 */
@Composable
fun IconBadge(
    icon: ImageVector,
    backgroundColor: Color = Primary.copy(alpha = 0.2f),
    iconColor: Color = Primary,
    size: Dp = 48.dp
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = Modifier.size(size)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
}

/**
 * Section header with optional action
 */
@Composable
fun SectionHeader(
    title: String,
    action: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        if (action != null && onActionClick != null) {
            Text(
                text = action,
                style = MaterialTheme.typography.labelMedium,
                color = Primary,
                modifier = Modifier.clickable(onClick = onActionClick)
            )
        }
    }
}
