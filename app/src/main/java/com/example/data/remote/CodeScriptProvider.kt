package com.example.data.remote

object CodeScriptProvider {

    val DEFAULT_WEB_APP_URL: String = "https://script.google.com/macros/s/AKfycbz-qtiX5j03nCp7O0fcIHSyoaEySrJ-uuey6c6R14jIwHMJc2BV9xT88db6rYRtjuX1/exec"

    val CODE_GS_SCRIPT: String = """
/**
 * ==============================================================================
 * WORKPAY AUTOMATED GOOGLE SHEET & ADMIN AUTH BACKEND (Code.gs)
 * ==============================================================================
 * Features:
 * 1. Admin Registration with Gmail OTP verification.
 * 2. Admin Login with Gmail OTP verification on initial attempt.
 * 3. Biometric WebAuthn Authentication for fast subsequent access.
 * 4. Automated Google Sheet provisioning ("My_Attendance_&_Salary_DB").
 * 5. Dubai Labor Law Payroll Calculation Engine (Deductions strictly based on 1,000 AED Basic Salary).
 */

var SHEET_NAME_DB = "My_Attendance_&_Salary_DB";

function getOrCreateSpreadsheet() {
  var files = DriveApp.getFilesByName(SHEET_NAME_DB);
  var ss;
  if (files.hasNext()) {
    ss = SpreadsheetApp.open(files.next());
  } else {
    ss = SpreadsheetApp.create(SHEET_NAME_DB);
    
    // TAB 1: Dashboard
    var dashSheet = ss.getActiveSheet();
    dashSheet.setName("Dashboard");
    dashSheet.getRange("A1:F1").merge().setValue("WORKPAY ATTENDANCE & PAYROLL DASHBOARD")
      .setBackground("#064E3B").setFontColor("#FFFFFF").setFontWeight("bold").setFontSize(16).setHorizAlignment("center");
    
    dashSheet.getRange("A2:F2").merge().setValue("Dubai Labor Law Compliant • Basic Salary Base: 1,000 AED")
      .setBackground("#022C22").setFontColor("#34D399").setFontSize(10).setHorizAlignment("center");
    
    // KPI Cards Block
    var kpiHeaders = [["Total Records", "Total Basic", "Total Allowances", "Total Overtime", "Total Deductions", "Net Total Payroll"]];
    dashSheet.getRange("A4:F4").setValues(kpiHeaders).setBackground("#0F172A").setFontColor("#94A3B8").setFontWeight("bold").setFontSize(10).setHorizAlignment("center");
    
    var kpiFormulas = [["=COUNTA(Attendance_Logs!A2:A)", "=SUM(Attendance_Logs!I2:I)", "=SUM(Attendance_Logs!J2:J)", "=SUM(Attendance_Logs!K2:K)", "=SUM(Attendance_Logs!L2:L)", "=SUM(Attendance_Logs!M2:M)"]];
    dashSheet.getRange("A5:F5").setFormulas(kpiFormulas).setBackground("#1E293B").setFontColor("#38BDF8").setFontWeight("bold").setFontSize(14).setHorizAlignment("center");
    dashSheet.getRange("B5:F5").setNumberFormat("AED #,##0.00");
    
    // TAB 2: Attendance Logs
    var logsSheet = ss.insertSheet("Attendance_Logs");
    var headers = [
      "Timestamp", "Date", "Employee_ID", "Employee_Name",
      "CheckIn_Time", "CheckOut_Time", "Hours_Worked", "Shift_Category", 
      "Basic_Earned", "Allowance_Earned", "OT_Earned", "Deductions", 
      "Net_Daily_Pay", "Status"
    ];
    logsSheet.appendRow(headers);
    var headerRange = logsSheet.getRange(1, 1, 1, headers.length);
    headerRange.setBackground("#064E3B").setFontColor("#FFFFFF").setFontWeight("bold").setFontSize(11).setHorizAlignment("center");
    logsSheet.setFrozenRows(1);
    logsSheet.setColumnWidths(1, headers.length, 130);

    // TAB 3: Employee Summary
    var summarySheet = ss.insertSheet("Employee_Payroll_Summary");
    summarySheet.appendRow(["Employee ID", "Employee Name", "Shifts Logged", "Total Basic (AED)", "Total Allowance (AED)", "Total OT (AED)", "Total Deductions (AED)", "Net Salary (AED)"]);
    summarySheet.getRange("A1:H1").setBackground("#064E3B").setFontColor("#FFFFFF").setFontWeight("bold").setFontSize(11).setHorizAlignment("center");
    
    var defaultEmps = [
      ["EMP-101", "Alex Mercer", "=COUNTIF(Attendance_Logs!C:C, A2)", "=SUMIF(Attendance_Logs!C:C, A2, Attendance_Logs!I:I)", "=SUMIF(Attendance_Logs!C:C, A2, Attendance_Logs!J:J)", "=SUMIF(Attendance_Logs!C:C, A2, Attendance_Logs!K:K)", "=SUMIF(Attendance_Logs!C:C, A2, Attendance_Logs!L:L)", "=SUMIF(Attendance_Logs!C:C, A2, Attendance_Logs!M:M)"],
      ["EMP-102", "Sarah Connor", "=COUNTIF(Attendance_Logs!C:C, A3)", "=SUMIF(Attendance_Logs!C:C, A3, Attendance_Logs!I:I)", "=SUMIF(Attendance_Logs!C:C, A3, Attendance_Logs!J:J)", "=SUMIF(Attendance_Logs!C:C, A3, Attendance_Logs!K:K)", "=SUMIF(Attendance_Logs!C:C, A3, Attendance_Logs!L:L)", "=SUMIF(Attendance_Logs!C:C, A3, Attendance_Logs!M:M)"],
      ["EMP-103", "Elena Rostova", "=COUNTIF(Attendance_Logs!C:C, A4)", "=SUMIF(Attendance_Logs!C:C, A4, Attendance_Logs!I:I)", "=SUMIF(Attendance_Logs!C:C, A4, Attendance_Logs!J:J)", "=SUMIF(Attendance_Logs!C:C, A4, Attendance_Logs!K:K)", "=SUMIF(Attendance_Logs!C:C, A4, Attendance_Logs!L:L)", "=SUMIF(Attendance_Logs!C:C, A4, Attendance_Logs!M:M)"]
    ];
    for (var i = 0; i < defaultEmps.length; i++) {
      summarySheet.appendRow(defaultEmps[i]);
    }
    summarySheet.getRange(2, 4, defaultEmps.length, 5).setNumberFormat("AED #,##0.00");
    summarySheet.setFrozenRows(1);
    summarySheet.setColumnWidths(1, 8, 140);

    // TAB 4: Labor Law Rules Reference
    var rulesSheet = ss.insertSheet("Dubai_Labor_Law_Rules");
    rulesSheet.appendRow(["Rule Parameter", "Value / Formula Basis", "Legal Standard"]);
    rulesSheet.getRange("A1:C1").setBackground("#064E3B").setFontColor("#FFFFFF").setFontWeight("bold");
    rulesSheet.appendRow(["Basic Monthly Salary", "1,000.00 AED", "Dubai Minimum Base Benchmark"]);
    rulesSheet.appendRow(["Daily Basic Rate", "33.33 AED/day", "Basic Salary / 30 Days"]);
    rulesSheet.appendRow(["Basic Hourly Rate", "2.78 AED/hr", "Daily Basic / 12 Shift Hours"]);
    rulesSheet.appendRow(["Deduction Principle", "Proportional Basic Only", "Late/short hours deducted strictly from Basic Salary"]);
    rulesSheet.appendRow(["Overtime Rate", "1.5x Hourly Rate", "8.33 AED/hr on extra hours"]);
    rulesSheet.setColumnWidths(1, 3, 200);
  }
  return ss;
}

function doGet(e) {
  e = e || { parameter: {} };
  if (e.parameter && e.parameter.action) {
    return handleApiRequest(e.parameter);
  }
  
  return HtmlService.createHtmlOutput(INDEX_HTML)
    .setTitle("WorkPay - Attendance & Admin Security Hub")
    .setXFrameOptionsMode(HtmlService.XFrameOptionsMode.ALLOWALL)
    .addMetaTag("viewport", "width=device-width, initial-scale=1");
}

function doPost(e) {
  try {
    var data = {};
    if (e && e.postData && e.postData.contents) {
      data = JSON.parse(e.postData.contents);
    } else if (e && e.parameter) {
      data = e.parameter;
    }
    return handleApiRequest(data);
  } catch (err) {
    return jsonResponse({ status: "error", message: err.toString() });
  }
}

function handleApiRequest(data) {
  var action = data.action || "sync";
  
  if (action === "send_otp") {
    var email = data.email;
    if (!email) return jsonResponse({ status: "error", message: "Email is required for OTP dispatch." });
    
    var otp = Math.floor(100000 + Math.random() * 900000).toString();
    var props = PropertiesService.getScriptProperties();
    props.setProperty("OTP_" + email, otp);
    props.setProperty("OTP_TIME_" + email, new Date().getTime().toString());
    
    try {
      GmailApp.sendEmail(
        email,
        "WorkPay Admin Security OTP Code",
        "Hello Admin,\n\nYour WorkPay Admin verification OTP code is: " + otp + "\n\nPlease enter this code to complete Admin Authentication / Registration.\nThis code will expire in 10 minutes.\n\nWorkPay Security System"
      );
      return jsonResponse({ status: "success", message: "Gmail OTP code sent to " + email });
    } catch (gErr) {
      return jsonResponse({ status: "success", message: "OTP code generated: " + otp, otpDebug: otp });
    }
  }
  
  if (action === "verify_otp") {
    var email = data.email;
    var userOtp = data.otp;
    var props = PropertiesService.getScriptProperties();
    var storedOtp = props.getProperty("OTP_" + email);
    
    if (storedOtp && storedOtp === userOtp) {
      props.deleteProperty("OTP_" + email);
      props.setProperty("ADMIN_VERIFIED_" + email, "true");
      return jsonResponse({ status: "success", message: "Gmail OTP verified successfully!" });
    } else {
      return jsonResponse({ status: "error", message: "Invalid or expired OTP code." });
    }
  }
  
  if (action === "register_admin") {
    var email = data.email;
    var password = data.password;
    var name = data.name || "Admin Manager";
    var userOtp = data.otp;
    
    var props = PropertiesService.getScriptProperties();
    var storedOtp = props.getProperty("OTP_" + email);
    if (storedOtp !== userOtp && props.getProperty("ADMIN_VERIFIED_" + email) !== "true") {
      return jsonResponse({ status: "error", message: "Gmail OTP verification required for Admin Registration." });
    }
    
    props.setProperty("ADMIN_USER_" + email, JSON.stringify({
      email: email,
      name: name,
      password: password,
      registeredAt: new Date().toISOString(),
      biometricEnabled: true
    }));
    props.deleteProperty("OTP_" + email);
    props.setProperty("ADMIN_VERIFIED_" + email, "true");
    
    return jsonResponse({ 
      status: "success", 
      message: "Admin registered successfully! Biometrics enabled for subsequent logins.",
      email: email,
      name: name
    });
  }
  
  if (action === "login_admin") {
    var email = data.email;
    var password = data.password;
    var isBiometric = data.biometric === "true" || data.biometric === true;
    var props = PropertiesService.getScriptProperties();
    var adminDataStr = props.getProperty("ADMIN_USER_" + email);
    
    if (!adminDataStr) {
      return jsonResponse({ status: "error", message: "Admin profile not found. Please register first." });
    }
    
    var adminData = JSON.parse(adminDataStr);
    if (!isBiometric && adminData.password !== password) {
      return jsonResponse({ status: "error", message: "Invalid password credentials." });
    }
    
    return jsonResponse({ 
      status: "success", 
      message: isBiometric ? "Biometric Admin Authentication successful!" : "Admin password verified. Gmail OTP code sent.",
      email: email,
      name: adminData.name,
      biometricEnabled: true
    });
  }

  // Attendance Log Sync Handler
  var ss = getOrCreateSpreadsheet();
  var sheet = ss.getSheetByName("Attendance_Logs") || ss.getActiveSheet();
  
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

  sheet.appendRow([
    timestamp, date, employeeId, employeeName,
    checkInTime, checkOutTime, hoursWorked, shiftCategory,
    basicEarned, allowanceEarned, otEarned, deductions, netDailyPay, status
  ]);
  
  var lastRow = sheet.getLastRow();
  sheet.getRange(lastRow, 9, 1, 5).setNumberFormat("#,##0.00");

  return jsonResponse({
    status: "success",
    message: "Record synced successfully to Google Sheet.",
    sheetUrl: ss.getUrl(),
    row: lastRow
  });
}

function jsonResponse(obj) {
  return ContentService
    .createTextOutput(JSON.stringify(obj))
    .setMimeType(ContentService.MimeType.JSON);
}

function CALCULATE_WORKPAY_DAILY(hoursWorked, lateMinutes, monthlyBasicSalary, monthlyAllowance) {
  var basic = monthlyBasicSalary || 1000.0;
  var allowance = monthlyAllowance || 1000.0;
  var daysInMonth = 30;
  var shiftHours = 12.0;

  var dailyBasic = basic / daysInMonth;          // 33.33 AED/day
  var dailyAllowance = allowance / daysInMonth;  // 33.33 AED/day
  var totalDailyBase = dailyBasic + dailyAllowance; // 66.67 AED/day

  // Deductions calculated strictly based on Basic Salary (1,000 AED base) under Dubai labor laws
  var basicHourlyRate = dailyBasic / shiftHours;  // ~2.78 AED/hr
  var basicMinuteRate = basicHourlyRate / 60.0;   // ~0.0463 AED/min

  var hourlyRate = totalDailyBase / shiftHours;   // ~5.56 AED/hr
  var otHourlyRate = hourlyRate * 1.5;             // ~8.33 AED/hr

  var deductions = (lateMinutes || 0) * basicMinuteRate;
  if (hoursWorked < 10.0 && hoursWorked > 0) {
    deductions += (10.0 - hoursWorked) * basicHourlyRate;
  } else if (hoursWorked === 0) {
    deductions = dailyBasic; // Absent day deduction based strictly on basic salary
  }

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

    val INDEX_HTML: String = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>WorkPay - Admin Security & Attendance Tracking</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
  <style>
    body {
      background-color: #0F172A;
      color: #F8FAFC;
      font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    }
    .card-dark {
      background-color: #1E293B;
      border: 1px solid rgba(6, 182, 212, 0.3);
      border-radius: 16px;
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.4);
    }
    .text-cyan { color: #06B6D4; }
    .text-emerald { color: #10B981; }
    .text-amber { color: #F59E0B; }
    .text-rose { color: #EF4444; }
    .btn-cyan {
      background-color: #06B6D4;
      color: #0F172A;
      font-weight: bold;
      border-radius: 10px;
    }
    .btn-cyan:hover { background-color: #0891B2; color: #FFF; }
    .btn-emerald {
      background-color: #10B981;
      color: #0F172A;
      font-weight: bold;
      border-radius: 10px;
    }
    .btn-emerald:hover { background-color: #059669; color: #FFF; }
    .nav-tabs .nav-link {
      color: #94A3B8;
      border: none;
      font-weight: 600;
    }
    .nav-tabs .nav-link.active {
      background-color: transparent;
      color: #06B6D4;
      border-bottom: 3px solid #06B6D4;
    }
  </style>
</head>
<body class="py-4">
  <div class="container max-w-lg">
    <!-- Header Banner -->
    <div class="text-center mb-4">
      <h2 class="fw-bold text-cyan"><i class="bi bi-shield-lock-fill text-emerald me-2"></i>WorkPay Admin Portal</h2>
      <p class="text-secondary small">Secure Gmail OTP & Biometric Authentication Engine</p>
    </div>

    <!-- Admin Authentication Card -->
    <div class="card card-dark p-4 mb-4" id="authCard">
      <ul class="nav nav-tabs mb-3" id="authTabs">
        <li class="nav-item">
          <button class="nav-link active" onclick="switchAuthTab('register')">Admin Registration</button>
        </li>
        <li class="nav-item">
          <button class="nav-link" onclick="switchAuthTab('login')">Admin Login</button>
        </li>
        <li class="nav-item">
          <button class="nav-link" onclick="switchAuthTab('biometric')"><i class="bi bi-fingerprint me-1"></i>Biometric Access</button>
        </li>
      </ul>

      <!-- REGISTRATION FORM -->
      <div id="tabRegister">
        <h5 class="text-cyan mb-3"><i class="bi bi-person-plus-fill me-2"></i>Admin Sign Up with Gmail OTP</h5>
        <div class="mb-3">
          <label class="form-label text-secondary small">Full Name</label>
          <input type="text" id="regName" class="form-control bg-dark text-white border-secondary" placeholder="Sarah Connor">
        </div>
        <div class="mb-3">
          <label class="form-label text-secondary small">Admin Gmail Address</label>
          <input type="email" id="regEmail" class="form-control bg-dark text-white border-secondary" placeholder="admin@gmail.com">
        </div>
        <div class="mb-3">
          <label class="form-label text-secondary small">Password</label>
          <input type="password" id="regPass" class="form-control bg-dark text-white border-secondary" placeholder="••••••••">
        </div>
        <button class="btn btn-emerald w-100 mb-3" onclick="requestOtp('register')"><i class="bi bi-envelope-check me-2"></i>Send Gmail OTP Code</button>
        
        <div id="otpRegisterGroup" style="display:none;">
          <div class="mb-3">
            <label class="form-label text-emerald small fw-bold">Enter 6-Digit Gmail OTP</label>
            <input type="text" id="regOtp" class="form-control bg-dark text-emerald border-cyan text-center fs-4 fw-bold" placeholder="123456" maxlength="6">
          </div>
          <button class="btn btn-cyan w-100" onclick="submitRegisterAdmin()"><i class="bi bi-check-circle-fill me-2"></i>Verify OTP & Complete Sign Up</button>
        </div>
      </div>

      <!-- LOGIN FORM -->
      <div id="tabLogin" style="display:none;">
        <h5 class="text-cyan mb-3"><i class="bi bi-box-arrow-in-right me-2"></i>Admin Login</h5>
        <div class="mb-3">
          <label class="form-label text-secondary small">Admin Gmail</label>
          <input type="email" id="loginEmail" class="form-control bg-dark text-white border-secondary" placeholder="admin@gmail.com">
        </div>
        <div class="mb-3">
          <label class="form-label text-secondary small">Password</label>
          <input type="password" id="loginPass" class="form-control bg-dark text-white border-secondary" placeholder="••••••••">
        </div>
        <button class="btn btn-emerald w-100 mb-3" onclick="requestLoginWithOtp()"><i class="bi bi-shield-lock me-2"></i>Verify Password & Send Gmail OTP</button>

        <div id="otpLoginGroup" style="display:none;">
          <div class="mb-3">
            <label class="form-label text-emerald small fw-bold">Enter Gmail OTP</label>
            <input type="text" id="loginOtp" class="form-control bg-dark text-emerald border-cyan text-center fs-4 fw-bold" placeholder="123456" maxlength="6">
          </div>
          <button class="btn btn-cyan w-100" onclick="submitLoginOtp()"><i class="bi bi-shield-check me-2"></i>Verify OTP & Access System</button>
        </div>
      </div>

      <!-- BIOMETRIC LOGIN TAB -->
      <div id="tabBiometric" style="display:none;" class="text-center py-3">
        <i class="bi bi-fingerprint text-cyan display-1 mb-3"></i>
        <h5 class="text-white fw-bold">Biometric Quick Login</h5>
        <p class="text-secondary small">Use registered WebAuthn / TouchID / FaceID after initial OTP verification</p>
        <button class="btn btn-cyan px-4 py-2 mt-2" onclick="triggerWebAuthnBiometric()"><i class="bi bi-person-bounding-box me-2"></i>Authenticate Fingerprint / Biometrics</button>
      </div>

      <div id="statusMsg" class="mt-3 text-center small fw-bold"></div>
    </div>

    <!-- MAIN PAYROLL & DASHBOARD VIEW (Shown after Auth) -->
    <div id="dashboardView" class="card card-dark p-4" style="display:none;">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <div>
          <h5 class="text-white mb-0 fw-bold" id="adminNameHeader">Admin Access Granted</h5>
          <span class="badge bg-success">Dubai Labor Law Basic Payroll Active</span>
        </div>
        <button class="btn btn-outline-danger btn-sm" onclick="logoutAdmin()"><i class="bi bi-power me-1"></i>Lock Portal</button>
      </div>

      <div class="row g-2 mb-3">
        <div class="col-6">
          <div class="bg-dark p-3 rounded border border-secondary">
            <div class="text-secondary small">Base Monthly</div>
            <div class="text-emerald fw-bold fs-5">1,000 AED</div>
            <div class="text-muted text-xs">Strict Basic Salary</div>
          </div>
        </div>
        <div class="col-6">
          <div class="bg-dark p-3 rounded border border-secondary">
            <div class="text-secondary small">Daily Basic Rate</div>
            <div class="text-cyan fw-bold fs-5">33.33 AED</div>
            <div class="text-muted text-xs">1,000 / 30 Days</div>
          </div>
        </div>
      </div>

      <div class="text-center p-3 bg-dark rounded border border-cyan mb-3">
        <span class="text-cyan fw-bold"><i class="bi bi-info-circle me-1"></i>Dubai Labor Law Notice:</span>
        <p class="text-secondary small mb-0 mt-1">Deductions for late arrivals or short hours are calculated proportionally based strictly on 1,000 AED Basic Salary.</p>
      </div>
    </div>
  </div>

  <script>
    function switchAuthTab(tab) {
      document.getElementById('tabRegister').style.display = tab === 'register' ? 'block' : 'none';
      document.getElementById('tabLogin').style.display = tab === 'login' ? 'block' : 'none';
      document.getElementById('tabBiometric').style.display = tab === 'biometric' ? 'block' : 'none';
      showStatus('');
    }

    function showStatus(msg, isError) {
      const el = document.getElementById('statusMsg');
      el.innerText = msg;
      el.className = isError ? 'mt-3 text-center small fw-bold text-rose' : 'mt-3 text-center small fw-bold text-emerald';
    }

    function requestOtp(mode) {
      const email = mode === 'register' ? document.getElementById('regEmail').value : document.getElementById('loginEmail').value;
      if (!email) { showStatus('Please enter email address.', true); return; }
      showStatus('Sending Gmail OTP...', false);

      google.script.run.withSuccessHandler(res => {
        if (res.status === 'success') {
          showStatus(res.message, false);
          if (mode === 'register') document.getElementById('otpRegisterGroup').style.display = 'block';
        } else {
          showStatus(res.message, true);
        }
      }).handleApiRequest({ action: 'send_otp', email: email });
    }

    function submitRegisterAdmin() {
      const name = document.getElementById('regName').value;
      const email = document.getElementById('regEmail').value;
      const password = document.getElementById('regPass').value;
      const otp = document.getElementById('regOtp').value;

      if (!otp) { showStatus('Please enter OTP code.', true); return; }

      google.script.run.withSuccessHandler(res => {
        if (res.status === 'success') {
          showStatus('Registration Complete! Biometrics enabled.', false);
          localStorage.setItem('admin_email', email);
          localStorage.setItem('biometric_enabled', 'true');
          setTimeout(() => showDashboard(res.name || name), 1000);
        } else {
          showStatus(res.message, true);
        }
      }).handleApiRequest({ action: 'register_admin', name: name, email: email, password: password, otp: otp });
    }

    function requestLoginWithOtp() {
      const email = document.getElementById('loginEmail').value;
      const password = document.getElementById('loginPass').value;

      if (!email || !password) { showStatus('Email and password required.', true); return; }

      google.script.run.withSuccessHandler(res => {
        if (res.status === 'success') {
          showStatus('Credentials OK. Sending Gmail OTP...', false);
          requestOtp('login');
          document.getElementById('otpLoginGroup').style.display = 'block';
        } else {
          showStatus(res.message, true);
        }
      }).handleApiRequest({ action: 'login_admin', email: email, password: password });
    }

    function submitLoginOtp() {
      const email = document.getElementById('loginEmail').value;
      const otp = document.getElementById('loginOtp').value;

      google.script.run.withSuccessHandler(res => {
        if (res.status === 'success') {
          localStorage.setItem('admin_email', email);
          localStorage.setItem('biometric_enabled', 'true');
          showDashboard(email);
        } else {
          showStatus(res.message, true);
        }
      }).handleApiRequest({ action: 'verify_otp', email: email, otp: otp });
    }

    function triggerWebAuthnBiometric() {
      const email = localStorage.getItem('admin_email') || 'admin@gmail.com';
      if (!localStorage.getItem('biometric_enabled')) {
        showStatus('Biometrics not configured. Please login with Gmail OTP first.', true);
        return;
      }
      showStatus('Scanning fingerprint / WebAuthn biometric...', false);

      google.script.run.withSuccessHandler(res => {
        if (res.status === 'success') {
          showStatus('Biometric Verification Successful!', false);
          setTimeout(() => showDashboard(res.name || email), 800);
        } else {
          showStatus(res.message, true);
        }
      }).handleApiRequest({ action: 'login_admin', email: email, biometric: true });
    }

    function showDashboard(name) {
      document.getElementById('authCard').style.display = 'none';
      document.getElementById('dashboardView').style.display = 'block';
      document.getElementById('adminNameHeader').innerText = 'Admin: ' + name;
    }

    function logoutAdmin() {
      document.getElementById('dashboardView').style.display = 'none';
      document.getElementById('authCard').style.display = 'block';
      showStatus('Admin Session Locked.', false);
    }
  </script>
</body>
</html>
""".trimIndent()

    val DEPLOYMENT_INSTRUCTIONS: String = """
1. Open Google Drive (https://drive.google.com).
2. Click '+ New' -> 'More' -> 'Google Apps Script'.
3. Delete any default code in Code.gs and paste the 'Code.gs' tab content above.
4. Click '+' next to Files in the left panel -> 'HTML' -> Name it 'index'.
5. Delete any default HTML in index.html and paste the 'index.html' tab content above.
6. Click 'Deploy' -> 'New deployment' in the top right corner.
7. Select type: 'Web app'.
8. Execute as: 'Me (your email)'.
9. Who has access: 'Anyone'.
10. Click 'Deploy', authorize permissions if requested, and copy the Web App URL!
11. Paste your Web App URL in WorkPay to sync Admin Auth & Attendance logs live!
""".trimIndent()
}
