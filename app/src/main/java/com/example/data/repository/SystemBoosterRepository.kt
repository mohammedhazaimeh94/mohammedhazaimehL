package com.example.data.repository

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import coil.Coil
import com.example.data.model.SystemMetrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

data class BoostExecutionReport(
    val freedRamMb: Long,
    val clearedCacheKb: Long,
    val trimmedPackagesCount: Int,
    val metricsAfter: SystemMetrics
)

class SystemBoosterRepository(private val context: Context) {

    fun collectMetrics(turboMode: Boolean): SystemMetrics {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val totalRamMb = (memInfo.totalMem / (1024L * 1024L)).coerceAtLeast(1L)
        val availRamMb = (memInfo.availMem / (1024L * 1024L)).coerceAtLeast(0L)
        val usedRamMb = (totalRamMb - availRamMb).coerceAtLeast(0L)
        val ramPercent = ((usedRamMb.toFloat() / totalRamMb.toFloat()) * 100f).roundToInt().coerceIn(0, 100)

        val statFs = StatFs(Environment.getDataDirectory().path)
        val totalBytes = statFs.blockCountLong * statFs.blockSizeLong
        val freeBytes = statFs.availableBlocksLong * statFs.blockSizeLong
        val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)

        val gbDiv = 1024f * 1024f * 1024f
        val totalStorageGb = totalBytes / gbDiv
        val freeStorageGb = freeBytes / gbDiv
        val usedStorageGb = usedBytes / gbDiv
        val storagePercent = if (totalBytes > 0L) {
            ((usedBytes.toFloat() / totalBytes.toFloat()) * 100f).roundToInt().coerceIn(0, 100)
        } else 0

        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 100) ?: 100
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100)?.coerceAtLeast(1) ?: 100
        val batteryPct = ((level.toFloat() / scale.toFloat()) * 100f).roundToInt().coerceIn(0, 100)
        val tempTenths = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 310) ?: 310
        val batteryTempC = tempTenths / 10f

        val ramScore = (100 - ramPercent).coerceIn(10, 100)
        val storageScore = (100 - storagePercent).coerceIn(15, 100)
        val thermalBonus = if (batteryTempC < 40f) 10 else if (batteryTempC < 45f) 4 else -8
        val turboBonus = if (turboMode) 7 else 0
        val score = ((ramScore * 0.62f) + (storageScore * 0.28f) + thermalBonus + turboBonus)
            .roundToInt()
            .coerceIn(25, 99)

        return SystemMetrics(
            totalRamMb = totalRamMb,
            usedRamMb = usedRamMb,
            availableRamMb = availRamMb,
            ramUsagePercent = ramPercent,
            totalStorageGb = totalStorageGb,
            usedStorageGb = usedStorageGb,
            freeStorageGb = freeStorageGb,
            storageUsagePercent = storagePercent,
            cpuCores = Runtime.getRuntime().availableProcessors(),
            batteryLevel = batteryPct,
            batteryTempC = batteryTempC,
            deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}".trim(),
            androidVersion = "Android ${Build.VERSION.RELEASE}",
            isLowMemory = memInfo.lowMemory,
            responsivenessScore = score
        )
    }

    suspend fun runManualBoost(
        installedPackageNames: List<String>,
        turboMode: Boolean,
        onStepProgress: (progress: Float, message: String) -> Unit
    ): BoostExecutionReport = withContext(Dispatchers.IO) {
        val before = collectMetrics(turboMode)

        onStepProgress(0.22f, "فحص الذاكرة العشوائية والعمليات الخلفية…")
        delay(260)

        onStepProgress(0.48f, "تنظيف ذاكرة الصور والملفات المؤقتة…")
        val clearedBytes = clearLauncherCaches()
        delay(260)

        onStepProgress(0.76f, "تحرير العمليات غير النشطة لتحسين الاستجابة…")
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val ownPkg = context.packageName
        var trimmedCount = 0
        for (pkg in installedPackageNames) {
            if (pkg == ownPkg) continue
            runCatching {
                activityManager.killBackgroundProcesses(pkg)
                trimmedCount++
            }
        }

        // Request JVM garbage collection for the launcher process itself
        System.gc()
        delay(320)

        onStepProgress(1.0f, "اكتمل التحسين وتحديث مؤشرات النظام")
        val after = collectMetrics(turboMode)
        val actualRamDelta = (after.availableRamMb - before.availableRamMb).coerceAtLeast(0L)
        val cacheKb = (clearedBytes / 1024L).coerceAtLeast(0L)

        BoostExecutionReport(
            freedRamMb = actualRamDelta,
            clearedCacheKb = cacheKb,
            trimmedPackagesCount = trimmedCount,
            metricsAfter = after
        )
    }

    private fun clearLauncherCaches(): Long {
        var bytesFreed = 0L
        runCatching {
            val imageLoader = Coil.imageLoader(context)
            imageLoader.memoryCache?.clear()
            imageLoader.diskCache?.let { disk ->
                bytesFreed += disk.size
                disk.clear()
            }
        }
        bytesFreed += deleteDirectoryContents(context.cacheDir)
        context.externalCacheDir?.let { ext ->
            bytesFreed += deleteDirectoryContents(ext)
        }
        return bytesFreed
    }

    private fun deleteDirectoryContents(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var total = 0L
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                total += deleteDirectoryContents(file)
                file.delete()
            } else {
                val len = file.length()
                if (file.delete()) {
                    total += len
                }
            }
        }
        return total
    }
}
