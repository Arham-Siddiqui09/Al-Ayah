package com.example.ayahflow.ui.reader

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ayahflow.R
import com.example.ayahflow.data.sync.SyncState
import com.example.ayahflow.ui.AppViewModel
import com.example.ayahflow.ui.components.*
import com.example.ayahflow.ui.design.AyahColors
import com.example.ayahflow.ui.design.LocalIsDarkMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    appViewModel: AppViewModel,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark       = LocalIsDarkMode.current
    val currentAyah  by viewModel.currentAyah.collectAsState()
    val syncState    by viewModel.syncState.collectAsState()
    val syncProgress by viewModel.syncProgress.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val bookmarkedAyahs by viewModel.bookmarkedAyahs.collectAsState()

    val scope = rememberCoroutineScope()
    var showBookmarksSheet by remember { mutableStateOf(false) }

    // Top background color based on the screenshot, but adapting to match Journey screen
    val bg = if (isDark) AyahColors.DarkBg else AyahColors.CreamBg

    BotanicalBackground(
        isDark = isDark,
        opacity = if (isDark) 0.12f else 0.20f,
        modifier = modifier
            .fillMaxSize()
            .background(bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 12.dp)
        ) {
            // ── Top Bar (Verse of the Day) ───────────────────────────────────
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Verse of the Day",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) AyahColors.DarkTextPrimary else AyahColors.GreenPrimary
                )
            }

            Spacer(Modifier.height(16.dp))

            when (syncState) {
                SyncState.NOT_STARTED, SyncState.DOWNLOADING, SyncState.VALIDATING -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(40.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = AyahColors.GreenSecondary)
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = if (syncState == SyncState.VALIDATING) "Validating Quran Data..." else "Downloading Quran...\n($syncProgress / 114 Surahs)",
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            color = if (isDark) AyahColors.DarkTextSecondary else AyahColors.TextSecondary
                        )
                    }
                }
                SyncState.FAILED -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(40.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("\u26A0\uFE0F Download Failed", fontSize = 18.sp, color = Color(0xFFD97706))
                        Spacer(Modifier.height(10.dp))
                        Text(errorMessage ?: "Unknown error", fontSize = 13.sp, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = { viewModel.retrySync() }) { Text("Retry") }
                    }
                }
                SyncState.COMPLETED -> {
                    val ayah = currentAyah
                    if (ayah == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AyahColors.GreenSecondary)
                        }
                    } else {
                        // ── Card Area with Leaves ──────────────────────────────
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            // 1. The torn paper card
                            QuranVerseCard(
                                ayah = ayah,
                                isDark = isDark,
                                modifier = Modifier.fillMaxSize()
                                    .scale(1.15f)
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        // ── Navigation Buttons ─────────────────────────────────
                        VerseNavigation(
                            onPrevious  = { viewModel.previousAyah() },
                            onNext      = { viewModel.nextAyah() },
                            onListClick = { showBookmarksSheet = true },
                            hasPrevious = ayah.globalIndex > 1,
                            hasNext     = ayah.globalIndex < 6236,
                            isDark      = isDark,
                            modifier    = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    if (showBookmarksSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBookmarksSheet = false },
            containerColor = if (isDark) AyahColors.DarkSurface else AyahColors.Surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Liked Ayahs",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) AyahColors.DarkTextPrimary else AyahColors.TextPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (bookmarkedAyahs.isEmpty()) {
                    Text(
                        text = "You haven't liked any Ayahs yet. Tap the heart icon on the widget to save them here!",
                        fontSize = 14.sp,
                        color = if (isDark) AyahColors.DarkTextSecondary else AyahColors.TextSecondary,
                        modifier = Modifier.padding(vertical = 32.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(bookmarkedAyahs) { b ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isDark) AyahColors.DarkCard else Color(0xFFF0F2EB), RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.jumpToAyah(b.globalIndex)
                                        showBookmarksSheet = false
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = null,
                                    tint = AyahColors.GreenPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${b.surahName} ${b.surahNumber}:${b.ayahNumber}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDark) AyahColors.DarkTextPrimary else AyahColors.TextPrimary
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = b.arabicText,
                                        fontSize = 16.sp,
                                        color = if (isDark) AyahColors.DarkTextSecondary else AyahColors.TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
