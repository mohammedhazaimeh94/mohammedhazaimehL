package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.LauncherPreferences
import com.example.data.model.ScreenProfile
import com.example.data.model.WallpaperInterval
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.madarDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "madar_launcher_prefs"
)

class LauncherPreferencesStore(private val context: Context) {

    private object Keys {
        val SCREEN_PROFILE = stringPreferencesKey("screen_profile")
        val GRID_COLUMNS = intPreferencesKey("grid_columns")
        val ICON_SCALE = floatPreferencesKey("icon_scale")
        val TURBO_MODE = booleanPreferencesKey("turbo_mode")
        val WALLPAPER_INTERVAL = stringPreferencesKey("wallpaper_interval")
        val SELECTED_WALLPAPER_ID = stringPreferencesKey("selected_wallpaper_id")
        val CUSTOM_WALLPAPER_URI = stringPreferencesKey("custom_wallpaper_uri")
        val LAST_WALLPAPER_ROTATION_MS = longPreferencesKey("last_wallpaper_rotation_ms")
        val BACKDROP_DIM_ALPHA = floatPreferencesKey("backdrop_dim_alpha")
        val PINNED_PACKAGES = stringSetPreferencesKey("pinned_packages")
        val LAST_BOOST_TIMESTAMP = longPreferencesKey("last_boost_timestamp")
        val LAST_FREED_RAM_MB = longPreferencesKey("last_freed_ram_mb")
        val LAST_CLEARED_CACHE_KB = longPreferencesKey("last_cleared_cache_kb")
        val TOTAL_BOOSTS_COUNT = intPreferencesKey("total_boosts_count")
    }

    val preferencesFlow: Flow<LauncherPreferences> = context.madarDataStore.data.map { prefs ->
        val profile = runCatching {
            ScreenProfile.valueOf(prefs[Keys.SCREEN_PROFILE] ?: ScreenProfile.AUTO.name)
        }.getOrDefault(ScreenProfile.AUTO)

        val interval = runCatching {
            WallpaperInterval.valueOf(
                prefs[Keys.WALLPAPER_INTERVAL] ?: WallpaperInterval.EVERY_24_HOURS.name
            )
        }.getOrDefault(WallpaperInterval.EVERY_24_HOURS)

        LauncherPreferences(
            screenProfile = profile,
            gridColumns = prefs[Keys.GRID_COLUMNS] ?: 0,
            iconScale = (prefs[Keys.ICON_SCALE] ?: 1.0f).coerceIn(0.8f, 1.3f),
            turboMode = prefs[Keys.TURBO_MODE] ?: false,
            wallpaperInterval = interval,
            selectedWallpaperId = prefs[Keys.SELECTED_WALLPAPER_ID] ?: "bundled_cockpit",
            customWallpaperUri = prefs[Keys.CUSTOM_WALLPAPER_URI]?.takeIf { it.isNotBlank() },
            lastWallpaperRotationMs = prefs[Keys.LAST_WALLPAPER_ROTATION_MS] ?: 0L,
            backdropDimAlpha = (prefs[Keys.BACKDROP_DIM_ALPHA] ?: 0.42f).coerceIn(0.15f, 0.78f),
            pinnedPackages = prefs[Keys.PINNED_PACKAGES] ?: emptySet(),
            lastBoostTimestamp = prefs[Keys.LAST_BOOST_TIMESTAMP] ?: 0L,
            lastFreedRamMb = prefs[Keys.LAST_FREED_RAM_MB] ?: 0L,
            lastClearedCacheKb = prefs[Keys.LAST_CLEARED_CACHE_KB] ?: 0L,
            totalBoostsCount = prefs[Keys.TOTAL_BOOSTS_COUNT] ?: 0
        )
    }

    suspend fun setScreenProfile(profile: ScreenProfile) {
        context.madarDataStore.edit { it[Keys.SCREEN_PROFILE] = profile.name }
    }

    suspend fun setGridColumns(columns: Int) {
        context.madarDataStore.edit { it[Keys.GRID_COLUMNS] = columns }
    }

    suspend fun setIconScale(scale: Float) {
        context.madarDataStore.edit { it[Keys.ICON_SCALE] = scale.coerceIn(0.8f, 1.3f) }
    }

    suspend fun setTurboMode(enabled: Boolean) {
        context.madarDataStore.edit { it[Keys.TURBO_MODE] = enabled }
    }

    suspend fun setWallpaperInterval(interval: WallpaperInterval) {
        context.madarDataStore.edit {
            it[Keys.WALLPAPER_INTERVAL] = interval.name
            if (interval != WallpaperInterval.STATIC_FIXED && (it[Keys.LAST_WALLPAPER_ROTATION_MS] ?: 0L) == 0L) {
                it[Keys.LAST_WALLPAPER_ROTATION_MS] = System.currentTimeMillis()
            }
        }
    }

    suspend fun selectWallpaper(id: String, rotationTimestampMs: Long = System.currentTimeMillis()) {
        context.madarDataStore.edit {
            it[Keys.SELECTED_WALLPAPER_ID] = id
            it[Keys.CUSTOM_WALLPAPER_URI] = ""
            it[Keys.LAST_WALLPAPER_ROTATION_MS] = rotationTimestampMs
        }
    }

    suspend fun setCustomWallpaper(uriString: String) {
        context.madarDataStore.edit {
            it[Keys.CUSTOM_WALLPAPER_URI] = uriString
            it[Keys.SELECTED_WALLPAPER_ID] = "custom_local"
            it[Keys.WALLPAPER_INTERVAL] = WallpaperInterval.STATIC_FIXED.name
            it[Keys.LAST_WALLPAPER_ROTATION_MS] = System.currentTimeMillis()
        }
    }

    suspend fun setBackdropDimAlpha(alpha: Float) {
        context.madarDataStore.edit { it[Keys.BACKDROP_DIM_ALPHA] = alpha.coerceIn(0.15f, 0.78f) }
    }

    suspend fun togglePinnedPackage(packageName: String) {
        context.madarDataStore.edit { prefs ->
            val current = prefs[Keys.PINNED_PACKAGES] ?: emptySet()
            prefs[Keys.PINNED_PACKAGES] = if (packageName in current) {
                current - packageName
            } else {
                current + packageName
            }
        }
    }

    suspend fun seedPinnedIfEmpty(defaultPackages: Set<String>) {
        context.madarDataStore.edit { prefs ->
            if (prefs[Keys.PINNED_PACKAGES] == null && defaultPackages.isNotEmpty()) {
                prefs[Keys.PINNED_PACKAGES] = defaultPackages
            }
        }
    }

    suspend fun recordBoostResult(freedRamMb: Long, clearedCacheKb: Long, timestamp: Long) {
        context.madarDataStore.edit { prefs ->
            prefs[Keys.LAST_BOOST_TIMESTAMP] = timestamp
            prefs[Keys.LAST_FREED_RAM_MB] = freedRamMb
            prefs[Keys.LAST_CLEARED_CACHE_KB] = clearedCacheKb
            prefs[Keys.TOTAL_BOOSTS_COUNT] = (prefs[Keys.TOTAL_BOOSTS_COUNT] ?: 0) + 1
        }
    }
}
