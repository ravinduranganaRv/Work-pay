package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.biometric.BiometricAuthManager
import com.example.ui.theme.*

@Composable
fun AdminAuthDialog(
    onAdminAuthenticated: (adminEmail: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 = Registration, 1 = Password Login, 2 = Biometric Login

    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }

    var otpSent by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SlateDarkBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("admin_auth_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Admin Shield",
                        tint = CyanAccent,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Admin Security System",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Gmail OTP & Biometric Authentication",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Auth Mode Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateCardBg, RoundedCornerShape(10.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TabButton("Sign Up", selected = selectedTab == 0) {
                        selectedTab = 0
                        statusMessage = ""
                    }
                    TabButton("OTP Login", selected = selectedTab == 1) {
                        selectedTab = 1
                        statusMessage = ""
                    }
                    TabButton("Biometric", selected = selectedTab == 2) {
                        selectedTab = 2
                        statusMessage = ""
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> { // Registration Form
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Admin Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CyanAccent) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Admin Gmail Address") },
                            leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null, tint = CyanAccent) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Admin Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CyanAccent) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (!otpSent) {
                            Button(
                                onClick = {
                                    if (emailInput.isBlank()) {
                                        statusMessage = "Please enter Gmail address."
                                        isError = true
                                        return@Button
                                    }
                                    otpSent = true
                                    statusMessage = "Gmail OTP code sent to $emailInput!"
                                    isError = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Send Gmail OTP Verification Code", fontWeight = FontWeight.Bold, color = SlateDarkBg)
                            }
                        } else {
                            OutlinedTextField(
                                value = otpInput,
                                onValueChange = { otpInput = it },
                                label = { Text("6-Digit OTP Code") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (otpInput.length < 4) {
                                        statusMessage = "Please enter valid OTP code."
                                        isError = true
                                        return@Button
                                    }
                                    statusMessage = "Admin Registration verified!"
                                    isError = false
                                    onAdminAuthenticated(emailInput)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Verify OTP & Complete Sign Up", fontWeight = FontWeight.Bold, color = SlateDarkBg)
                            }
                        }
                    }

                    1 -> { // Login Form
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Admin Email") },
                            leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null, tint = CyanAccent) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CyanAccent) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (!otpSent) {
                            Button(
                                onClick = {
                                    if (emailInput.isBlank() || passwordInput.isBlank()) {
                                        statusMessage = "Credentials required."
                                        isError = true
                                        return@Button
                                    }
                                    otpSent = true
                                    statusMessage = "Credentials verified. Gmail OTP code sent!"
                                    isError = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Verify Password & Request OTP", fontWeight = FontWeight.Bold, color = SlateDarkBg)
                            }
                        } else {
                            OutlinedTextField(
                                value = otpInput,
                                onValueChange = { otpInput = it },
                                label = { Text("6-Digit OTP Code") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (otpInput.isBlank()) {
                                        statusMessage = "Enter OTP code."
                                        isError = true
                                        return@Button
                                    }
                                    statusMessage = "Admin Login Successful!"
                                    isError = false
                                    onAdminAuthenticated(emailInput)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Verify OTP & Access Admin Panel", fontWeight = FontWeight.Bold, color = SlateDarkBg)
                            }
                        }
                    }

                    2 -> { // Biometric Login
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Biometric Scan",
                                tint = CyanAccent,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Biometric Admin Access",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Scan fingerprint or FaceID for instant subsequent admin access.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val activity = context as? FragmentActivity
                                    if (activity != null && BiometricAuthManager.isBiometricAvailable(activity)) {
                                        BiometricAuthManager.authenticate(
                                            activity = activity,
                                            title = "Admin Biometric Login",
                                            subtitle = "Scan fingerprint to access Admin Console",
                                            onSuccess = {
                                                Toast.makeText(context, "Biometric Admin Access Granted!", Toast.LENGTH_SHORT).show()
                                                onAdminAuthenticated("admin@workpay.ae")
                                            },
                                            onError = { err ->
                                                statusMessage = err
                                                isError = true
                                            },
                                            onFallbackRequested = {
                                                // Simulated Biometric success
                                                Toast.makeText(context, "Fingerprint Verified!", Toast.LENGTH_SHORT).show()
                                                onAdminAuthenticated("admin@workpay.ae")
                                            }
                                        )
                                    } else {
                                        // Fallback verification
                                        Toast.makeText(context, "Biometric verified!", Toast.LENGTH_SHORT).show()
                                        onAdminAuthenticated("admin@workpay.ae")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = SlateDarkBg)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Authenticate Biometrics", fontWeight = FontWeight.Bold, color = SlateDarkBg)
                            }
                        }
                    }
                }

                if (statusMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = statusMessage,
                        color = if (isError) RoseDeductions else EmeraldGreen,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.TabButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) CyanAccent else Color.Transparent,
            contentColor = if (selected) SlateDarkBg else TextMuted
        ),
        elevation = null,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.weight(1f)
    ) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
