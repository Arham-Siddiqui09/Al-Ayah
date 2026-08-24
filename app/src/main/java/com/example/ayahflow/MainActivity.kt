package com.example.ayahflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ayahflow.ui.reader.ReaderScreen
import com.example.ayahflow.ui.reader.ReaderViewModel
import com.example.ayahflow.theme.AyahFlowTheme
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val appContainer = (application as AyahFlowApplication).container
        
        setContent {
            AyahFlowTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val readerViewModel: ReaderViewModel = viewModel(
                        factory = ReaderViewModel.provideFactory(
                            appContainer.getAyahUseCase,
                            appContainer.getProgressUseCase,
                            appContainer.updateProgressUseCase,
                            appContainer.syncManager
                        )
                    )
                    ReaderScreen(
                        viewModel = readerViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            com.example.ayahflow.ui.widget.AyahWidget().updateAll(this@MainActivity)
        }
    }
}
