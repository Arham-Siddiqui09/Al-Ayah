package com.example.ayahflow.ui.reader

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ayahflow.data.model.AyahEntity
import kotlinx.coroutines.launch

@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    modifier: Modifier = Modifier
) {
    val currentAyah by viewModel.currentAyah.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val syncProgress by viewModel.syncProgress.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFFDFBF7) // Ivory paper color
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (syncState) {
                com.example.ayahflow.data.sync.SyncState.NOT_STARTED,
                com.example.ayahflow.data.sync.SyncState.DOWNLOADING,
                com.example.ayahflow.data.sync.SyncState.VALIDATING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(bottom = 24.dp),
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = if (syncState == com.example.ayahflow.data.sync.SyncState.VALIDATING) {
                            "Validating Quran Data..."
                        } else {
                            "Downloading Quran Data... ($syncProgress/114 Surahs)"
                        },
                        fontSize = 18.sp,
                        color = Color(0xFF4B5563),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "This will only happen once.",
                        fontSize = 14.sp,
                        color = Color(0xFF9CA3AF),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                com.example.ayahflow.data.sync.SyncState.FAILED -> {
                    Text(
                        text = "Failed to download Quran.",
                        fontSize = 18.sp,
                        color = Color(0xFFEF4444),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = errorMessage ?: "Unknown error",
                        fontSize = 14.sp,
                        color = Color(0xFF4B5563),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    Button(onClick = { viewModel.retrySync() }) {
                        Text("Retry")
                    }
                }
                com.example.ayahflow.data.sync.SyncState.COMPLETED -> {
                    currentAyah?.let { ayah ->
                        AyahCard(ayah, onNext = { viewModel.nextAyah() }, onPrev = { viewModel.previousAyah() })
                    } ?: CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun AyahCard(
    ayah: AyahEntity,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0x1A000000)
            )
            .clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = ayah.arabicText,
                fontSize = 32.sp,
                lineHeight = 48.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1F2937),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Text(
                text = ayah.translation,
                fontSize = 18.sp,
                lineHeight = 28.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFF4B5563),
                modifier = Modifier.padding(bottom = 40.dp)
            )
            
            Divider(color = Color(0xFFE5E7EB), modifier = Modifier.padding(bottom = 16.dp))

            Text(
                text = "${ayah.surahName} · ${ayah.surahNumber}:${ayah.ayahNumber}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "${ayah.globalIndex} / 6236",
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF)
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onPrev) {
                    Text("Previous")
                }
                TextButton(onClick = onNext) {
                    Text("Next")
                }
            }
        }
    }
}
