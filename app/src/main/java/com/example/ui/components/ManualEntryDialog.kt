package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.engine.SalaryCalculationEngine
import com.example.data.engine.SalaryConfig
import com.example.data.local.AttendanceRecord
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateDarkBg
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Calendar

@Composable
fun ManualEntryDialog(
    editingRecord: AttendanceRecord? = null,
    onSave: (AttendanceRecord) -> Unit,
    onDismiss: () -> Unit
) {
    var dateString by remember { mutableStateOf(editingRecord?.dateString ?: SalaryCalculationEngine.formatDate(System.currentTimeMillis())) }
    var hoursInput by remember { mutableStateOf(editingRecord?.hoursWorked?.toString() ?: "12.0") }
    var deductionsInput by remember { mutableStateOf(editingRecord?.deductions?.toString() ?: "0.0") }
    var notesInput by remember { mutableStateOf(editingRecord?.notes ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SlateDarkBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
            modifier = Modifier.testTag("manual_entry_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (editingRecord == null) "Manual Attendance Entry" else "Edit Attendance Entry",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = dateString,
                    onValueChange = { dateString = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = SlateCardBg,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = hoursInput,
                    onValueChange = { hoursInput = it },
                    label = { Text("Hours Worked (e.g., 10.0, 12.0, 13.5)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = SlateCardBg,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = deductionsInput,
                    onValueChange = { deductionsInput = it },
                    label = { Text("Manual Deductions (AED)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = SlateCardBg,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Notes / Shift Remarks") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = SlateCardBg,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val hours = hoursInput.toDoubleOrNull() ?: 12.0
                            val userDeductions = deductionsInput.toDoubleOrNull() ?: 0.0
                            val config = SalaryConfig()

                            val checkInMillis = System.currentTimeMillis() - (hours * 3600 * 1000).toLong()
                            val checkOutMillis = System.currentTimeMillis()

                            val calc = SalaryCalculationEngine.calculate(checkInMillis, checkOutMillis, config)

                            val record = AttendanceRecord(
                                id = editingRecord?.id ?: 0,
                                employeeId = editingRecord?.employeeId ?: "",
                                employeeName = editingRecord?.employeeName ?: "",
                                dateString = dateString.ifBlank { SalaryCalculationEngine.formatDate(checkInMillis) },
                                checkInTime = editingRecord?.checkInTime ?: checkInMillis,
                                checkOutTime = checkOutMillis,
                                hoursWorked = hours,
                                shiftCategory = calc.shiftCategory,
                                basicEarned = calc.basicEarned,
                                allowanceEarned = calc.allowanceEarned,
                                otEarned = calc.otEarned,
                                deductions = userDeductions + calc.deductions,
                                netDailyPay = (calc.basicEarned + calc.allowanceEarned + calc.otEarned - userDeductions - calc.deductions).coerceAtLeast(0.0),
                                status = calc.status,
                                notes = notesInput
                            )
                            onSave(record)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_manual_entry_button")
                    ) {
                        Text("Save Entry", color = SlateDarkBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
