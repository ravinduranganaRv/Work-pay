package com.example.data.engine

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

data class SalaryConfig(
    val monthlyBasicSalary: Double = 1000.0,
    val monthlyAllowance: Double = 1000.0,
    val daysInMonth: Int = 30,
    val standardShiftHours: Double = 12.0,
    val standardStartHour: Int = 9,  // 9:00 AM
    val standardStartMinute: Int = 0,
    val standardEndHour: Int = 21,   // 9:00 PM
    val otMultiplier: Double = 1.5
) {
    val dailyBasic: Double get() = monthlyBasicSalary / daysInMonth
    val dailyAllowance: Double get() = monthlyAllowance / daysInMonth
    val totalDailyBase: Double get() = dailyBasic + dailyAllowance

    // Deductions calculated strictly based on Basic Salary (e.g. 1,000 AED base) under Dubai labor laws
    val basicHourlyRate: Double get() = dailyBasic / standardShiftHours
    val basicMinuteRate: Double get() = basicHourlyRate / 60.0

    val hourlyRate: Double get() = totalDailyBase / standardShiftHours
    val minuteRate: Double get() = hourlyRate / 60.0
    val otHourlyRate: Double get() = hourlyRate * otMultiplier
}

data class CalculationResult(
    val hoursWorked: Double,
    val shiftCategory: String, // "10 Hours", "12 Hours (Full)", "Overtime (>12h)", "Short Shift"
    val basicEarned: Double,
    val allowanceEarned: Double,
    val otEarned: Double,
    val deductions: Double,
    val netDailyPay: Double,
    val status: String,
    val lateMinutes: Long
)

object SalaryCalculationEngine {

    fun calculate(
        checkInMillis: Long,
        checkOutMillis: Long,
        config: SalaryConfig = SalaryConfig()
    ): CalculationResult {
        val durationMs = max(0L, checkOutMillis - checkInMillis)
        val hoursWorked = (durationMs.toDouble() / (1000.0 * 60.0 * 60.0))

        // Check for late arrival based on scheduled 9:00 AM
        val checkInCalendar = Calendar.getInstance().apply { timeInMillis = checkInMillis }
        val scheduledStart = Calendar.getInstance().apply {
            timeInMillis = checkInMillis
            set(Calendar.HOUR_OF_DAY, config.standardStartHour)
            set(Calendar.MINUTE, config.standardStartMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val lateMinutes = if (checkInCalendar.after(scheduledStart)) {
            val diffMs = checkInCalendar.timeInMillis - scheduledStart.timeInMillis
            diffMs / (1000 * 60)
        } else {
            0L
        }

        // Deductions calculation (late minutes + short shift penalty or absent days)
        // Calculated proportionally based strictly on the employee's Basic Salary (1,000 AED base) in accordance with Dubai labor laws
        var deductions = lateMinutes * config.basicMinuteRate
        if (hoursWorked < 10.0 && hoursWorked > 0) {
            val shortHours = 10.0 - hoursWorked
            deductions += shortHours * config.basicHourlyRate
        } else if (hoursWorked == 0.0) {
            deductions = config.dailyBasic
        }

        // Standard hours (capped at 12 hours)
        val standardHoursWorked = hoursWorked.coerceAtMost(config.standardShiftHours)
        val proportion = (standardHoursWorked / config.standardShiftHours).coerceIn(0.0, 1.0)

        val basicEarned = config.dailyBasic * proportion
        val allowanceEarned = config.dailyAllowance * proportion

        // Overtime calculation (> 12 hours)
        val otHours = max(0.0, hoursWorked - config.standardShiftHours)
        val otEarned = otHours * config.otHourlyRate

        // Net daily pay calculation
        val netDailyPay = max(0.0, basicEarned + allowanceEarned + otEarned - deductions)

        // Categorize Shift
        val (shiftCategory, status) = when {
            hoursWorked >= 12.0 + 0.1 -> "Overtime (>12h)" to "OVERTIME"
            hoursWorked >= 11.0 -> "12 Hours (Full)" to "COMPLETED"
            hoursWorked >= 9.5 -> "10 Hours Shift" to "COMPLETED"
            else -> "Short Shift" to "SHORT_SHIFT"
        }

        return CalculationResult(
            hoursWorked = Math.round(hoursWorked * 100.0) / 100.0,
            shiftCategory = shiftCategory,
            basicEarned = Math.round(basicEarned * 100.0) / 100.0,
            allowanceEarned = Math.round(allowanceEarned * 100.0) / 100.0,
            otEarned = Math.round(otEarned * 100.0) / 100.0,
            deductions = Math.round(deductions * 100.0) / 100.0,
            netDailyPay = Math.round(netDailyPay * 100.0) / 100.0,
            status = status,
            lateMinutes = lateMinutes
        )
    }

    fun formatDate(timeMillis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timeMillis))
    }

    fun formatTime(timeMillis: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timeMillis))
    }
}
