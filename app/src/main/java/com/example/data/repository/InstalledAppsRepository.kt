package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.data.model.AppCategory
import com.example.data.model.InstalledAppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InstalledAppsRepository(private val context: Context) {

    private val iconCache = mutableMapOf<String, ImageBitmap>()

    suspend fun loadInstalledApps(): List<InstalledAppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL)
        }

        val ownPackage = context.packageName
        val seenPackages = HashSet<String>()
        val result = ArrayList<InstalledAppInfo>(resolveInfos.size)

        for (resolveInfo in resolveInfos) {
            val activityInfo = resolveInfo.activityInfo ?: continue
            val pkgName = activityInfo.packageName ?: continue
            if (pkgName == ownPackage || !seenPackages.add(pkgName)) continue

            val label = runCatching { resolveInfo.loadLabel(pm)?.toString() }
                .getOrNull()
                ?.takeIf { it.isNotBlank() } ?: pkgName

            val pkgInfo = runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getPackageInfo(pkgName, PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(pkgName, 0)
                }
            }.getOrNull()

            val appInfo = pkgInfo?.applicationInfo ?: activityInfo.applicationInfo
            val versionName = pkgInfo?.versionName ?: "1.0"
            val versionCode = if (pkgInfo != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pkgInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    pkgInfo.versionCode.toLong()
                }
            } else {
                1L
            }
            val firstInstallTime = pkgInfo?.firstInstallTime ?: 0L
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val category = classifyApp(appInfo, pkgName, label)

            val bitmap = iconCache[pkgName] ?: runCatching {
                val drawable = resolveInfo.loadIcon(pm)
                drawableToImageBitmap(drawable, 96)?.also { iconCache[pkgName] = it }
            }.getOrNull()

            result.add(
                InstalledAppInfo(
                    packageName = pkgName,
                    label = label,
                    versionName = versionName,
                    versionCode = versionCode,
                    firstInstallTime = firstInstallTime,
                    category = category,
                    isSystemApp = isSystem,
                    iconBitmap = bitmap
                )
            )
        }

        result.sortedBy { it.label.lowercase() }
    }

    fun launchApp(packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun openAppDetails(packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    fun isDefaultHomeLauncher(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(android.app.role.RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(android.app.role.RoleManager.ROLE_HOME)) {
                return roleManager.isRoleHeld(android.app.role.RoleManager.ROLE_HOME)
            }
        }
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.resolveActivity(
                homeIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
        }
        return resolveInfo?.activityInfo?.packageName == context.packageName
    }

    fun createDefaultLauncherRequestIntent(): Intent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(android.app.role.RoleManager::class.java)
            if (roleManager != null &&
                roleManager.isRoleAvailable(android.app.role.RoleManager.ROLE_HOME) &&
                !roleManager.isRoleHeld(android.app.role.RoleManager.ROLE_HOME)
            ) {
                return roleManager.createRequestRoleIntent(android.app.role.RoleManager.ROLE_HOME)
            }
        }
        return Intent(Settings.ACTION_HOME_SETTINGS)
    }

    fun openDefaultLauncherSettings(): Boolean {
        val homeSettings = Intent(Settings.ACTION_HOME_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            context.startActivity(homeSettings)
            true
        }.getOrElse {
            val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching {
                context.startActivity(fallback)
                true
            }.getOrDefault(false)
        }
    }

    fun openStorageSettings() {
        val intent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    fun openTrustedHttpsUrl(url: String): Boolean {
        if (!url.startsWith("https://")) return false
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    private fun classifyApp(
        appInfo: ApplicationInfo,
        pkg: String,
        label: String
    ): AppCategory {
        val lowerPkg = pkg.lowercase()
        val lowerLabel = label.lowercase()

        if (lowerPkg.contains("map") || lowerPkg.contains("nav") || lowerPkg.contains("waze") ||
            lowerPkg.contains("osmand") || lowerPkg.contains("gps") || lowerLabel.contains("خرائط")
        ) {
            return AppCategory.NAVIGATION
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (appInfo.category) {
                ApplicationInfo.CATEGORY_MAPS -> return AppCategory.NAVIGATION
                ApplicationInfo.CATEGORY_AUDIO,
                ApplicationInfo.CATEGORY_VIDEO,
                ApplicationInfo.CATEGORY_IMAGE -> return AppCategory.MEDIA
                ApplicationInfo.CATEGORY_SOCIAL -> return AppCategory.COMMUNICATION
                ApplicationInfo.CATEGORY_GAME -> return AppCategory.GAMES
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> return AppCategory.TOOLS
            }
        }

        return when {
            listOf("music", "video", "vlc", "tube", "radio", "spotify", "media", "player", "gallery", "camera", "podcast")
                .any { lowerPkg.contains(it) || lowerLabel.contains(it) } -> AppCategory.MEDIA
            listOf("whatsapp", "telegram", "dialer", "phone", "message", "sms", "mail", "chat", "contacts", "signal")
                .any { lowerPkg.contains(it) || lowerLabel.contains(it) } -> AppCategory.COMMUNICATION
            listOf("game", "chess", "puzzle", "arcade", "play")
                .any { lowerPkg.contains(it) } -> AppCategory.GAMES
            listOf("settings", "calc", "clock", "files", "browser", "chrome", "firefox", "fennec", "termux", "tool", "weather", "calendar")
                .any { lowerPkg.contains(it) || lowerLabel.contains(it) } -> AppCategory.TOOLS
            else -> AppCategory.OTHER
        }
    }

    private fun drawableToImageBitmap(drawable: Drawable, targetSizePx: Int): ImageBitmap? {
        return try {
            if (drawable is BitmapDrawable && drawable.bitmap != null) {
                val bmp = drawable.bitmap
                val scaled = if (bmp.width > targetSizePx * 2 || bmp.height > targetSizePx * 2) {
                    Bitmap.createScaledBitmap(bmp, targetSizePx, targetSizePx, true)
                } else {
                    bmp
                }
                scaled.asImageBitmap()
            } else {
                val width = drawable.intrinsicWidth.takeIf { it > 0 }?.coerceAtMost(targetSizePx) ?: targetSizePx
                val height = drawable.intrinsicHeight.takeIf { it > 0 }?.coerceAtMost(targetSizePx) ?: targetSizePx
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap.asImageBitmap()
            }
        } catch (_: Throwable) {
            null
        }
    }
}
