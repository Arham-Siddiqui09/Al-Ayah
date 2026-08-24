package com.example.ayahflow.ui.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.ayahflow.AyahFlowApplication

// ─── Glance State Keys ───────────────────────────────────────────────────────
private val KEY_GLOBAL_INDEX  = intPreferencesKey("global_index")
private val KEY_ARABIC        = stringPreferencesKey("arabic")
private val KEY_TRANSLATION   = stringPreferencesKey("translation")
private val KEY_SURAH_NAME    = stringPreferencesKey("surah_name")
private val KEY_SURAH_NUMBER  = intPreferencesKey("surah_number")
private val KEY_AYAH_NUMBER   = intPreferencesKey("ayah_number")

// ─── Light Palette (matches app card) ───────────────────────────────────────
private val CardColor     = Color(0xFFFFFFFF)
private val DividerColor  = Color(0xFFE5E7EB)
private val ArabicColor   = Color(0xFF1F2937)
private val TransColor    = Color(0xFF4B5563)
private val MetaColor     = Color(0xFF6B7280)
private val ProgressColor = Color(0xFF9CA3AF)
private val BtnBg         = Color(0xFFF3F4F6)
private val BtnBgActive   = Color(0xFF1F2937)
private val BtnTextActive = Color(0xFFFFFFFF)
private val BtnTextDim    = Color(0xFFD1D5DB)

// Helper: load current ayah into Glance state
suspend fun loadAyahIntoState(context: Context, glanceId: GlanceId) {
    val appContainer = (context.applicationContext as AyahFlowApplication).container
    val progress = appContainer.progressRepository.getProgressSync()
    val index = progress?.currentGlobalIndex ?: 1
    val ayah = appContainer.quranRepository.getAyahByGlobalIndex(index)
    if (ayah != null) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            prefs.toMutablePreferences().apply {
                this[KEY_GLOBAL_INDEX] = ayah.globalIndex
                this[KEY_ARABIC]       = ayah.arabicText
                this[KEY_TRANSLATION]  = ayah.translation
                this[KEY_SURAH_NAME]   = ayah.surahName
                this[KEY_SURAH_NUMBER] = ayah.surahNumber
                this[KEY_AYAH_NUMBER]  = ayah.ayahNumber
            }
        }
    }
}

class AyahWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Load fresh data from DB into state on first render
        loadAyahIntoState(context, id)

        provideContent {
            val prefs       = currentState<androidx.datastore.preferences.core.Preferences>()
            val globalIndex = prefs[KEY_GLOBAL_INDEX] ?: 0
            val arabic      = prefs[KEY_ARABIC]       ?: ""
            val translation = prefs[KEY_TRANSLATION]  ?: ""
            val surahName   = prefs[KEY_SURAH_NAME]   ?: ""
            val surahNumber = prefs[KEY_SURAH_NUMBER] ?: 0
            val ayahNumber  = prefs[KEY_AYAH_NUMBER]  ?: 0

            val hasPrev = globalIndex > 1
            val hasNext = globalIndex < 6236
            val hasData = globalIndex > 0 && arabic.isNotEmpty()

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(CardColor)
                    .cornerRadius(20.dp)
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                if (hasData) {
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Surah · Verse
                        Text(
                            text = "$surahName  ·  $surahNumber:$ayahNumber",
                            style = TextStyle(
                                color = ColorProvider(MetaColor),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            ),
                            modifier = GlanceModifier.fillMaxWidth()
                        )

                        Spacer(modifier = GlanceModifier.height(10.dp))

                        // Arabic
                        Text(
                            text = arabic,
                            style = TextStyle(
                                color = ColorProvider(ArabicColor),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            ),
                            modifier = GlanceModifier.fillMaxWidth()
                        )

                        Spacer(modifier = GlanceModifier.height(10.dp))

                        // Divider
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(DividerColor)
                        ) {}

                        Spacer(modifier = GlanceModifier.height(10.dp))

                        // Translation
                        Text(
                            text = translation,
                            style = TextStyle(
                                color = ColorProvider(TransColor),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            ),
                            modifier = GlanceModifier.fillMaxWidth(),
                            maxLines = 4
                        )

                        Spacer(modifier = GlanceModifier.defaultWeight())

                        // Nav Row
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .height(44.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // ← Prev
                            Box(
                                modifier = GlanceModifier
                                    .width(56.dp)
                                    .height(40.dp)
                                    .background(if (hasPrev) BtnBgActive else BtnBg)
                                    .cornerRadius(12.dp)
                                    .let {
                                        if (hasPrev) it.clickable(actionRunCallback<PreviousAyahAction>())
                                        else it
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "←",
                                    style = TextStyle(
                                        color = ColorProvider(if (hasPrev) BtnTextActive else BtnTextDim),
                                        fontSize = 20.sp,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }

                            Spacer(modifier = GlanceModifier.defaultWeight())

                            Text(
                                text = "$globalIndex / 6236",
                                style = TextStyle(
                                    color = ColorProvider(ProgressColor),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            )

                            Spacer(modifier = GlanceModifier.defaultWeight())

                            // → Next
                            Box(
                                modifier = GlanceModifier
                                    .width(56.dp)
                                    .height(40.dp)
                                    .background(if (hasNext) BtnBgActive else BtnBg)
                                    .cornerRadius(12.dp)
                                    .let {
                                        if (hasNext) it.clickable(actionRunCallback<NextAyahAction>())
                                        else it
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "→",
                                    style = TextStyle(
                                        color = ColorProvider(if (hasNext) BtnTextActive else BtnTextDim),
                                        fontSize = 20.sp,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "بِسْمِ ٱللَّهِ",
                            style = TextStyle(
                                color = ColorProvider(ArabicColor),
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(12.dp))
                        Text(
                            text = "Open AyahFlow to begin\nyour Quran journey.",
                            style = TextStyle(
                                color = ColorProvider(TransColor),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        }
    }
}

class NextAyahAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val appContainer = (context.applicationContext as AyahFlowApplication).container
        // Step 1: get current index from Glance state (instant, no DB)
        val manager = GlanceAppWidgetManager(context)
        val currentIndex = try {
            val stateFlow = androidx.glance.appwidget.state.getAppWidgetState(
                context, PreferencesGlanceStateDefinition, glanceId
            )
            stateFlow[KEY_GLOBAL_INDEX] ?: 1
        } catch (e: Exception) { 1 }

        val nextIndex = (currentIndex + 1).coerceAtMost(6236)
        val nextAyah = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            appContainer.quranRepository.getAyahByGlobalIndex(nextIndex)
        } ?: return

        // Step 2: write next ayah into Glance state (instant re-render)
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            prefs.toMutablePreferences().apply {
                this[KEY_GLOBAL_INDEX] = nextAyah.globalIndex
                this[KEY_ARABIC]       = nextAyah.arabicText
                this[KEY_TRANSLATION]  = nextAyah.translation
                this[KEY_SURAH_NAME]   = nextAyah.surahName
                this[KEY_SURAH_NUMBER] = nextAyah.surahNumber
                this[KEY_AYAH_NUMBER]  = nextAyah.ayahNumber
            }
        }
        // Step 3: re-render this widget instance immediately
        AyahWidget().update(context, glanceId)

        // Step 4: persist to Room DB in background (non-blocking)
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            appContainer.progressRepository.updateProgress(nextIndex)
        }
    }
}

class PreviousAyahAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val appContainer = (context.applicationContext as AyahFlowApplication).container

        val currentIndex = try {
            val stateFlow = androidx.glance.appwidget.state.getAppWidgetState(
                context, PreferencesGlanceStateDefinition, glanceId
            )
            stateFlow[KEY_GLOBAL_INDEX] ?: 1
        } catch (e: Exception) { 1 }

        val prevIndex = (currentIndex - 1).coerceAtLeast(1)
        if (prevIndex == currentIndex) return

        val prevAyah = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            appContainer.quranRepository.getAyahByGlobalIndex(prevIndex)
        } ?: return

        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            prefs.toMutablePreferences().apply {
                this[KEY_GLOBAL_INDEX] = prevAyah.globalIndex
                this[KEY_ARABIC]       = prevAyah.arabicText
                this[KEY_TRANSLATION]  = prevAyah.translation
                this[KEY_SURAH_NAME]   = prevAyah.surahName
                this[KEY_SURAH_NUMBER] = prevAyah.surahNumber
                this[KEY_AYAH_NUMBER]  = prevAyah.ayahNumber
            }
        }
        AyahWidget().update(context, glanceId)

        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            appContainer.progressRepository.updateProgress(prevIndex)
        }
    }
}
