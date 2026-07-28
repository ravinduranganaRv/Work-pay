package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AttendanceRecord
import com.example.ui.theme.Chart10HoursColor
import com.example.ui.theme.Chart12HoursColor
import com.example.ui.theme.ChartOvertimeColor
import com.example.ui.theme.ChartShortageColor
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ShiftBreakdownChart(
    records: List<AttendanceRecord>,
    modifier: Modifier = Modifier
) {
    // Categorize records
    var count10h = 0
    var count12h = 0
    var countOt = 0
    var countShort = 0

    records.filter { it.checkOutTime != null }.forEach { record ->
        when {
            record.hoursWorked >= 12.1 -> countOt++
            record.hoursWorked >= 11.0 -> count12h++
            record.hoursWorked >= 9.5 -> count10h++
            else -> countShort++
        }
    }

    val maxVal = maxOf(count10h, count12h, countOt, countShort, 1).toFloat()

    val animProgress = remember { Animatable(0f) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(records) {
        animProgress.animateTo(1f, animationSpec = tween(1000))
    }

    GlassCard(
        modifier = modifier.testTag("shift_breakdown_chart_card"),
        cornerRadius = 20.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Work Shift Analytics",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Monthly shift duration breakdown",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                    val width = size.width
                    val height = size.height
                    val barWidth = 48.dp.toPx()
                    val gap = (width - (barWidth * 4)) / 5

                    val categories = listOf(
                        Triple("10 Hours", count10h, Chart10HoursColor),
                        Triple("12 Hours", count12h, Chart12HoursColor),
                        Triple("Overtime", countOt, ChartOvertimeColor),
                        Triple("Shortage", countShort, ChartShortageColor)
                    )

                    categories.forEachIndexed { index, triple ->
                        val x = gap + index * (barWidth + gap)
                        val barHeight = ((triple.second / maxVal) * (height - 30.dp.toPx())) * animProgress.value
                        val y = height - barHeight - 20.dp.toPx()

                        // Draw Bar Background slot
                        drawRoundRect(
                            color = SlateCardBg,
                            topLeft = Offset(x, 0f),
                            size = Size(barWidth, height - 20.dp.toPx()),
                            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                        )

                        // Draw Filled Animated Bar
                        drawRoundRect(
                            color = triple.third,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Legend Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendChip(
                    title = "10h Shift",
                    count = count10h,
                    color = Chart10HoursColor,
                    isSelected = selectedCategory == "10h",
                    onClick = { selectedCategory = if (selectedCategory == "10h") null else "10h" }
                )
                LegendChip(
                    title = "12h Full",
                    count = count12h,
                    color = Chart12HoursColor,
                    isSelected = selectedCategory == "12h",
                    onClick = { selectedCategory = if (selectedCategory == "12h") null else "12h" }
                )
                LegendChip(
                    title = "Overtime",
                    count = countOt,
                    color = ChartOvertimeColor,
                    isSelected = selectedCategory == "OT",
                    onClick = { selectedCategory = if (selectedCategory == "OT") null else "OT" }
                )
                LegendChip(
                    title = "Shortage",
                    count = countShort,
                    color = ChartShortageColor,
                    isSelected = selectedCategory == "Short",
                    onClick = { selectedCategory = if (selectedCategory == "Short") null else "Short" }
                )
            }
        }
    }
}

@Composable
private fun LegendChip(
    title: String,
    count: Int,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color.copy(alpha = 0.25f) else Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) color else color.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$title ($count)",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
