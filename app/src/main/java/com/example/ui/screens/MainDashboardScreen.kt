package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.biometric.BiometricAuthManager
import com.example.data.engine.SalaryCalculationEngine
import com.example.data.local.AttendanceRecord
import com.example.ui.components.AttendanceItemCard
import com.example.ui.components.BiometricModal
import com.example.ui.components.CodeGsDialog
import com.example.ui.components.EmployeeManagementDialog
import com.example.ui.components.GlassCard
import com.example.ui.components.KpiCard
import com.example.ui.components.ManualEntryDialog
import com.example.ui.components.ShiftBreakdownChart
import com.example.ui.theme.AmberOvertime
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoseDeductions
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateDarkBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AttendanceViewModel
import kotlinx.coroutines.delay

@Composable
fun MainDashboardScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val displayedRecords by viewModel.displayedRecords.collectAsStateWithLifecycle()
    val activeCheckIn by viewModel.activeCheckIn.collectAsStateWithLifecycle()
    val kpiSummary by viewModel.kpiSummary.collectAsStateWithLifecycle()
    val webAppUrl by viewModel.webAppUrl.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    val allEmployees by viewModel.allEmployees.collectAsStateWithLifecycle()
    val currentEmployee by viewModel.currentEmployee.collectAsStateWithLifecycle()
    val selectedFilterId by viewModel.selectedFilterEmployeeId.collectAsStateWithLifecycle()

    var showBiometricModal by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<String?>(null) } // "CHECK_IN" or "CHECK_OUT"
    var showCodeGsDialog by remember { mutableStateOf(false) }
    var showAdminAuthDialog by remember { mutableStateOf(false) }
    var showManualEntryDialog by remember { mutableStateOf(false) }
    var showEmployeeDialog by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<AttendanceRecord?>(null) }

    // Role colors
    val roleColor = when (currentEmployee.role) {
        "Admin" -> RoseDeductions
        "Manager" -> AmberOvertime
        else -> EmeraldGreen
    }

    // Live elapsed timer for active check-in
    var activeTimerText by remember { mutableStateOf("00:00:00") }
    LaunchedEffect(activeCheckIn) {
        while (activeCheckIn != null) {
            val diffMs = System.currentTimeMillis() - activeCheckIn!!.checkInTime
            val hrs = diffMs / (1000 * 3600)
            val mins = (diffMs % (1000 * 3600)) / (1000 * 60)
            val secs = (diffMs % (1000 * 60)) / 1000
            activeTimerText = String.format("%02d:%02d:%02d", hrs, mins, secs)
            delay(1000)
        }
    }

    // Trigger biometric authentication with native prompt or fallback modal
    fun triggerBiometricAuth(action: String) {
        pendingAction = action
        val fragmentActivity = context as? FragmentActivity
        if (fragmentActivity != null && BiometricAuthManager.isBiometricAvailable(fragmentActivity)) {
            BiometricAuthManager.authenticate(
                activity = fragmentActivity,
                title = "WorkPay Biometric $action",
                subtitle = "Authenticate fingerprint for ${currentEmployee.name} ($action)",
                onSuccess = {
                    if (action == "CHECK_IN") viewModel.performCheckIn()
                    else viewModel.performCheckOut()
                    pendingAction = null
                },
                onError = { err ->
                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                    showBiometricModal = true
                },
                onFallbackRequested = {
                    showBiometricModal = true
                }
            )
        } else {
            showBiometricModal = true
        }
    }

    Scaffold(
        containerColor = SlateDarkBg,
        modifier = modifier.fillMaxSize().testTag("main_dashboard_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // TOP APP BAR & HEADER WITH ROLE SWITCHER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = EmeraldGreen.copy(alpha = 0.2f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Logo",
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "WorkPay",
                                style = MaterialTheme.typography.displayLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }
                        Text(
                            text = "Attendance & Salary Tracking Hub",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // User Profile & Role Switch Chip
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SlateCardBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, roleColor.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .clickable { showEmployeeDialog = true }
                                .testTag("profile_role_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (currentEmployee.role == "Admin") Icons.Default.Security else Icons.Default.Person,
                                    contentDescription = "Role",
                                    tint = roleColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = currentEmployee.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = currentEmployee.role,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = roleColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        if (currentEmployee.role == "Admin" || currentEmployee.role == "Manager") {
                            IconButton(
                                onClick = { showAdminAuthDialog = true },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(SlateCardBg)
                                    .testTag("admin_auth_header_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Admin Security",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(
                                onClick = { showCodeGsDialog = true },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(SlateCardBg)
                                    .testTag("code_gs_header_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = "Code.gs",
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.syncAllUnsynced() },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SlateCardBg)
                                .testTag("sync_all_button")
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = CyanAccent,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Sync All",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // OFFLINE DB STORAGE & SYNC STATUS INDICATOR BAR
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x1F1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateCardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = "Storage",
                                tint = CyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Local Room DB Storage Active",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (webAppUrl.isNotBlank()) EmeraldGreen else AmberOvertime)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSyncing) "Syncing..." else if (webAppUrl.isNotBlank()) "Synced / Online" else "Offline Mode",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (webAppUrl.isNotBlank()) EmeraldGreen else AmberOvertime,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // MANAGER & ADMIN TEAM FILTER DROPDOWN / CHIPS
            if (currentEmployee.role == "Manager" || currentEmployee.role == "Admin") {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = CyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Filter Team View (Role: ${currentEmployee.role})",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedFilterId == "ALL",
                                    onClick = { viewModel.selectedFilterEmployeeId.value = "ALL" },
                                    label = { Text("All Team (${allEmployees.size})") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyanAccent,
                                        selectedLabelColor = SlateDarkBg,
                                        containerColor = SlateCardBg,
                                        labelColor = TextPrimary
                                    )
                                )
                            }
                            items(allEmployees, key = { it.employeeId }) { emp ->
                                FilterChip(
                                    selected = selectedFilterId == emp.employeeId,
                                    onClick = { viewModel.selectedFilterEmployeeId.value = emp.employeeId },
                                    label = { Text("${emp.name} (${emp.employeeId})") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyanAccent,
                                        selectedLabelColor = SlateDarkBg,
                                        containerColor = SlateCardBg,
                                        labelColor = TextPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // ACTIVE CHECK-IN STATUS BANNER
            item {
                AnimatedVisibility(visible = activeCheckIn != null) {
                    activeCheckIn?.let { record ->
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            glowColor = CyanAccent.copy(alpha = 0.25f),
                            cornerRadius = 16.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(CyanAccent)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "SHIFT ACTIVE - ${record.employeeName}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = CyanAccent,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Checked In at ${SalaryCalculationEngine.formatTime(record.checkInTime)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SlateDarkBg,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = activeTimerText,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = CyanAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // MAIN ACTION BUTTONS (CHECK IN / CHECK OUT / VIEW GOOGLE SHEET)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Check In Button
                        Button(
                            onClick = { triggerBiometricAuth("CHECK_IN") },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("check_in_button"),
                            enabled = activeCheckIn == null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanAccent,
                                disabledContainerColor = SlateCardBg
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Login,
                                contentDescription = "Check In",
                                tint = if (activeCheckIn == null) SlateDarkBg else TextMuted
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CHECK IN",
                                color = if (activeCheckIn == null) SlateDarkBg else TextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        // Check Out Button
                        Button(
                            onClick = { triggerBiometricAuth("CHECK_OUT") },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("check_out_button"),
                            enabled = activeCheckIn != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldGreen,
                                disabledContainerColor = SlateCardBg
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Check Out",
                                tint = if (activeCheckIn != null) SlateDarkBg else TextMuted
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CHECK OUT",
                                color = if (activeCheckIn != null) SlateDarkBg else TextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    // View Google Sheet Dynamic Button
                    Button(
                        onClick = {
                            if (webAppUrl.isNotBlank()) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webAppUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Unable to open URL: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                if (currentEmployee.role == "Admin" || currentEmployee.role == "Manager") {
                                    showCodeGsDialog = true
                                } else {
                                    Toast.makeText(context, "Only Managers & Admins can configure Google Sheet backend", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("view_google_sheet_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SlateCardBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Sheet",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (webAppUrl.isNotBlank()) "View Google Sheet" else "Configure Google Sheet Backend",
                            color = EmeraldGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // LIVE KPI CARDS GRID
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Salary & Monthly Overview (${if (selectedFilterId == "ALL") "All Team" else selectedFilterId})",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        KpiCard(
                            title = "Net Earnings",
                            value = "AED ${String.format("%.2f", kpiSummary.netMonthlyEarnings)}",
                            subtitle = "Target: 2,000 AED Base",
                            icon = Icons.Default.Payments,
                            iconTint = EmeraldGreen,
                            progress = (kpiSummary.netMonthlyEarnings / kpiSummary.targetMonthlySalary).toFloat(),
                            modifier = Modifier.weight(1f),
                            testTagStr = "kpi_net_earnings"
                        )

                        KpiCard(
                            title = "Days Worked",
                            value = "${kpiSummary.totalDaysWorked} Days",
                            subtitle = "Standard: 30 days shift",
                            icon = Icons.Default.DateRange,
                            iconTint = CyanAccent,
                            progress = kpiSummary.totalDaysWorked / 30f,
                            modifier = Modifier.weight(1f),
                            testTagStr = "kpi_days_worked"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        KpiCard(
                            title = "Overtime Worked",
                            value = "${kpiSummary.overtimeHours} Hrs",
                            subtitle = "Capped beyond 12h daily",
                            icon = Icons.Default.Schedule,
                            iconTint = AmberOvertime,
                            modifier = Modifier.weight(1f),
                            testTagStr = "kpi_overtime"
                        )

                        KpiCard(
                            title = "Total Deductions",
                            value = "AED ${String.format("%.2f", kpiSummary.totalDeductions)}",
                            subtitle = "Late arrivals & shortage",
                            icon = Icons.Default.MoneyOff,
                            iconTint = RoseDeductions,
                            modifier = Modifier.weight(1f),
                            testTagStr = "kpi_deductions"
                        )
                    }
                }
            }

            // INTERACTIVE SHIFT BREAKDOWN CHART
            item {
                ShiftBreakdownChart(records = displayedRecords)
            }

            // ATTENDANCE LOGS LIST HEADER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Attendance Records (${displayedRecords.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedButton(
                        onClick = {
                            editingRecord = null
                            showManualEntryDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("add_manual_entry_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Manual Log", color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ATTENDANCE ITEMS OR EMPTY STATE
            if (displayedRecords.isEmpty()) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Empty",
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Attendance Logs Found",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Press 'Check In' above with biometric fingerprint auth to record a shift!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(displayedRecords, key = { it.id }) { record ->
                    AttendanceItemCard(
                        record = record,
                        onSyncClick = { viewModel.syncSingleRecord(it) },
                        onEditClick = {
                            editingRecord = it
                            showManualEntryDialog = true
                        },
                        onDeleteClick = { viewModel.deleteRecord(it) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // MODALS & DIALOGS
    if (showEmployeeDialog) {
        EmployeeManagementDialog(
            employees = allEmployees,
            currentEmployee = currentEmployee,
            onSelectEmployee = { emp ->
                viewModel.selectEmployeeProfile(emp)
                showEmployeeDialog = false
            },
            onAddEmployee = { emp ->
                viewModel.addEmployee(emp)
            },
            onDeleteEmployee = { emp ->
                viewModel.deleteEmployee(emp)
            },
            onDismiss = { showEmployeeDialog = false }
        )
    }

    if (showBiometricModal) {
        BiometricModal(
            actionName = if (pendingAction == "CHECK_IN") "Check In" else "Check Out",
            onAuthSuccess = {
                if (pendingAction == "CHECK_IN") {
                    viewModel.performCheckIn()
                } else {
                    viewModel.performCheckOut()
                }
                showBiometricModal = false
                pendingAction = null
            },
            onDismiss = {
                showBiometricModal = false
                pendingAction = null
            }
        )
    }

    if (showCodeGsDialog) {
        CodeGsDialog(
            currentWebAppUrl = webAppUrl,
            onSaveWebAppUrl = { viewModel.saveWebAppUrl(it) },
            onDismiss = { showCodeGsDialog = false }
        )
    }

    if (showAdminAuthDialog) {
        com.example.ui.components.AdminAuthDialog(
            onAdminAuthenticated = { email ->
                Toast.makeText(context, "Admin Access Granted: $email", Toast.LENGTH_LONG).show()
                showAdminAuthDialog = false
            },
            onDismiss = { showAdminAuthDialog = false }
        )
    }

    if (showManualEntryDialog) {
        ManualEntryDialog(
            editingRecord = editingRecord,
            onSave = { record ->
                viewModel.insertOrUpdateRecord(record)
                showManualEntryDialog = false
            },
            onDismiss = { showManualEntryDialog = false }
        )
    }
}
