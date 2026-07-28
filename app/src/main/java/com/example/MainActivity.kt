package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import com.example.ui.screens.MainDashboardScreen
import com.example.ui.theme.WorkPayTheme
import com.example.ui.viewmodel.AttendanceViewModel

class MainActivity : FragmentActivity() {

    private val attendanceViewModel: AttendanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkPayTheme {
                MainDashboardScreen(viewModel = attendanceViewModel)
            }
        }
    }
}
