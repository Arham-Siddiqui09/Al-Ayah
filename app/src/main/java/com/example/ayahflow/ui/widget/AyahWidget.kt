package com.example.ayahflow.ui.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.color.ColorProvider
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
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.example.ayahflow.AyahFlowApplication
import com.example.ayahflow.R

private val KEY_GLOBAL_INDEX  = intPreferencesKey("global_index")
private val KEY_ARABIC        = stringPreferencesKey("arabic")
private val KEY_TRANSLATION   = stringPreferencesKey("translation")
private val KEY_SURAH_NAME    = stringPreferencesKey("surah_name")
private val KEY_SURAH_NUMBER  = intPreferencesKey("surah_number")
private val KEY_AYAH_NUMBER   = intPreferencesKey("ayah_number")
private val KEY_IS_BOOKMARKED = booleanPreferencesKey("is_bookmarked")

// Colors to match Reader Screen (Light mode)
private val ArabicColor   = androidx.glance.unit.ColorProvider(Color(0xFF202936))
private val TransColor    = androidx.glance.unit.ColorProvider(Color(0xFF687078))
private val MetaColor     = androidx.glance.unit.ColorProvider(Color(0xFF9BA3AB))
private val BtnBg         = androidx.glance.unit.ColorProvider(Color(0xFF164E3D))
private val BtnBgDisabled = androidx.glance.unit.ColorProvider(Color(0xFF8BA79E))

suspend fun loadAyahIntoState(context: Context, glanceId: GlanceId) {
    val appContainer = (context.applicationContext as AyahFlowApplication).container
    val progress = appContainer.progressRepository.getProgressSync()
    val index = progress?.currentGlobalIndex ?: 1
    val ayah = appContainer.quranRepository.getAyahByGlobalIndex(index)
    val isBookmarked = appContainer.bookmarkRepository.getAllBookmarks() // we can't easily collect flow here...
    // wait, we can just do a sync query for bookmark!
    val exists = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val dao = com.example.ayahflow.data.local.QuranDatabase.getDatabase(context).bookmarkDao()
        dao.isBookmarkedSync(index)
    }

    if (ayah != null) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            prefs.toMutablePreferences().apply {
                this[KEY_GLOBAL_INDEX] = ayah.globalIndex
                this[KEY_ARABIC]       = ayah.arabicText
                this[KEY_TRANSLATION]  = ayah.translation
                this[KEY_SURAH_NAME]   = ayah.surahName
                this[KEY_SURAH_NUMBER] = ayah.surahNumber
                this[KEY_AYAH_NUMBER]  = ayah.ayahNumber
                this[KEY_IS_BOOKMARKED] = exists
            }
        }
    }
}

class AyahWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        loadAyahIntoState(context, id)

        provideContent {
            val prefs       = currentState<androidx.datastore.preferences.core.Preferences>()
            val globalIndex = prefs[KEY_GLOBAL_INDEX] ?: 0
            val arabic      = prefs[KEY_ARABIC]       ?: ""
            val translation = prefs[KEY_TRANSLATION]  ?: ""
            val surahName   = prefs[KEY_SURAH_NAME]   ?: ""
            val surahNumber = prefs[KEY_SURAH_NUMBER] ?: 0
            val ayahNumber  = prefs[KEY_AYAH_NUMBER]  ?: 0
            val isBookmarked = prefs[KEY_IS_BOOKMARKED] ?: false

            val hasPrev = globalIndex > 1
            val hasNext = globalIndex < 6236
            val hasData = globalIndex > 0 && arabic.isNotEmpty()

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ImageProvider(R.drawable.torn_paper))
                    .padding(horizontal = 29.dp, vertical = 88.dp),
                contentAlignment = Alignment.Center
            ) {
                if (hasData) {
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LazyColumn(
                            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            item { Spacer(modifier = GlanceModifier.height(16.dp)) }
                            
                            item {
                                Text(
                                    text = arabic,
                                    style = TextStyle(
                                        color = ArabicColor,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    ),
                                    modifier = GlanceModifier.fillMaxWidth()
                                )
                            }
                            
                            item { Spacer(modifier = GlanceModifier.height(12.dp)) }
                            
                            item {
                                Text(
                                    text = "$surahName · $surahNumber:$ayahNumber",
                                    style = TextStyle(
                                        color = MetaColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    ),
                                    modifier = GlanceModifier.fillMaxWidth()
                                )
                            }
                            
                            item { Spacer(modifier = GlanceModifier.height(12.dp)) }
                            
                            item {
                                Text(
                                    text = translation,
                                    style = TextStyle(
                                        color = TransColor,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    ),
                                    modifier = GlanceModifier.fillMaxWidth()
                                )
                            }
                            
                            item { Spacer(modifier = GlanceModifier.height(16.dp)) }
                        }

                        // Fixed Nav Buttons at the bottom
                        Row(
                            modifier = GlanceModifier.fillMaxWidth().padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // ← Prev (Eco)
                            Box(
                                modifier = GlanceModifier
                                    .size(56.dp)
                                    .background(if (hasPrev) BtnBg else BtnBgDisabled)
                                    .cornerRadius(28.dp)
                                    .let {
                                        if (hasPrev) it.clickable(actionRunCallback<PreviousAyahAction>())
                                        else it
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_eco),
                                    contentDescription = "Previous",
                                    modifier = GlanceModifier.size(28.dp),
                                    colorFilter = androidx.glance.ColorFilter.tint(androidx.glance.unit.ColorProvider(Color.White))
                                )
                            }

                            Spacer(modifier = GlanceModifier.defaultWeight())
                            
                            // Center (Bookmark / Favorite)
                            Box(
                                modifier = GlanceModifier
                                    .size(56.dp)
                                    .background(BtnBg)
                                    .cornerRadius(28.dp)
                                    .clickable(actionRunCallback<ToggleBookmarkAction>()),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(if (isBookmarked) R.drawable.ic_favorite else R.drawable.ic_favorite_border),
                                    contentDescription = "Like",
                                    modifier = GlanceModifier.size(28.dp),
                                    colorFilter = androidx.glance.ColorFilter.tint(androidx.glance.unit.ColorProvider(Color.White))
                                )
                            }
                            
                            Spacer(modifier = GlanceModifier.defaultWeight())

                            // → Next (Redo)
                            Box(
                                modifier = GlanceModifier
                                    .size(56.dp)
                                    .background(if (hasNext) BtnBg else BtnBgDisabled)
                                    .cornerRadius(28.dp)
                                    .let {
                                        if (hasNext) it.clickable(actionRunCallback<NextAyahAction>())
                                        else it
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_redo),
                                    contentDescription = "Next",
                                    modifier = GlanceModifier.size(28.dp),
                                    colorFilter = androidx.glance.ColorFilter.tint(androidx.glance.unit.ColorProvider(Color.White))
                                )
                            }
                        }
                    }
                } else {
                    Text("Loading Ayah...", style = TextStyle(color = ArabicColor, fontSize = 16.sp))
                }
            }
        }
    }
}

class ToggleBookmarkAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val appContainer = (context.applicationContext as AyahFlowApplication).container
        
        val currentIndex = try {
            val stateFlow = androidx.glance.appwidget.state.getAppWidgetState(
                context, PreferencesGlanceStateDefinition, glanceId
            )
            stateFlow[KEY_GLOBAL_INDEX] ?: 1
        } catch (e: Exception) { 1 }

        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            appContainer.bookmarkRepository.toggleBookmark(currentIndex)
        }
        
        val isBookmarkedNow = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val dao = com.example.ayahflow.data.local.QuranDatabase.getDatabase(context).bookmarkDao()
            dao.isBookmarkedSync(currentIndex)
        }

        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            prefs.toMutablePreferences().apply {
                this[KEY_IS_BOOKMARKED] = isBookmarkedNow
            }
        }
        AyahWidget().update(context, glanceId)
    }
}

class NextAyahAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val appContainer = (context.applicationContext as AyahFlowApplication).container
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
        
        val exists = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val dao = com.example.ayahflow.data.local.QuranDatabase.getDatabase(context).bookmarkDao()
            dao.isBookmarkedSync(nextIndex)
        }

        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            prefs.toMutablePreferences().apply {
                this[KEY_GLOBAL_INDEX] = nextAyah.globalIndex
                this[KEY_ARABIC]       = nextAyah.arabicText
                this[KEY_TRANSLATION]  = nextAyah.translation
                this[KEY_SURAH_NAME]   = nextAyah.surahName
                this[KEY_SURAH_NUMBER] = nextAyah.surahNumber
                this[KEY_AYAH_NUMBER]  = nextAyah.ayahNumber
                this[KEY_IS_BOOKMARKED] = exists
            }
        }
        AyahWidget().update(context, glanceId)

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
        
        val exists = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val dao = com.example.ayahflow.data.local.QuranDatabase.getDatabase(context).bookmarkDao()
            dao.isBookmarkedSync(prevIndex)
        }

        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            prefs.toMutablePreferences().apply {
                this[KEY_GLOBAL_INDEX] = prevAyah.globalIndex
                this[KEY_ARABIC]       = prevAyah.arabicText
                this[KEY_TRANSLATION]  = prevAyah.translation
                this[KEY_SURAH_NAME]   = prevAyah.surahName
                this[KEY_SURAH_NUMBER] = prevAyah.surahNumber
                this[KEY_AYAH_NUMBER]  = prevAyah.ayahNumber
                this[KEY_IS_BOOKMARKED] = exists
            }
        }
        AyahWidget().update(context, glanceId)

        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            appContainer.progressRepository.updateProgress(prevIndex)
        }
    }
}
