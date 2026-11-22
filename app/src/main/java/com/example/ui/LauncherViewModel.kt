package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LauncherPreferencesStore
import com.example.data.model.AppCategory
import com.example.data.model.AppSortOption
import com.example.data.model.BoostState
import com.example.data.model.InstalledAppInfo
import com.example.data.model.LauncherPreferences
import com.example.data.model.LauncherTab
import com.example.data.model.OpenSourceAppItem
import com.example.data.model.ScreenProfile
import com.example.data.model.SystemMetrics
import com.example.data.model.WallpaperInterval
import com.example.data.model.WallpaperItem
import com.example.data.repository.InstalledAppsRepository
import com.example.data.repository.OpenSourceStoreRepository
import com.example.data.repository.SystemBoosterRepository
import com.example.data.repository.WallpaperRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LauncherUiState(
    val activeTab: LauncherTab = LauncherTab.HOME,
    val preferences: LauncherPreferences = LauncherPreferences(),
    val currentTimeText: String = "",
    val currentSecondsText: String = "",
    val currentDateText: String = "",
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val isLoadingApps: Boolean = true,
    val appSearchQuery: String = "",
    val selectedCategory: AppCategory = AppCategory.ALL,
    val sortOption: AppSortOption = AppSortOption.NAME,
    val systemMetrics: SystemMetrics = SystemMetrics(),
    val boostState: BoostState = BoostState(),
    val wallpapers: List<WallpaperItem> = emptyList(),
    val activeWallpaper: WallpaperItem? = null,
    val isLoadingWallpapers: Boolean = false,
    val isApplyingSystemWallpaper: Boolean = false,
    val nextRotationLabel: String = "",
    val openSourceCatalog: List<OpenSourceAppItem> = emptyList(),
    val openSourceSearchQuery: String = "",
    val openSourceSearchResults: List<OpenSourceAppItem> = emptyList(),
    val isCheckingUpdates: Boolean = false,
    val isSearchingStore: Boolean = false,
    val isDefaultLauncher: Boolean = false,
    val showScreenSettingsSheet: Boolean = false,
    val snackbarMessage: String? = null
) {
    val pinnedApps: List<InstalledAppInfo>
        get() = installedApps.filter { it.packageName in preferences.pinnedPackages }

    val filteredInstalledApps: List<InstalledAppInfo>
        get() {
            val q = appSearchQuery.trim().lowercase()
            val filtered = installedApps.filter { app ->
                val matchesCat = selectedCategory == AppCategory.ALL || app.category == selectedCategory
                val matchesQuery = q.isEmpty() ||
                    app.label.lowercase().contains(q) ||
                    app.packageName.lowercase().contains(q)
                matchesCat && matchesQuery
            }
            return when (sortOption) {
                AppSortOption.NAME -> filtered.sortedBy { it.label.lowercase() }
                AppSortOption.INSTALL_DATE -> filtered.sortedByDescending { it.firstInstallTime }
                AppSortOption.CATEGORY -> filtered.sortedWith(
                    compareBy<InstalledAppInfo> { it.category.ordinal }
                        .thenBy { it.label.lowercase() }
                )
            }
        }

    val availableUpdatesCount: Int
        get() = openSourceCatalog.count { it.hasUpdate }
}

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsStore = LauncherPreferencesStore(application)
    private val appsRepo = InstalledAppsRepository(application)
    private val boosterRepo = SystemBoosterRepository(application)
    private val wallpaperRepo = WallpaperRepository(application)
    private val storeRepo = OpenSourceStoreRepository()

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
    private val secondsFormat = SimpleDateFormat("ss", Locale.US)
    private val arabicDateFormat = SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar"))

    init {
        val bundled = wallpaperRepo.getBundledWallpapers()
        _uiState.update {
            it.copy(
                wallpapers = bundled,
                activeWallpaper = bundled.firstOrNull(),
                systemMetrics = boosterRepo.collectMetrics(false),
                isDefaultLauncher = appsRepo.isDefaultHomeLauncher()
            )
        }

        observePreferences()
        loadAppsAndStore()
        refreshOnlineWallpapers(silent = true)
        startClockAndScheduleTicker()
        startMetricsMonitor()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            prefsStore.preferencesFlow.collect { prefs ->
                _uiState.update { state ->
                    val resolvedActive = resolveActiveWallpaper(prefs, state.wallpapers)
                    state.copy(
                        preferences = prefs,
                        activeWallpaper = resolvedActive,
                        boostState = state.boostState.copy(
                            lastBoostTimestamp = prefs.lastBoostTimestamp,
                            lastFreedRamMb = prefs.lastFreedRamMb,
                            lastClearedCacheKb = prefs.lastClearedCacheKb,
                            totalBoostsCount = prefs.totalBoostsCount
                        ),
                        nextRotationLabel = computeNextRotationLabel(prefs)
                    )
                }
            }
        }
    }

    private fun resolveActiveWallpaper(
        prefs: LauncherPreferences,
        wallpapers: List<WallpaperItem>
    ): WallpaperItem? {
        if (!prefs.customWallpaperUri.isNullOrBlank() && prefs.selectedWallpaperId == "custom_local") {
            return WallpaperItem(
                id = "custom_local",
                title = "خلفية مخصصة من جهازك",
                author = "مختارة محلياً",
                sourceName = "معرض الجهاز",
                license = "شخصية",
                categoryAr = "خلفية ثابتة",
                localUri = prefs.customWallpaperUri
            )
        }
        return wallpapers.firstOrNull { it.id == prefs.selectedWallpaperId }
            ?: wallpapers.firstOrNull()
    }

    private fun loadAppsAndStore() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingApps = true) }
            val apps = appsRepo.loadInstalledApps()
            val initialStore = storeRepo.buildInitialCatalog(apps)

            // Seed up to 6 useful apps as pinned shortcuts on very first launch
            if (apps.isNotEmpty()) {
                val seedCandidates = apps
                    .sortedWith(
                        compareBy<InstalledAppInfo> { it.isSystemApp }
                            .thenBy { it.category.ordinal }
                    )
                    .take(6)
                    .map { it.packageName }
                    .toSet()
                prefsStore.seedPinnedIfEmpty(seedCandidates)
            }

            _uiState.update {
                it.copy(
                    installedApps = apps,
                    isLoadingApps = false,
                    openSourceCatalog = initialStore
                )
            }

            checkOpenSourceUpdates(silent = true)
        }
    }

    fun reloadInstalledApps() {
        loadAppsAndStore()
    }

    private fun startClockAndScheduleTicker() {
        viewModelScope.launch {
            while (isActive) {
                val now = Date()
                val nowMs = now.time
                val currentPrefs = _uiState.value.preferences
                val interval = currentPrefs.wallpaperInterval

                if (interval != WallpaperInterval.STATIC_FIXED &&
                    currentPrefs.lastWallpaperRotationMs > 0L &&
                    nowMs - currentPrefs.lastWallpaperRotationMs >= interval.durationMs
                ) {
                    rotateToNextWallpaper(silent = true)
                }

                _uiState.update {
                    it.copy(
                        currentTimeText = timeFormat.format(now),
                        currentSecondsText = secondsFormat.format(now),
                        currentDateText = arabicDateFormat.format(now),
                        nextRotationLabel = computeNextRotationLabel(it.preferences)
                    )
                }
                delay(1000L)
            }
        }
    }

    private fun startMetricsMonitor() {
        viewModelScope.launch {
            while (isActive) {
                val metrics = boosterRepo.collectMetrics(_uiState.value.preferences.turboMode)
                _uiState.update { it.copy(systemMetrics = metrics) }
                delay(4000L)
            }
        }
    }

    fun selectTab(tab: LauncherTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun toggleScreenSettingsSheet(show: Boolean) {
        _uiState.update { it.copy(showScreenSettingsSheet = show) }
    }

    fun setAppSearchQuery(query: String) {
        _uiState.update { it.copy(appSearchQuery = query) }
    }

    fun selectAppCategory(category: AppCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun selectAppSort(sort: AppSortOption) {
        _uiState.update { it.copy(sortOption = sort) }
    }

    fun launchApp(packageName: String) {
        val launched = appsRepo.launchApp(packageName)
        if (!launched) {
            postSnackbar("تعذر تشغيل هذا التطبيق حالياً")
        }
    }

    fun openAppDetails(packageName: String) {
        appsRepo.openAppDetails(packageName)
    }

    fun togglePinApp(packageName: String, label: String) {
        viewModelScope.launch {
            val wasPinned = packageName in _uiState.value.preferences.pinnedPackages
            prefsStore.togglePinnedPackage(packageName)
            postSnackbar(
                if (wasPinned) "تمت إزالة $label من الشاشة الرئيسية"
                else "تم تثبيت $label في الشاشة الرئيسية"
            )
        }
    }

    fun triggerManualBoost() {
        if (_uiState.value.boostState.isBoosting) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    boostState = it.boostState.copy(
                        isBoosting = true,
                        progress = 0.08f,
                        stepMessage = "بدء فحص موارد النظام…"
                    )
                )
            }

            val packages = _uiState.value.installedApps.map { it.packageName }
            val turbo = _uiState.value.preferences.turboMode
            val report = boosterRepo.runManualBoost(
                installedPackageNames = packages,
                turboMode = turbo
            ) { progress, msg ->
                _uiState.update {
                    it.copy(
                        boostState = it.boostState.copy(
                            progress = progress,
                            stepMessage = msg
                        )
                    )
                }
            }

            val now = System.currentTimeMillis()
            prefsStore.recordBoostResult(
                freedRamMb = report.freedRamMb,
                clearedCacheKb = report.clearedCacheKb,
                timestamp = now
            )

            _uiState.update {
                it.copy(
                    systemMetrics = report.metricsAfter,
                    boostState = it.boostState.copy(
                        isBoosting = false,
                        progress = 1f,
                        stepMessage = "النظام يعمل بأفضل استجابة",
                        lastClosedBgCount = report.trimmedPackagesCount
                    )
                )
            }

            val summary = buildString {
                append("تم تسريع النظام بنجاح")
                if (report.freedRamMb > 0) append(" • تحرير ${report.freedRamMb} MB ذاكرة")
                if (report.clearedCacheKb > 0) append(" • تنظيف ${report.clearedCacheKb} KB مؤقتة")
            }
            postSnackbar(summary)
        }
    }

    fun openStorageSettings() {
        appsRepo.openStorageSettings()
    }

    fun isDefaultHomeLauncher(): Boolean = appsRepo.isDefaultHomeLauncher()

    fun createDefaultLauncherRequestIntent() = appsRepo.createDefaultLauncherRequestIntent()

    fun refreshDefaultLauncherStatus(showFeedbackIfGranted: Boolean = false) {
        val wasDefault = _uiState.value.isDefaultLauncher
        val nowDefault = appsRepo.isDefaultHomeLauncher()
        _uiState.update { it.copy(isDefaultLauncher = nowDefault) }
        if (showFeedbackIfGranted && nowDefault && !wasDefault) {
            postSnackbar("تم تعيين مدار لانشر كمشغّل افتراضي بنجاح")
        }
    }

    fun openDefaultLauncherSettings() {
        if (!appsRepo.openDefaultLauncherSettings()) {
            postSnackbar("افتح إعدادات النظام واختر مدار لانشر كمشغل رئيسي")
        }
    }

    fun setScreenProfile(profile: ScreenProfile) {
        viewModelScope.launch { prefsStore.setScreenProfile(profile) }
    }

    fun setGridColumns(columns: Int) {
        viewModelScope.launch { prefsStore.setGridColumns(columns) }
    }

    fun setIconScale(scale: Float) {
        viewModelScope.launch { prefsStore.setIconScale(scale) }
    }

    fun setTurboMode(enabled: Boolean) {
        viewModelScope.launch {
            prefsStore.setTurboMode(enabled)
            val metrics = boosterRepo.collectMetrics(enabled)
            _uiState.update { it.copy(systemMetrics = metrics) }
            postSnackbar(
                if (enabled) "تم تفعيل وضع الأداء الفائق للشاشات"
                else "تم إيقاف وضع الأداء الفائق"
            )
        }
    }

    fun setBackdropDimAlpha(alpha: Float) {
        viewModelScope.launch { prefsStore.setBackdropDimAlpha(alpha) }
    }

    fun setWallpaperInterval(interval: WallpaperInterval) {
        viewModelScope.launch {
            prefsStore.setWallpaperInterval(interval)
            postSnackbar("تم ضبط نظام الخلفية: ${interval.labelAr}")
        }
    }

    fun selectWallpaper(wallpaper: WallpaperItem) {
        viewModelScope.launch {
            prefsStore.selectWallpaper(wallpaper.id, System.currentTimeMillis())
            postSnackbar("تم تطبيق الخلفية: ${wallpaper.title}")
        }
    }

    fun setCustomLocalWallpaper(uri: Uri) {
        viewModelScope.launch {
            prefsStore.setCustomWallpaper(uri.toString())
            postSnackbar("تم تعيين الصورة المختارة كخلفية ثابتة")
        }
    }

    fun rotateToNextWallpaper(silent: Boolean = false) {
        viewModelScope.launch {
            val all = _uiState.value.wallpapers
            if (all.isEmpty()) return@launch
            val currentId = _uiState.value.activeWallpaper?.id
            val currentIdx = all.indexOfFirst { it.id == currentId }
            val next = all[(currentIdx + 1) % all.size]
            prefsStore.selectWallpaper(next.id, System.currentTimeMillis())
            if (!silent) {
                postSnackbar("تم تبديل الخلفية إلى: ${next.title}")
            }
        }
    }

    fun refreshOnlineWallpapers(silent: Boolean = false) {
        if (_uiState.value.isLoadingWallpapers) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingWallpapers = true) }
            val bundled = wallpaperRepo.getBundledWallpapers()
            val result = wallpaperRepo.fetchOpenSourceWallpapers()
            result.onSuccess { online ->
                val merged = bundled + online
                _uiState.update { state ->
                    state.copy(
                        wallpapers = merged,
                        activeWallpaper = resolveActiveWallpaper(state.preferences, merged),
                        isLoadingWallpapers = false
                    )
                }
                if (!silent) {
                    postSnackbar("تم جلب ${online.size} خلفية مفتوحة المصدر من Wikimedia Commons")
                }
            }.onFailure {
                _uiState.update { it.copy(isLoadingWallpapers = false) }
                if (!silent) {
                    postSnackbar("تعذر الاتصال بـ Wikimedia Commons — يتم عرض الخلفيات المدمجة")
                }
            }
        }
    }

    fun applyCurrentWallpaperToSystem() {
        val current = _uiState.value.activeWallpaper ?: return
        if (_uiState.value.isApplyingSystemWallpaper) return
        viewModelScope.launch {
            _uiState.update { it.copy(isApplyingSystemWallpaper = true) }
            val res = wallpaperRepo.applyAsSystemWallpaper(current)
            _uiState.update { it.copy(isApplyingSystemWallpaper = false) }
            res.onSuccess {
                postSnackbar("تم تعيين الخلفية كخلفية رسمية لنظام أندرويد")
            }.onFailure {
                postSnackbar("تعذر تعيين خلفية النظام: ${it.localizedMessage ?: "خطأ غير متوقع"}")
            }
        }
    }

    fun checkOpenSourceUpdates(silent: Boolean = false) {
        if (_uiState.value.isCheckingUpdates) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingUpdates = true) }
            val currentCatalog = _uiState.value.openSourceCatalog.ifEmpty {
                storeRepo.buildInitialCatalog(_uiState.value.installedApps)
            }
            val updated = storeRepo.checkLiveUpdates(currentCatalog, _uiState.value.installedApps)
            _uiState.update {
                it.copy(
                    openSourceCatalog = updated,
                    isCheckingUpdates = false
                )
            }
            if (!silent) {
                val updatesFound = updated.count { it.hasUpdate }
                postSnackbar(
                    if (updatesFound > 0) "تم العثور على $updatesFound تحديث مفتوح المصدر عبر F-Droid"
                    else "اكتمل الفحص عبر F-Droid — تطبيقاتك محدثة"
                )
            }
        }
    }

    fun setOpenSourceSearchQuery(query: String) {
        _uiState.update { it.copy(openSourceSearchQuery = query) }
        if (query.isBlank()) {
            _uiState.update { it.copy(openSourceSearchResults = emptyList(), isSearchingStore = false) }
        }
    }

    fun executeOpenSourceSearch() {
        val query = _uiState.value.openSourceSearchQuery.trim()
        if (query.length < 2) {
            postSnackbar("أدخل كلمتين أو حرفين على الأقل للبحث في F-Droid")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSearchingStore = true) }
            val res = storeRepo.searchFDroidCatalog(query, _uiState.value.installedApps)
            res.onSuccess { results ->
                _uiState.update {
                    it.copy(
                        openSourceSearchResults = results,
                        isSearchingStore = false
                    )
                }
                if (results.isEmpty()) {
                    postSnackbar("لا توجد نتائج مطابقة في مستودع F-Droid")
                }
            }.onFailure {
                _uiState.update { it.copy(isSearchingStore = false) }
                postSnackbar("تعذر البحث في F-Droid — تحقق من الاتصال بالإنترنت")
            }
        }
    }

    fun openTrustedOpenSourcePage(item: OpenSourceAppItem) {
        val ok = appsRepo.openTrustedHttpsUrl(item.pageUrl)
        if (!ok) {
            postSnackbar("تعذر فتح رابط المصدر المفتوح")
        }
    }

    fun consumeSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun postSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    private fun computeNextRotationLabel(prefs: LauncherPreferences): String {
        val interval = prefs.wallpaperInterval
        if (interval == WallpaperInterval.STATIC_FIXED) return "خلفية ثابتة (التغيير التلقائي متوقف)"
        val last = prefs.lastWallpaperRotationMs
        if (last <= 0L) return "نشط • ${interval.shortLabelAr}"
        val remaining = (interval.durationMs - (System.currentTimeMillis() - last)).coerceAtLeast(0L)
        val totalMinutes = remaining / 60_000L
        val hours = totalMinutes / 60L
        val mins = totalMinutes % 60L
        val secs = (remaining % 60_000L) / 1000L
        return when {
            hours > 0 -> "التبديل القادم خلال $hours س $mins د"
            mins > 0 -> "التبديل القادم خلال $mins د $secs ث"
            else -> "التبديل القادم خلال $secs ثانية"
        }
    }
}
