package com.example.data.remote

object CodeScriptProvider {

    val CODE_GS_SCRIPT: String = "https://script.google.com/macros/s/AKfycbz-qtiX5j03nCp7O0fcIHSyoaEySrJ-uuey6c6R14jIwHMJc2BV9xT88db6rYRtjuX1/exec""
/**
 * ==============================================================================
 * WORKPAY AUTOMATED GOOGLE SHEET BACKEND (Code.gs)
 * ==============================================================================
 * Automatically provisions a Google Sheet named "My_Attendance_&_Salary_DB"
 * in Google Drive if it doesn't exist, formats styled headers, handles CORS,
 * and provides doGet / doPost endpoints for live attendance & salary tracking.
 */

var SHEET_NAME_DB = "My_Attendance_&_Salary_DB";

/**
 * Ensures database spreadsheet exists, creates headers if new, and returns active Sheet.
 */
function getOrCreateSpreadsheet() {
  var files = DriveApp.getFilesByName(SHEET_NAME_DB);
  var ss;
  if (files.hasNext()) {
    ss = SpreadsheetApp.open(files.next());
  } else {
    ss = SpreadsheetApp.create(SHEET_NAME_DB);
    var sheet = ss.getActiveSheet();
    sheet.setName("Attendance_Logs");
    
    // Styled Headers
    var headers = [
      "Timestamp", 
      "Date", 
      "Employee_ID",
      "Employee_Name",
      "CheckIn_Time", 
      "CheckOut_Time", 
      "Hours_Worked", 
      "Shift_Category", 
      "Basic_Earned", 
      "Allowance_Earned", 
      "OT_Earned", 
      "Deductions", 
      "Net_Daily_Pay", 
      "Status"
    ];
    
    sheet.appendRow(headers);
    
    // Header Styling (Dark Emerald background, white bold text)
    var headerRange = sheet.getRange(1, 1, 1, headers.length);
    headerRange.setBackground("#064E3B");
    headerRange.setFontColor("#FFFFFF");
    headerRange.setFontWeight("bold");
    headerRange.setFontSize(11);
    headerRange.setHorizAlignment("center");
    
    sheet.setFrozenRows(1);
    sheet.setColumnWidths(1, headers.length, 130);
  }
  return ss;
}

/**
 * GET Handler - Returns Database status and Google Sheet URL.
 */
function doGet(e) {
  try {
    var ss = getOrCreateSpreadsheet();
    var response = {
      "status": "success",
      "message": "WorkPay Google Sheet Backend active.",
      "sheetUrl": ss.getUrl(),
      "sheetName": SHEET_NAME_DB
    };
    return ContentService
      .createTextOutput(JSON.stringify(response))
      .setMimeType(ContentService.MimeType.JSON);
  } catch (err) {
    return ContentService
      .createTextOutput(JSON.stringify({"status": "error", "message": err.toString()}))
      .setMimeType(ContentService.MimeType.JSON);
  }
}

/**
 * POST Handler - Syncs attendance record to Google Sheet.
 */
function doPost(e) {
  try {
    var ss = getOrCreateSpreadsheet();
    var sheet = ss.getSheetByName("Attendance_Logs") || ss.getActiveSheet();
    
    var data = {};
    if (e.postData && e.postData.contents) {
      data = JSON.parse(e.postData.contents);
    } else {
      data = e.parameter;
    }
    
    var timestamp = data.timestamp || new Date().toISOString();
    var date = data.date || "";
    var employeeId = data.employeeId || "EMP-101";
    var employeeName = data.employeeName || "Alex Mercer";
    var checkInTime = data.checkInTime || "";
    var checkOutTime = data.checkOutTime || "";
    var hoursWorked = data.hoursWorked || 0;
    var shiftCategory = data.shiftCategory || "";
    var basicEarned = data.basicEarned || 0;
    var allowanceEarned = data.allowanceEarned || 0;
    var otEarned = data.otEarned || 0;
    var deductions = data.deductions || 0;
    var netDailyPay = data.netDailyPay || 0;
    var status = data.status || "COMPLETED";

    // Append new record row
    sheet.appendRow([
      timestamp,
      date,
      employeeId,
      employeeName,
      checkInTime,
      checkOutTime,
      hoursWorked,
      shiftCategory,
      basicEarned,
      allowanceEarned,
      otEarned,
      deductions,
      netDailyPay,
      status
    ]);
    
    // Auto format numbers
    var lastRow = sheet.getLastRow();
    sheet.getRange(lastRow, 9, 1, 5).setNumberFormat("#,##0.00");

    var response = {
      "status": "success",
      "message": "Record synced successfully to Google Sheet.",
      "sheetUrl": ss.getUrl(),
      "row": lastRow
    };

    return ContentService
      .createTextOutput(JSON.stringify(response))
      .setMimeType(ContentService.MimeType.JSON);

  } catch (err) {
    return ContentService
      .createTextOutput(JSON.stringify({
        "status": "error",
        "message": err.toString()
      }))
      .setMimeType(ContentService.MimeType.JSON);
  }
}

/**
 * Custom Function for Google Sheets & Apps Script Payroll Engine:
 * Calculates daily payroll & deductions strictly in accordance with Dubai Labor Laws.
 * Deductions for late arrivals, short hours, or absent days are calculated proportionally
 * based strictly on the employee's Basic Salary (1,000 AED base), excluding allowances.
 * Allowances and overtime calculations remain unchanged.
 */
function CALCULATE_WORKPAY_DAILY(hoursWorked, lateMinutes, monthlyBasicSalary, monthlyAllowance) {
  var basic = monthlyBasicSalary || 1000.0;
  var allowance = monthlyAllowance || 1000.0;
  var daysInMonth = 30;
  var shiftHours = 12.0;

  var dailyBasic = basic / daysInMonth;          // 33.33 AED/day
  var dailyAllowance = allowance / daysInMonth;  // 33.33 AED/day
  var totalDailyBase = dailyBasic + dailyAllowance; // 66.67 AED/day

  // Basic salary rates for deductions strictly under Dubai labor laws
  var basicHourlyRate = dailyBasic / shiftHours;  // ~2.78 AED/hr
  var basicMinuteRate = basicHourlyRate / 60.0;   // ~0.0463 AED/min

  var hourlyRate = totalDailyBase / shiftHours;   // ~5.56 AED/hr
  var otHourlyRate = hourlyRate * 1.5;             // ~8.33 AED/hr

  // Calculate deductions
  var deductions = (lateMinutes || 0) * basicMinuteRate;
  if (hoursWorked < 10.0 && hoursWorked > 0) {
    deductions += (10.0 - hoursWorked) * basicHourlyRate;
  } else if (hoursWorked === 0) {
    deductions = dailyBasic; // Absent day deduction based strictly on basic salary
  }

  // Calculate earnings
  var standardHours = Math.min(hoursWorked, shiftHours);
  var proportion = Math.max(0, Math.min(1, standardHours / shiftHours));
  var basicEarned = dailyBasic * proportion;
  var allowanceEarned = dailyAllowance * proportion;

  var otHours = Math.max(0, hoursWorked - shiftHours);
  var otEarned = otHours * otHourlyRate;

  var netDailyPay = Math.max(0, basicEarned + allowanceEarned + otEarned - deductions);

  return [
    ["Basic Earned", Math.round(basicEarned * 100) / 100],
    ["Allowance Earned", Math.round(allowanceEarned * 100) / 100],
    ["OT Earned", Math.round(otEarned * 100) / 100],
    ["Deductions (Dubai Basic Law)", Math.round(deductions * 100) / 100],
    ["Net Daily Pay", Math.round(netDailyPay * 100) / 100]
  ];
}
""".trimIndent()

    val DEPLOYMENT_INSTRUCTIONS: String = """
1. Open Google Drive (https://drive.google.com).
2. Click '+ New' -> 'More' -> 'Google Apps Script'.
3. Delete any default code in Code.gs, paste the complete code above, and click Save (ctrl + s).
4. Click 'Deploy' -> 'New deployment' in the top right.
5. Select type: 'Web app'.
6. Execute as: 'Me (your email)'.
7. Who has access: 'Anyone'.
8. Click 'Deploy', authorize permissions if requested, and copy the Web App URL.
9. Paste your Web App URL in the Settings inside WorkPay!
""".trimIndent()
}
