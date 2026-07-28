package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: String,          // e.g. "EMP-101"
    val name: String,                // e.g. "Alex Mercer"
    val role: String = "Employee",   // "Employee", "Manager", "Admin"
    val basicSalary: Double = 1000.0,
    val allowance: Double = 1000.0,
    val pin: String = "1234"
)
