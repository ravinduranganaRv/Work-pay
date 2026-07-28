package com.example.data.repository

import com.example.data.engine.CalculationResult
import com.example.data.engine.SalaryCalculationEngine
import com.example.data.engine.SalaryConfig
import com.example.data.local.AttendanceDao
import com.example.data.local.AttendanceRecord
import com.example.data.local.Employee
import com.example.data.local.EmployeeDao
import com.example.data.remote.GoogleSheetService
import com.example.data.remote.SheetSyncPayload
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class AttendanceRepository(
    private val dao: AttendanceDao,
    private val employeeDao: EmployeeDao
) {

    val allRecords: Flow<List<AttendanceRecord>> = dao.getAllRecords()
    val activeCheckIn: Flow<AttendanceRecord?> = dao.getActiveCheckIn()
    val allEmployees: Flow<List<Employee>> = employeeDao.getAllEmployees()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://script.google.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val googleSheetService = retrofit.create(GoogleSheetService::class.java)

    fun getRecordsForEmployee(empId: String): Flow<List<AttendanceRecord>> =
        dao.getRecordsByEmployee(empId)

    fun getActiveCheckInForEmployee(empId: String): Flow<AttendanceRecord?> =
        dao.getActiveCheckInByEmployee(empId)

    suspend fun initializeDefaultEmployees() {
        if (employeeDao.getEmployeeCount() == 0) {
            val defaults = listOf(
                Employee(employeeId = "EMP-101", name = "Alex Mercer", role = "Employee", basicSalary = 1000.0, allowance = 1000.0, pin = "1234"),
                Employee(employeeId = "EMP-102", name = "Sarah Connor", role = "Manager", basicSalary = 1500.0, allowance = 1200.0, pin = "2222"),
                Employee(employeeId = "EMP-100", name = "Admin Root", role = "Admin", basicSalary = 2000.0, allowance = 1500.0, pin = "9999")
            )
            employeeDao.insertEmployees(defaults)
        }
    }

    suspend fun insertEmployee(employee: Employee) {
        employeeDao.insertEmployee(employee)
    }

    suspend fun updateEmployee(employee: Employee) {
        employeeDao.updateEmployee(employee)
    }

    suspend fun deleteEmployee(employee: Employee) {
        employeeDao.deleteEmployee(employee)
    }

    suspend fun checkIn(
        employeeId: String = "EMP-101",
        employeeName: String = "Alex Mercer",
        timeMillis: Long = System.currentTimeMillis(),
        notes: String = ""
    ): Long {
        val dateString = SalaryCalculationEngine.formatDate(timeMillis)
        val record = AttendanceRecord(
            employeeId = employeeId,
            employeeName = employeeName,
            dateString = dateString,
            checkInTime = timeMillis,
            checkOutTime = null,
            status = "CHECKED_IN",
            notes = notes
        )
        return dao.insertRecord(record)
    }

    suspend fun checkOut(
        activeRecord: AttendanceRecord,
        checkOutTimeMillis: Long = System.currentTimeMillis(),
        salaryConfig: SalaryConfig = SalaryConfig()
    ): CalculationResult {
        val calc = SalaryCalculationEngine.calculate(
            checkInMillis = activeRecord.checkInTime,
            checkOutMillis = checkOutTimeMillis,
            config = salaryConfig
        )

        val updatedRecord = activeRecord.copy(
            checkOutTime = checkOutTimeMillis,
            hoursWorked = calc.hoursWorked,
            shiftCategory = calc.shiftCategory,
            basicEarned = calc.basicEarned,
            allowanceEarned = calc.allowanceEarned,
            otEarned = calc.otEarned,
            deductions = calc.deductions,
            netDailyPay = calc.netDailyPay,
            status = calc.status
        )

        dao.updateRecord(updatedRecord)
        return calc
    }

    suspend fun insertManualRecord(record: AttendanceRecord) {
        dao.insertRecord(record)
    }

    suspend fun updateRecord(record: AttendanceRecord) {
        dao.updateRecord(record)
    }

    suspend fun deleteRecord(record: AttendanceRecord) {
        dao.deleteRecord(record)
    }

    suspend fun syncRecordToSheet(
        record: AttendanceRecord,
        webAppUrl: String
    ): Result<String> {
        if (webAppUrl.isBlank()) {
            return Result.failure(IllegalArgumentException("Web App URL is empty"))
        }

        return try {
            val payload = SheetSyncPayload(
                id = record.id,
                employeeId = record.employeeId,
                employeeName = record.employeeName,
                timestamp = SalaryCalculationEngine.formatDate(record.checkInTime) + " " + SalaryCalculationEngine.formatTime(record.checkInTime),
                date = record.dateString,
                checkInTime = SalaryCalculationEngine.formatTime(record.checkInTime),
                checkOutTime = record.checkOutTime?.let { SalaryCalculationEngine.formatTime(it) } ?: "N/A",
                hoursWorked = record.hoursWorked,
                shiftCategory = record.shiftCategory,
                basicEarned = record.basicEarned,
                allowanceEarned = record.allowanceEarned,
                otEarned = record.otEarned,
                deductions = record.deductions,
                netDailyPay = record.netDailyPay,
                status = record.status
            )

            val response = googleSheetService.syncAttendanceRecord(webAppUrl, payload)
            if (response.isSuccessful && response.body()?.status == "success") {
                dao.updateSyncStatus(record.id, true)
                val sheetUrl = response.body()?.sheetUrl ?: ""
                Result.success(sheetUrl)
            } else {
                val errMsg = response.body()?.message ?: "Server returned error: ${response.code()}"
                Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
