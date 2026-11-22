package com.example.data.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.ImageBitmap

enum class LauncherTab {
    HOME,
    APPS,
    BOOST,
    WALLPAPERS,
    UPDATES
}

enum class ScreenProfile(val labelAr: String, val descAr: String) {
    AUTO("تلقائي ذكي", "يتكيف تلقائياً مع أبعاد واتجاه أي شاشة"),
    CAR_WIDE("شاشة سيارة صينية", "شريط جانبي وأيقونات كبيرة وواضحة للقيادة"),
    TV_TABLET("تلفاز وتابلت", "شبكة واسعة مناسبة للشاشات الكبيرة"),
    COMPACT("هاتف ومدمج", "تصميم عمودي مريح للاستخدام السريع")
}

enum class AppCategory(val labelAr: String) {
    ALL("الكل"),
    NAVIGATION("الملاحة والخرائط"),
    MEDIA("الوسائط"),
    COMMUNICATION("التواصل"),
    TOOLS("الأدوات والنظام"),
    GAMES("الألعاب"),
    OTHER("أخرى")
}

enum class AppSortOption(val labelAr: String) {
    NAME("الاسم"),
    INSTALL_DATE("تاريخ التثبيت"),
    CATEGORY("الفئة")
}

data class InstalledAppInfo(
    val packageName: String,
    val label: String,
    val versionName: String,
    val versionCode: Long,
    val firstInstallTime: Long,
    val category: AppCategory,
    val isSystemApp: Boolean,
    val iconBitmap: ImageBitmap? = null
)

enum class WallpaperInterval(val labelAr: String, val shortLabelAr: String, val durationMs: Long) {
    STATIC_FIXED("خلفية ثابتة (يدوي)", "ثابتة", 0L),
    EVERY_30_MIN("تغيير تلقائي كل 30 دقيقة", "كل 30 دقيقة", 30L * 60L * 1000L),
    EVERY_6_HOURS("تغيير تلقائي كل 6 ساعات", "كل 6 ساعات", 6L * 60L * 60L * 1000L),
    EVERY_24_HOURS("تغيير تلقائي كل 24 ساعة", "كل 24 ساعة", 24L * 60L * 60L * 1000L)
}

data class WallpaperItem(
    val id: String,
    val title: String,
    val author: String,
    val sourceName: String,
    val license: String,
    val categoryAr: String,
    val imageUrl: String? = null,
    @DrawableRes val drawableRes: Int? = null,
    val localUri: String? = null
)

data class SystemMetrics(
    val totalRamMb: Long = 0L,
    val usedRamMb: Long = 0L,
    val availableRamMb: Long = 0L,
    val ramUsagePercent: Int = 0,
    val totalStorageGb: Float = 0f,
    val usedStorageGb: Float = 0f,
    val freeStorageGb: Float = 0f,
    val storageUsagePercent: Int = 0,
    val cpuCores: Int = 4,
    val batteryLevel: Int = 100,
    val batteryTempC: Float = 32f,
    val deviceModel: String = "",
    val androidVersion: String = "",
    val isLowMemory: Boolean = false,
    val responsivenessScore: Int = 88
)

data class BoostState(
    val isBoosting: Boolean = false,
    val progress: Float = 0f,
    val stepMessage: String = "",
    val lastBoostTimestamp: Long = 0L,
    val lastFreedRamMb: Long = 0L,
    val lastClearedCacheKb: Long = 0L,
    val lastClosedBgCount: Int = 0,
    val totalBoostsCount: Int = 0
)

data class OpenSourceAppItem(
    val packageName: String,
    val name: String,
    val arabicSummary: String,
    val categoryAr: String,
    val repoName: String = "F-Droid",
    val iconUrl: String? = null,
    val pageUrl: String = "https://f-droid.org/packages/$packageName/",
    val installedVersionName: String? = null,
    val installedVersionCode: Long? = null,
    val latestVersionName: String? = null,
    val latestVersionCode: Long? = null,
    val isChecking: Boolean = false
) {
    val isInstalled: Boolean get() = installedVersionCode != null
    val hasUpdate: Boolean
        get() = installedVersionCode != null &&
            latestVersionCode != null &&
            latestVersionCode > installedVersionCode
}

data class LauncherPreferences(
    val screenProfile: ScreenProfile = ScreenProfile.AUTO,
    val gridColumns: Int = 0, // 0 means automatic based on available width
    val iconScale: Float = 1.0f,
    val turboMode: Boolean = false,
    val wallpaperInterval: WallpaperInterval = WallpaperInterval.EVERY_24_HOURS,
    val selectedWallpaperId: String = "bundled_cockpit",
    val customWallpaperUri: String? = null,
    val lastWallpaperRotationMs: Long = 0L,
    val backdropDimAlpha: Float = 0.42f,
    val pinnedPackages: Set<String> = emptySet(),
    val lastBoostTimestamp: Long = 0L,
    val lastFreedRamMb: Long = 0L,
    val lastClearedCacheKb: Long = 0L,
    val totalBoostsCount: Int = 0
)
