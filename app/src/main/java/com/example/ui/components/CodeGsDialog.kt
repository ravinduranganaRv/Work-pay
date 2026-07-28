package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.remote.CodeScriptProvider
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateDarkBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CodeGsDialog(
    currentWebAppUrl: String,
    onSaveWebAppUrl: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var inputUrl by remember { mutableStateOf(currentWebAppUrl) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Code.gs, 1 = index.html, 2 = Deploy Instructions

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SlateDarkBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("code_gs_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Code",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Admin Auth & Sheet Backend Code",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Web App Endpoint Input Field
                OutlinedTextField(
                    value = inputUrl,
                    onValueChange = { inputUrl = it },
                    label = { Text("Deployed Google Web App URL") },
                    placeholder = { Text("https://script.google.com/macros/s/.../exec") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "URL",
                            tint = CyanAccent
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("web_app_url_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = SlateCardBg,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        TextButton(
                            onClick = { selectedTab = 0 },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (selectedTab == 0) EmeraldGreen else TextMuted
                            )
                        ) {
                            Text("1. Code.gs", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        TextButton(
                            onClick = { selectedTab = 1 },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (selectedTab == 1) EmeraldGreen else TextMuted
                            )
                        ) {
                            Text("2. index.html", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        TextButton(
                            onClick = { selectedTab = 2 },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (selectedTab == 2) EmeraldGreen else TextMuted
                            )
                        ) {
                            Text("3. Guide", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val copyText = when (selectedTab) {
                                0 -> CodeScriptProvider.CODE_GS_SCRIPT
                                1 -> CodeScriptProvider.INDEX_HTML
                                else -> CodeScriptProvider.DEPLOYMENT_INSTRUCTIONS
                            }
                            val label = when (selectedTab) {
                                0 -> "Code.gs"
                                1 -> "index.html"
                                else -> "Setup Guide"
                            }
                            val clip = ClipData.newPlainText(label, copyText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("copy_code_gs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(14.dp),
                            tint = SlateDarkBg
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy", color = SlateDarkBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(SlateCardBg, RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0x3394A3B8), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    val scrollState = rememberScrollState()
                    Column(modifier = Modifier.verticalScroll(scrollState)) {
                        val displayText = when (selectedTab) {
                            0 -> CodeScriptProvider.CODE_GS_SCRIPT
                            1 -> CodeScriptProvider.INDEX_HTML
                            else -> CodeScriptProvider.DEPLOYMENT_INSTRUCTIONS
                        }
                        Text(
                            text = displayText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = if (selectedTab == 2) TextPrimary else CyanAccent,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSaveWebAppUrl(inputUrl.trim())
                            Toast.makeText(context, "Google Sheet Web App URL saved!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_web_app_url_button")
                    ) {
                        Text("Save Endpoint", color = SlateDarkBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
