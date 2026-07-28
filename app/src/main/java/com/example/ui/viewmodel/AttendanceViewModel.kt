package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.engine.SalaryConfig
import com.example.data.local.AppDatabase
import com.example.data.local.AttendanceRecord
import com.example.data.local.Employee
import com.example.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class KpiSummary(
    val netMonthlyEarnings: Double = 0.0,
    val totalDaysWorked: Int = 0,
    val overtimeHours: Double = 0.0,
    val totalDeductions: Double = 0.0,
    val targetMonthlySalary: Double = 2000.0
)

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AttendanceRepository(db.attendanceDao(), db.employeeDao())

    val salaryConfig = MutableStateFlow(SalaryConfig())

    val allEmployees: StateFlow<List<Employee>> = repository.allEmployees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentEmployee = MutableStateFlow(
        Employee(
            employeeId = "EMP-101",
            name = "Alex Mercer",
            role = "Employee",
            basicSalary = 1000.0,
            allowance = 1000.0,
            pin = "1234"
        )
    )

    // Filter selected by Manager or Admin: "ALL" or specific employee ID
    val selectedFilterEmployeeId = MutableStateFlow("ALL")

    val allRecords: StateFlow<List<AttendanceRecord>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val displayedRecords: StateFlow<List<AttendanceRecord>> = combine(
        allRecords,
        currentEmployee,
        selectedFilterEmployeeId
    ) { records, emp, filterId ->
        when {
            emp.role == "Employee" -> records.filter { it.employeeId == emp.employeeId }
            filterId != "ALL" -> records.filter { it.employeeId == filterId }
            else -> records
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCheckIn: StateFlow<AttendanceRecord?> = combine(
        allRecords,
        currentEmployee
    ) { records, emp ->
        records.firstOrNull { it.employeeId == emp.employeeId && it.checkOutTime == null }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val sharedPrefs = application.getSharedPreferences("workpay_prefs", Context.MODE_PRIVATE)

    private val _webAppUrl = MutableStateFlow(
        sharedPrefs.getString("web_app_url", null)?.ifBlank { null } ?: com.example.data.remote.CodeScriptProvider.DEFAULT_WEB_APP_URL
    )
    val webAppUrl: StateFlow<String> = _webAppUrl.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    val isOnline: Boolean
        get() {
            val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val net = cm.activeNetwork ?: return false
            val cap = cm.getNetworkCapabilities(net) ?: return false
            return cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

    val kpiSummary: StateFlow<KpiSummary> = displayedRecords.map { list ->
        val completed = list.filter { it.checkOutTime != null }
        val netEarnings = completed.sumOf { it.netDailyPay }
        val daysWorked = completed.count()
        val otHours = completed.sumOf { (it.hoursWorked - 12.0).coerceAtLeast(0.0) }
        val deductions = completed.sumOf { it.deductions }
        KpiSummary(
            netMonthlyEarnings = Math.round(netEarnings * 100.0) / 100.0,
            totalDaysWorked = daysWorked,
            overtimeHours = Math.round(otHours * 10.0) / 10.0,
            totalDeductions = Math.round(deductions * 100.0) / 100.0
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), KpiSummary())

    init {
        viewModelScope.launch {
            repository.initializeDefaultEmployees()
        }
    }

    fun selectEmployeeProfile(employee: Employee) {
        currentEmployee.value = employee
        salaryConfig.value = SalaryConfig(
            monthlyBasicSalary = employee.basicSalary,
            monthlyAllowance = employee.allowance
        )
        Toast.makeText(getApplication(), "Switched profile to ${employee.name} (${employee.role})", Toast.LENGTH_SHORT).show()
    }

    fun addEmployee(employee: Employee) {
        viewModelScope.launch {
            repository.insertEmployee(employee)
            Toast.makeText(getApplication(), "Staff member ${employee.name} added!", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteEmployee(employee: Employee) {
        viewModelScope.launch {
            repository.deleteEmployee(employee)
            Toast.makeText(getApplication(), "Staff member removed", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveWebAppUrl(url: String) {
        _webAppUrl.value = url
        sharedPrefs.edit().putString("web_app_url", url).apply()
    }

    fun performCheckIn(notes: String = "") {
        viewModelScope.launch {
            if (activeCheckIn.value != null) {
                Toast.makeText(getApplication(), "Already checked in!", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val emp = currentEmployee.value
            repository.checkIn(
                employeeId = emp.employeeId,
                employeeName = emp.name,
                notes = notes
            )
            Toast.makeText(getApplication(), "Check-In Recorded for ${emp.name}!", Toast.LENGTH_SHORT).show()
        }
    }

    fun performCheckOut() {
        viewModelScope.launch {
            val currentActive = activeCheckIn.value
            if (currentActive == null) {
                Toast.makeText(getApplication(), "No active check-in found!", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val calc = repository.checkOut(currentActive, salaryConfig = salaryConfig.value)
            Toast.makeText(
                getApplication(),
                "Check-Out Recorded! Earned AED ${String.format("%.2f", calc.netDailyPay)}",
                Toast.LENGTH_LONG
            ).show()

            // Auto sync if webAppUrl is set
            if (_webAppUrl.value.isNotBlank()) {
                val updatedRecord = db.attendanceDao().getRecordById(currentActive.id)
                if (updatedRecord != null) {
                    syncSingleRecord(updatedRecord)
                }
            }
        }
    }

    fun syncSingleRecord(record: AttendanceRecord) {
        viewModelScope.launch {
            val url = _webAppUrl.value
            if (url.isBlank()) {
                Toast.makeText(getApplication(), "Saved locally (Offline mode). Configure Google Sheet URL to sync.", Toast.LENGTH_LONG).show()
                return@launch
            }
            _isSyncing.value = true
            val result = repository.syncRecordToSheet(record, url)
            _isSyncing.value = false

            result.onSuccess {
                Toast.makeText(getApplication(), "Synced to Google Sheet successfully!", Toast.LENGTH_SHORT).show()
            }.onFailure { err ->
                Toast.makeText(getApplication(), "Saved locally! Sync pending: ${err.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun syncAllUnsynced() {
        viewModelScope.launch {
            val url = _webAppUrl.value
            if (url.isBlank()) {
                Toast.makeText(getApplication(), "Configure Google Sheet Web App URL in settings first", Toast.LENGTH_LONG).show()
                return@launch
            }
            val unsyncedList = allRecords.value.filter { !it.isSynced && it.checkOutTime != null }
            if (unsyncedList.isEmpty()) {
                Toast.makeText(getApplication(), "All records are already synced!", Toast.LENGTH_SHORT).show()
                return@launch
            }

            _isSyncing.value = true
            var countSuccess = 0
            for (record in unsyncedList) {
                val res = repository.syncRecordToSheet(record, url)
                if (res.isSuccess) countSuccess++
            }
            _isSyncing.value = false
            Toast.makeText(getApplication(), "Synced $countSuccess / ${unsyncedList.size} records to Google Sheet", Toast.LENGTH_SHORT).show()
        }
    }

    fun insertOrUpdateRecord(record: AttendanceRecord) {
        viewModelScope.launch {
            val emp = currentEmployee.value
            val enriched = if (record.employeeId.isBlank()) {
                record.copy(employeeId = emp.employeeId, employeeName = emp.name)
            } else record

            if (enriched.id == 0L) {
                repository.insertManualRecord(enriched)
            } else {
                repository.updateRecord(enriched)
            }
        }
    }

    fun deleteRecord(record: AttendanceRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
            Toast.makeText(getApplication(), "Record deleted", Toast.LENGTH_SHORT).show()
        }
    }
}
