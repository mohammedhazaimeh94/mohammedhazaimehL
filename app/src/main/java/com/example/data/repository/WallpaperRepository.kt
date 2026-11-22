package com.example.data.repository

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.R
import com.example.data.model.WallpaperItem
import com.example.data.remote.OpenSourceNetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WallpaperRepository(private val context: Context) {

    fun getBundledWallpapers(): List<WallpaperItem> = listOf(
        WallpaperItem(
            id = "bundled_cockpit",
            title = "طريق الساحل الليلي",
            author = "مجموعة مدار المدمجة",
            sourceName = "مدمج • جاهز بدون إنترنت",
            license = "مفتوح المصدر",
            categoryAr = "قيادة وشاشات",
            drawableRes = R.drawable.img_wall_cockpit_1795332614304
        ),
        WallpaperItem(
            id = "bundled_desert_night",
            title = "صحراء النجوم والمجرة",
            author = "مجموعة مدار المدمجة",
            sourceName = "مدمج • جاهز بدون إنترنت",
            license = "مفتوح المصدر",
            categoryAr = "طبيعة ليلية",
            drawableRes = R.drawable.img_wall_desert_night_1795332612420
        ),
        WallpaperItem(
            id = "bundled_aurora_peaks",
            title = "قمم الشفق القطبي",
            author = "مجموعة مدار المدمجة",
            sourceName = "مدمج • جاهز بدون إنترنت",
            license = "مفتوح المصدر",
            categoryAr = "جبال وطبيعة",
            drawableRes = R.drawable.img_wall_aurora_peaks_1795332614903
        )
    )

    suspend fun fetchOpenSourceWallpapers(): Result<List<WallpaperItem>> = withContext(Dispatchers.IO) {
        val categories = listOf(
            "Category:Featured_pictures_of_landscapes" to "مناظر طبيعية",
            "Category:Featured_pictures_of_astronomy" to "فضاء وفلك",
            "Category:Featured_pictures_of_architecture" to "عمارة ومدن"
        )

        val collected = mutableListOf<WallpaperItem>()
        var lastError: Throwable? = null

        for ((wikiCategory, arabicCat) in categories) {
            try {
                val response = OpenSourceNetworkClient.wikimediaApi.getFeaturedCategoryImages(
                    categoryTitle = wikiCategory,
                    limit = 8
                )
                val pages = response.query?.pages?.values.orEmpty()
                for (page in pages) {
                    val info = page.imageinfo?.firstOrNull() ?: continue
                    val imageUrl = info.thumburl ?: info.url ?: continue
                    if (!imageUrl.startsWith("https://")) continue
                    val lower = imageUrl.lowercase()
                    if (!lower.contains(".jpg") && !lower.contains(".jpeg") && !lower.contains(".png") && !lower.contains(".webp")) {
                        continue
                    }
                    // Prefer wide/landscape images for Chinese car & TV screens
                    val w = info.width ?: 1600
                    val h = info.height ?: 900
                    if (w < h) continue

                    val rawTitle = page.title
                        ?.removePrefix("File:")
                        ?.substringBeforeLast('.')
                        ?.replace('_', ' ')
                        ?.take(48)
                        ?: "خلفية مفتوحة المصدر"

                    val artist = stripHtml(info.extmetadata?.get("Artist")?.value)
                        .takeIf { it.isNotBlank() }?.take(36) ?: "مساهم ويكيميديا"

                    val license = stripHtml(info.extmetadata?.get("LicenseShortName")?.value)
                        .takeIf { it.isNotBlank() }?.take(28) ?: "CC BY-SA / Public Domain"

                    collected.add(
                        WallpaperItem(
                            id = "wiki_${page.pageid ?: imageUrl.hashCode()}",
                            title = rawTitle,
                            author = artist,
                            sourceName = "Wikimedia Commons",
                            license = license,
                            categoryAr = arabicCat,
                            imageUrl = imageUrl
                        )
                    )
                }
            } catch (t: Throwable) {
                lastError = t
            }
        }

        if (collected.isNotEmpty()) {
            Result.success(collected.distinctBy { it.id })
        } else {
            Result.failure(lastError ?: IllegalStateException("تعذر جلب الخلفيات من Wikimedia Commons"))
        }
    }

    suspend fun applyAsSystemWallpaper(wallpaper: WallpaperItem): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap: Bitmap = when {
                wallpaper.drawableRes != null -> {
                    BitmapFactory.decodeResource(context.resources, wallpaper.drawableRes)
                        ?: error("تعذر قراءة الصورة المدمجة")
                }
                !wallpaper.localUri.isNullOrBlank() -> {
                    val uri = Uri.parse(wallpaper.localUri)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    } ?: error("تعذر قراءة الصورة المختارة")
                }
                !wallpaper.imageUrl.isNullOrBlank() -> {
                    val request = ImageRequest.Builder(context)
                        .data(wallpaper.imageUrl)
                        .allowHardware(false)
                        .build()
                    val result = Coil.imageLoader(context).execute(request)
                    if (result is SuccessResult) {
                        result.drawable.toBitmap()
                    } else {
                        error("تعذر تحميل الصورة من المصدر المفتوح")
                    }
                }
                else -> error("مصدر خلفية غير صالح")
            }

            val wallpaperManager = WallpaperManager.getInstance(context)
            wallpaperManager.setBitmap(bitmap)
        }
    }

    private fun stripHtml(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        return raw.replace(Regex("<[^>]*>"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
