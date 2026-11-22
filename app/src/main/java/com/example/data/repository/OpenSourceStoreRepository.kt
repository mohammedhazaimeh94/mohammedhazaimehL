package com.example.data.repository

import com.example.data.model.InstalledAppInfo
import com.example.data.model.OpenSourceAppItem
import com.example.data.remote.OpenSourceNetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class OpenSourceStoreRepository {

    private val curatedCatalog: List<OpenSourceAppItem> = listOf(
        OpenSourceAppItem(
            packageName = "app.organicmaps",
            name = "Organic Maps",
            arabicSummary = "خرائط وملاحة مجانية تعمل بدون إنترنت مثالية لشاشات السيارات",
            categoryAr = "الملاحة والخرائط",
            iconUrl = "https://f-droid.org/repo/app.organicmaps/en-US/icon.png"
        ),
        OpenSourceAppItem(
            packageName = "net.osmand.plus",
            name = "OsmAnd~",
            arabicSummary = "خرائط تفصيلية وملاحة صوتية مفتوحة المصدر بالكامل",
            categoryAr = "الملاحة والخرائط",
            iconUrl = "https://f-droid.org/repo/net.osmand.plus/en-US/icon.png"
        ),
        OpenSourceAppItem(
            packageName = "org.videolan.vlc",
            name = "VLC Media Player",
            arabicSummary = "مشغل فيديو وصوت شامل يدعم جميع الصيغ لشاشات أندرويد",
            categoryAr = "الوسائط",
            iconUrl = "https://f-droid.org/repo/org.videolan.vlc/en-US/icon.png"
        ),
        OpenSourceAppItem(
            packageName = "org.schabi.newpipe",
            name = "NewPipe",
            arabicSummary = "مشغل فيديو خفيف وسريع بدون إعلانات أو تسجيل دخول",
            categoryAr = "الوسائط",
            iconUrl = "https://f-droid.org/repo/org.schabi.newpipe/en-US/icon.png"
        ),
        OpenSourceAppItem(
            packageName = "com.github.libretube",
            name = "LibreTube",
            arabicSummary = "واجهة عصرية مفتوحة المصدر لمشاهدة المحتوى بدون حساب",
            categoryAr = "الوسائط",
            iconUrl = "https://f-droid.org/repo/com.github.libretube/en-US/icon.png"
        ),
        OpenSourceAppItem(
            packageName = "de.danoeh.antennapod",
            name = "AntennaPod",
            arabicSummary = "مشغل بودكاست وراديو صوتي مفتوح المصدر وسهل التحكم",
            categoryAr = "الوسائط",
            iconUrl = "https://f-droid.org/repo/de.danoeh.antennapod/en-US/icon.png"
        ),
        OpenSourceAppItem(
            packageName = "org.mozilla.fennec_fdroid",
            name = "Fennec F-Droid",
            arabicSummary = "متصفح ويب سريع يركز على الخصوصية وخفيف على الشاشات",
            categoryAr = "الأدوات والنظام",
            iconUrl = "https://f-droid.org/repo/org.mozilla.fennec_fdroid/en-US/icon.png"
        ),
        OpenSourceAppItem(
            packageName = "org.localsend.localsend_app",
            name = "LocalSend",
            arabicSummary = "إرسال واستقبال الملفات بسرعة بين الهاتف والشاشة عبر Wi-Fi",
            categoryAr = "الأدوات والنظام",
            iconUrl = "https://f-droid.org/repo/org.localsend.localsend_app/en-US/icon.png"
        ),
        OpenSourceAppItem(
            packageName = "org.kde.kdeconnect_tp",
            name = "KDE Connect",
            arabicSummary = "ربط الشاشة بالهاتف والكمبيوتر للتحكم والمشاركة اللاسلكية",
            categoryAr = "الأدوات والنظام",
            iconUrl = "https://f-droid.org/repo/org.kde.kdeconnect_tp/en-US/icon.png"
        ),
        OpenSourceAppItem(
            packageName = "org.breezyweather",
            name = "Breezy Weather",
            arabicSummary = "تطبيق طقس دقيق ومفتوح المصدر بتصميم عصري",
            categoryAr = "الأدوات والنظام",
            iconUrl = "https://f-droid.org/repo/org.breezyweather/en-US/icon.png"
        ),
        OpenSourceAppItem(
            packageName = "org.fossify.gallery",
            name = "Fossify Gallery",
            arabicSummary = "معرض صور وفيديو سريع وخفيف بدون إعلانات",
            categoryAr = "الوسائط",
            iconUrl = "https://f-droid.org/repo/org.fossify.gallery/en-US/icon.png"
        ),
        OpenSourceAppItem(
            packageName = "org.fossify.musicplayer",
            name = "Fossify Music",
            arabicSummary = "مشغل صوتيات محلي خفيف وسريع الاستجابة لشاشات السيارات",
            categoryAr = "الوسائط",
            iconUrl = "https://f-droid.org/repo/org.fossify.musicplayer/en-US/icon.png"
        ),
        OpenSourceAppItem(
            packageName = "org.fdroid.fdroid",
            name = "F-Droid Client",
            arabicSummary = "المتجر الرسمي المفتوح المصدر لتحديث التطبيقات تلقائياً بدون حساب",
            categoryAr = "الأدوات والنظام",
            iconUrl = "https://f-droid.org/repo/org.fdroid.fdroid/en-US/icon.png"
        ),
        OpenSourceAppItem(
            packageName = "com.termux",
            name = "Termux",
            arabicSummary = "طرفية لينكس متكاملة للمطورين وإدارة النظام المتقدمة",
            categoryAr = "الأدوات والنظام",
            iconUrl = "https://f-droid.org/repo/com.termux/en-US/icon.png"
        )
    )

    fun buildInitialCatalog(installedApps: List<InstalledAppInfo>): List<OpenSourceAppItem> {
        val installedByPkg = installedApps.associateBy { it.packageName }
        return curatedCatalog.map { item ->
            val installed = installedByPkg[item.packageName]
            item.copy(
                installedVersionName = installed?.versionName,
                installedVersionCode = installed?.versionCode
            )
        }
    }

    suspend fun checkLiveUpdates(
        catalog: List<OpenSourceAppItem>,
        installedApps: List<InstalledAppInfo>
    ): List<OpenSourceAppItem> = withContext(Dispatchers.IO) {
        val installedByPkg = installedApps.associateBy { it.packageName }

        // Also check up to 12 non-system user installed apps not in the curated list against F-Droid
        val curatedPkgs = catalog.map { it.packageName }.toSet()
        val extraInstalled = installedApps
            .filter { !it.isSystemApp && it.packageName !in curatedPkgs }
            .take(12)
            .map { app ->
                OpenSourceAppItem(
                    packageName = app.packageName,
                    name = app.label,
                    arabicSummary = "تطبيق مثبت على جهازك — يتم التحقق من توفره في مستودع F-Droid",
                    categoryAr = app.category.labelAr,
                    installedVersionName = app.versionName,
                    installedVersionCode = app.versionCode
                )
            }

        coroutineScope {
            val checkedCatalog = catalog.map { item ->
                async {
                    val installed = installedByPkg[item.packageName]
                    val live = runCatching {
                        OpenSourceNetworkClient.fdroidPackagesApi.getPackageInfo(item.packageName)
                    }.getOrNull()

                    val latestCode = live?.suggestedVersionCode
                        ?: live?.packages?.maxOfOrNull { it.versionCode ?: 0L }
                    val latestName = live?.packages
                        ?.firstOrNull { it.versionCode == latestCode }?.versionName
                        ?: live?.packages?.firstOrNull()?.versionName

                    item.copy(
                        installedVersionName = installed?.versionName,
                        installedVersionCode = installed?.versionCode,
                        latestVersionCode = latestCode,
                        latestVersionName = latestName,
                        isChecking = false
                    )
                }
            }.awaitAll()

            val checkedExtra = extraInstalled.map { item ->
                async {
                    val live = runCatching {
                        OpenSourceNetworkClient.fdroidPackagesApi.getPackageInfo(item.packageName)
                    }.getOrNull()
                    val latestCode = live?.suggestedVersionCode
                        ?: live?.packages?.maxOfOrNull { it.versionCode ?: 0L }
                    if (latestCode != null && latestCode > 0L) {
                        val latestName = live?.packages
                            ?.firstOrNull { it.versionCode == latestCode }?.versionName
                            ?: live?.packages?.firstOrNull()?.versionName
                        item.copy(
                            latestVersionCode = latestCode,
                            latestVersionName = latestName,
                            isChecking = false
                        )
                    } else {
                        null
                    }
                }
            }.awaitAll().filterNotNull()

            (checkedExtra + checkedCatalog)
                .distinctBy { it.packageName }
                .sortedWith(
                    compareByDescending<OpenSourceAppItem> { it.hasUpdate }
                        .thenByDescending { it.isInstalled }
                        .thenBy { it.name.lowercase() }
                )
        }
    }

    suspend fun searchFDroidCatalog(
        query: String,
        installedApps: List<InstalledAppInfo>
    ): Result<List<OpenSourceAppItem>> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext Result.success(emptyList())

        val installedByPkg = installedApps.associateBy { it.packageName }
        runCatching {
            val response = OpenSourceNetworkClient.fdroidSearchApi.searchApps(trimmed)
            response.apps.orEmpty().take(24).mapNotNull { app ->
                val url = app.url?.takeIf { it.startsWith("https://") } ?: return@mapNotNull null
                val pkg = url.trimEnd('/').substringAfterLast('/')
                if (pkg.isBlank()) return@mapNotNull null
                val installed = installedByPkg[pkg]
                OpenSourceAppItem(
                    packageName = pkg,
                    name = app.name?.takeIf { it.isNotBlank() } ?: pkg,
                    arabicSummary = app.summary?.takeIf { it.isNotBlank() } ?: "تطبيق مفتوح المصدر من مستودع F-Droid",
                    categoryAr = "نتائج F-Droid",
                    repoName = "F-Droid",
                    iconUrl = app.icon?.takeIf { it.startsWith("https://") },
                    pageUrl = url,
                    installedVersionName = installed?.versionName,
                    installedVersionCode = installed?.versionCode
                )
            }
        }
    }
}
