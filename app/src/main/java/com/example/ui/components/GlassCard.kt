package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateGlassBorder

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderColor: Color = SlateGlassBorder,
    glowColor: Color = Color.Unspecified,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val cardShape = RoundedCornerShape(cornerRadius)

    val backgroundBrush = if (glowColor != Color.Unspecified) {
        Brush.radialGradient(
            colors = listOf(glowColor, SlateCardBg),
            radius = 800f
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                SlateCardBg.copy(alpha = 0.95f),
                SlateCardBg.copy(alpha = 0.80f)
            )
        )
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, borderColor)
        ) {
            Box(
                modifier = Modifier
                    .background(backgroundBrush)
                    .padding(16.dp),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier,
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, borderColor)
        ) {
            Box(
                modifier = Modifier
                    .background(backgroundBrush)
                    .padding(16.dp),
                content = content
            )
        }
    }
}
