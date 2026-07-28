package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: String = "EMP-101",
    val employeeName: String = "Alex Mercer",
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String,           // e.g. "2026-07-28"
    val checkInTime: Long,            // Milliseconds timestamp
    val checkOutTime: Long? = null,   // Milliseconds timestamp, null if active
    val hoursWorked: Double = 0.0,
    val shiftCategory: String = "Standard 12h", // "10 Hours", "12 Hours (Full)", "Overtime (>12h)", "Short Shift"
    val basicEarned: Double = 0.0,    // AED
    val allowanceEarned: Double = 0.0,// AED
    val otEarned: Double = 0.0,       // AED
    val deductions: Double = 0.0,     // AED
    val netDailyPay: Double = 0.0,    // AED
    val status: String = "CHECKED_IN", // "CHECKED_IN", "COMPLETED", "SHORT_SHIFT", "OVERTIME"
    val isSynced: Boolean = false,
    val notes: String = ""
)
