package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateDarkBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BiometricModal(
    actionName: String, // "Check In" or "Check Out"
    onAuthSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    var isAuthenticating by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Touch sensor to verify fingerprint") }
    val scope = rememberCoroutineScope()

    val pulseScale = remember { Animatable(1.0f) }

    LaunchedEffect(isAuthenticating) {
        if (isAuthenticating) {
            pulseScale.animateTo(
                targetValue = 1.25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            pulseScale.snapTo(1.0f)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SlateDarkBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
            modifier = Modifier.testTag("biometric_modal")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Security",
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Biometric Verification",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Authorization required for $actionName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Interactive Animated Fingerprint Sensor
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(110.dp)
                        .scale(pulseScale.value)
                        .clip(CircleShape)
                        .background(
                            if (isSuccess) EmeraldGreen.copy(alpha = 0.2f)
                            else if (isAuthenticating) CyanAccent.copy(alpha = 0.25f)
                            else SlateCardBg
                        )
                        .border(
                            width = 2.dp,
                            color = if (isSuccess) EmeraldGreen else if (isAuthenticating) CyanAccent else TextMuted,
                            shape = CircleShape
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = !isAuthenticating && !isSuccess
                        ) {
                            scope.launch {
                                isAuthenticating = true
                                statusText = "Scanning biometric signature..."
                                delay(1200)
                                isAuthenticating = false
                                isSuccess = true
                                statusText = "Fingerprint Verified! Authorizing $actionName..."
                                delay(800)
                                onAuthSuccess()
                            }
                        }
                ) {
                    if (isSuccess) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(56.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Fingerprint Sensor",
                            tint = if (isAuthenticating) CyanAccent else TextPrimary,
                            modifier = Modifier
                                .size(60.dp)
                                .testTag("fingerprint_sensor_icon")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSuccess) EmeraldGreen else TextSecondary,
                    textAlign = TextAlign.Center,
                    fontWeight = if (isSuccess) FontWeight.Bold else FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("biometric_cancel_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (!isAuthenticating && !isSuccess) {
                                scope.launch {
                                    isAuthenticating = true
                                    statusText = "Verifying touch credentials..."
                                    delay(900)
                                    isAuthenticating = false
                                    isSuccess = true
                                    statusText = "Verified!"
                                    delay(600)
                                    onAuthSuccess()
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("biometric_scan_button"),
                        enabled = !isAuthenticating && !isSuccess,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isAuthenticating) "Scanning..." else "Simulate Touch",
                            color = SlateDarkBg,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
