package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

@JsonClass(generateAdapter = true)
data class SheetSyncPayload(
    @Json(name = "action") val action: String = "syncRecord",
    @Json(name = "id") val id: Long,
    @Json(name = "employeeId") val employeeId: String = "EMP-101",
    @Json(name = "employeeName") val employeeName: String = "Alex Mercer",
    @Json(name = "timestamp") val timestamp: String,
    @Json(name = "date") val date: String,
    @Json(name = "checkInTime") val checkInTime: String,
    @Json(name = "checkOutTime") val checkOutTime: String,
    @Json(name = "hoursWorked") val hoursWorked: Double,
    @Json(name = "shiftCategory") val shiftCategory: String,
    @Json(name = "basicEarned") val basicEarned: Double,
    @Json(name = "allowanceEarned") val allowanceEarned: Double,
    @Json(name = "otEarned") val otEarned: Double,
    @Json(name = "deductions") val deductions: Double,
    @Json(name = "netDailyPay") val netDailyPay: Double,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class SheetApiResponse(
    @Json(name = "status") val status: String?, // "success" or "error"
    @Json(name = "message") val message: String?,
    @Json(name = "sheetUrl") val sheetUrl: String?,
    @Json(name = "sheetName") val sheetName: String?
)

interface GoogleSheetService {
    @POST
    suspend fun syncAttendanceRecord(
        @Url url: String,
        @Body payload: SheetSyncPayload
    ): Response<SheetApiResponse>

    @GET
    suspend fun fetchSheetInfo(
        @Url url: String
    ): Response<SheetApiResponse>
}
