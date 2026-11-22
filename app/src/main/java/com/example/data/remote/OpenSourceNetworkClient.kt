package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class WikimediaResponse(
    val query: WikimediaQuery? = null
)

data class WikimediaQuery(
    val pages: Map<String, WikimediaPage>? = null
)

data class WikimediaPage(
    val pageid: Long? = null,
    val title: String? = null,
    val imageinfo: List<WikimediaImageInfo>? = null
)

data class WikimediaImageInfo(
    val url: String? = null,
    val thumburl: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val extmetadata: Map<String, WikimediaMetaField>? = null
)

data class WikimediaMetaField(
    val value: String? = null
)

data class FDroidPackageResponse(
    val packageName: String? = null,
    val suggestedVersionCode: Long? = null,
    val packages: List<FDroidVersionEntry>? = null
)

data class FDroidVersionEntry(
    val versionName: String? = null,
    val versionCode: Long? = null
)

data class FDroidSearchResponse(
    val apps: List<FDroidSearchApp>? = null
)

data class FDroidSearchApp(
    val name: String? = null,
    val summary: String? = null,
    val icon: String? = null,
    val url: String? = null
)

interface WikimediaCommonsApi {
    @GET("w/api.php")
    suspend fun getFeaturedCategoryImages(
        @Query("action") action: String = "query",
        @Query("generator") generator: String = "categorymembers",
        @Query("gcmtitle") categoryTitle: String,
        @Query("gcmtype") gcmType: String = "file",
        @Query("gcmlimit") limit: Int = 14,
        @Query("prop") prop: String = "imageinfo",
        @Query("iiprop") iiProp: String = "url|size|extmetadata",
        @Query("iiurlwidth") thumbWidth: Int = 1440,
        @Query("format") format: String = "json"
    ): WikimediaResponse
}

interface FDroidPackagesApi {
    @GET("api/v1/packages/{packageName}")
    suspend fun getPackageInfo(
        @Path("packageName") packageName: String
    ): FDroidPackageResponse
}

interface FDroidSearchApi {
    @GET("api/search_apps")
    suspend fun searchApps(
        @Query("q") query: String
    ): FDroidSearchResponse
}

object OpenSourceNetworkClient {
    private val userAgentInterceptor = Interceptor { chain ->
        val req = chain.request().newBuilder()
            .header("User-Agent", "MadarLauncher/1.0 (Android Open-Source Screen Launcher)")
            .build()
        chain.proceed(req)
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(userAgentInterceptor)
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val wikimediaApi: WikimediaCommonsApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://commons.wikimedia.org/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(WikimediaCommonsApi::class.java)
    }

    val fdroidPackagesApi: FDroidPackagesApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://f-droid.org/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(FDroidPackagesApi::class.java)
    }

    val fdroidSearchApi: FDroidSearchApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://search.f-droid.org/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(FDroidSearchApi::class.java)
    }
}
