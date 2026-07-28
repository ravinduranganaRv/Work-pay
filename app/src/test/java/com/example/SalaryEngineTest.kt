package com.example

import com.example.data.engine.SalaryCalculationEngine
import com.example.data.engine.SalaryConfig
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class SalaryEngineTest {

    @Test
    fun `test standard 12 hours shift calculation`() {
        val config = SalaryConfig() // 1000 basic + 1000 allowance for 30 days
        val startCal = Calendar.getInstance().apply {
            set(2026, Calendar.JULY, 28, 9, 0, 0)
        }
        val endCal = Calendar.getInstance().apply {
            set(2026, Calendar.JULY, 28, 21, 0, 0)
        }

        val result = SalaryCalculationEngine.calculate(
            checkInMillis = startCal.timeInMillis,
            checkOutMillis = endCal.timeInMillis,
            config = config
        )

        assertEquals(12.0, result.hoursWorked, 0.1)
        assertEquals("12 Hours (Full)", result.shiftCategory)
        assertEquals(33.33, result.basicEarned, 0.1)
        assertEquals(33.33, result.allowanceEarned, 0.1)
        assertEquals(0.0, result.otEarned, 0.01)
        assertEquals(0.0, result.deductions, 0.01)
        assertEquals(66.67, result.netDailyPay, 0.1)
    }

    @Test
    fun `test overtime shift calculation beyond 12 hours`() {
        val config = SalaryConfig()
        val startCal = Calendar.getInstance().apply {
            set(2026, Calendar.JULY, 28, 9, 0, 0)
        }
        val endCal = Calendar.getInstance().apply {
            set(2026, Calendar.JULY, 28, 23, 0, 0) // 14 hours worked (2h OT)
        }

        val result = SalaryCalculationEngine.calculate(
            checkInMillis = startCal.timeInMillis,
            checkOutMillis = endCal.timeInMillis,
            config = config
        )

        assertEquals(14.0, result.hoursWorked, 0.1)
        assertEquals("Overtime (>12h)", result.shiftCategory)
        assertEquals(2.0 * config.otHourlyRate, result.otEarned, 0.1)
    }

    @Test
    fun `test late check in deduction`() {
        val config = SalaryConfig()
        val startCal = Calendar.getInstance().apply {
            set(2026, Calendar.JULY, 28, 9, 30, 0) // 30 minutes late
        }
        val endCal = Calendar.getInstance().apply {
            set(2026, Calendar.JULY, 28, 21, 30, 0)
        }

        val result = SalaryCalculationEngine.calculate(
            checkInMillis = startCal.timeInMillis,
            checkOutMillis = endCal.timeInMillis,
            config = config
        )

        assertEquals(30L, result.lateMinutes)
        assertEquals(30 * config.minuteRate, result.deductions, 0.1)
    }
}
