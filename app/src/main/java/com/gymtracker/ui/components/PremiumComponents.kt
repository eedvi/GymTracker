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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymtracker.ui.theme.CardBackground
import com.gymtracker.ui.theme.CardBorder
import com.gymtracker.ui.theme.TextPrimary
import com.gymtracker.ui.theme.TextSecondary

/**
 * Minimal card with subtle border
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardShape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .clip(cardShape)
            .background(CardBackground)
            .border(1.dp, CardBorder, cardShape)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(16.dp)
    ) {
        content()
    }
}

/**
 * Gradient card - now minimal style
 */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(CardBackground, CardBackground),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardShape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .clip(cardShape)
            .background(CardBackground)
            .border(1.dp, CardBorder, cardShape)
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
 * Circular icon button with background
 */
@Composable
fun CircleIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = CardBackground,
    iconColor: Color = TextPrimary,
    size: Dp = 56.dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Surface(
            shape = CircleShape,
            color = backgroundColor,
            modifier = Modifier
                .size(size)
                .border(1.dp, CardBorder, CircleShape)
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
            color = TextSecondary
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
    valueColor: Color = TextPrimary,
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
            color = TextSecondary
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
        color = CardBackground,
        modifier = modifier.border(1.dp, CardBorder, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(4.dp)
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = index == selectedIndex
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) CardBorder else Color.Transparent,
                    modifier = Modifier.clickable { onOptionSelected(index) }
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Minimal list item card
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
            .background(CardBackground)
            .border(1.dp, CardBorder, cardShape)
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
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
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
    backgroundColor: Color = CardBorder,
    iconColor: Color = TextPrimary,
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
            color = TextPrimary
        )
        if (action != null && onActionClick != null) {
            Text(
                text = action,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                modifier = Modifier.clickable(onClick = onActionClick)
            )
        }
    }
}
