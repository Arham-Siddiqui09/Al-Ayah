package com.example.ayahflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ayahflow.theme.AyahFlowTheme
import com.example.ayahflow.ui.AppViewModel
import com.example.ayahflow.ui.design.LocalIsDarkMode
import com.example.ayahflow.ui.main.MainScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.glance.appwidget.updateAll

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as AyahFlowApplication).container

        setContent {
            val appViewModel: AppViewModel = viewModel(
                factory = AppViewModel.Factory(
                    prefsRepository    = container.userPreferencesRepository,
                    bookmarkRepository = container.bookmarkRepository
                )
            )
            val isDark by appViewModel.isDarkMode.collectAsState()

            AyahFlowTheme(darkTheme = isDark) {
                CompositionLocalProvider(LocalIsDarkMode provides isDark) {
                    MainScreen(appViewModel = appViewModel)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        CoroutineScope(Dispatchers.IO).launch {
            com.example.ayahflow.ui.widget.AyahWidget()
                .updateAll(this@MainActivity)
        }
    }
}
