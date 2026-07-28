package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records ORDER BY checkInTime DESC")
    fun getAllRecords(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE employeeId = :employeeId ORDER BY checkInTime DESC")
    fun getRecordsByEmployee(employeeId: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE checkOutTime IS NULL ORDER BY checkInTime DESC LIMIT 1")
    fun getActiveCheckIn(): Flow<AttendanceRecord?>

    @Query("SELECT * FROM attendance_records WHERE employeeId = :employeeId AND checkOutTime IS NULL ORDER BY checkInTime DESC LIMIT 1")
    fun getActiveCheckInByEmployee(employeeId: String): Flow<AttendanceRecord?>

    @Query("SELECT * FROM attendance_records WHERE id = :id")
    suspend fun getRecordById(id: Long): AttendanceRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceRecord): Long

    @Update
    suspend fun updateRecord(record: AttendanceRecord)

    @Delete
    suspend fun deleteRecord(record: AttendanceRecord)

    @Query("DELETE FROM attendance_records")
    suspend fun deleteAllRecords()

    @Query("UPDATE attendance_records SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, synced: Boolean)
}
