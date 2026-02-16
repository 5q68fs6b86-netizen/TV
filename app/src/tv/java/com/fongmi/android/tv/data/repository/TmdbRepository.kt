package com.fongmi.android.tv.data.repository

import com.fongmi.android.tv.Constant
import com.github.catvod.net.OkHttp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TMDB item data model
 */
data class TmdbItem(
    val id: Int,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val overview: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val mediaType: String, // "movie" or "tv"
    val genreIds: List<Int> = emptyList(),
    val logoPath: String? = null
) {
    val posterUrl: String?
        get() = posterPath?.let { "${Constant.TMDB_IMG_BASE_URL}w500$it" }

    val backdropUrl: String?
        get() = backdropPath?.let { "${Constant.TMDB_IMG_BASE_URL}w780$it" }

    /** Large backdrop for hero banner */
    val backdropUrlLarge: String?
        get() = backdropPath?.let { "${Constant.TMDB_IMG_BASE_URL}w1280$it" }

    /** Official title logo image URL */
    val logoUrl: String?
        get() = logoPath?.let { "${Constant.TMDB_IMG_BASE_URL}w500$it" }

    val year: String?
        get() = releaseDate?.takeIf { it.length >= 4 }?.substring(0, 4)

    /** Genre names resolved from genreIds */
    fun genreNames(genres: Map<Int, String>): List<String> =
        genreIds.mapNotNull { genres[it] }
}

data class TmdbProvider(
    val id: Int,
    val name: String,
    val logoPath: String?
) {
    val logoUrl: String?
        get() = logoPath?.let { "${Constant.TMDB_IMG_BASE_URL}${Constant.TMDB_LOGO_SIZE}$it" }
}

data class TmdbCompany(
    val id: Int,
    val name: String,
    val logoPath: String?
) {
    val logoUrl: String?
        get() = logoPath?.let { "${Constant.TMDB_IMG_BASE_URL}${Constant.TMDB_LOGO_SIZE}$it" }
}

/**
 * Result type for TMDB API calls
 */
sealed class TmdbResult {
    data object Loading : TmdbResult()
    data class Success(
        val trendingToday: List<TmdbItem> = emptyList(),
        val trendingWeek: List<TmdbItem> = emptyList(),
        val popularMovies: List<TmdbItem> = emptyList(),
        val popularTv: List<TmdbItem> = emptyList(),
        val nowPlaying: List<TmdbItem> = emptyList(),
        val trendingAnime: List<TmdbItem> = emptyList(),
        val topRatedMovies: List<TmdbItem> = emptyList(),
        val topRatedTv: List<TmdbItem> = emptyList(),
        val genres: Map<Int, String> = emptyMap(),
        val providers: List<TmdbProvider> = emptyList(),
        val companies: List<TmdbCompany> = emptyList(),
        val providerContent: Map<Int, List<TmdbItem>> = emptyMap(),
        val companyContent: Map<Int, List<TmdbItem>> = emptyMap(),
        val genreContent: Map<Int, List<TmdbItem>> = emptyMap()
    ) : TmdbResult()
    data class Error(val message: String) : TmdbResult()
}

// Well-known provider IDs for China region
private val FEATURED_PROVIDERS = listOf(
    TmdbProvider(8, "Netflix", null),
    TmdbProvider(337, "Disney Plus", null),
    TmdbProvider(350, "Apple TV Plus", null),
    TmdbProvider(1899, "Max", null),
    TmdbProvider(531, "Paramount Plus", null)
)

// Well-known company IDs
private val FEATURED_COMPANIES = listOf(
    TmdbCompany(33, "Universal Pictures", null),
    TmdbCompany(4, "Paramount Pictures", null),
    TmdbCompany(5, "Columbia Pictures", null),
    TmdbCompany(420, "Marvel Studios", null),
    TmdbCompany(174, "Warner Bros. Pictures", null),
    TmdbCompany(2, "Walt Disney Pictures", null)
)

// Genre IDs for browse section
private val BROWSE_GENRES = listOf(
    10751, // Family
    16,    // Animation
    10762, // Kids (TV)
    28,    // Action
    35,    // Comedy
    27,    // Horror
    878,   // Science Fiction
    10749  // Romance
)

/**
 * Repository for accessing TMDB (The Movie Database) API
 * Provides comprehensive content for the home screen
 */
@Singleton
class TmdbRepository @Inject constructor() {

    /**
     * Load all TMDB content in parallel for the home screen
     */
    fun loadAllContent(): Flow<TmdbResult> = flow {
        emit(TmdbResult.Loading)
        try {
            coroutineScope {
                // Phase 1: Core content (parallel)
                val trendingTodayDef = async { fetchList("trending/all/day") }
                val trendingWeekDef = async { fetchList("trending/all/week") }
                val popularMoviesDef = async { fetchList("movie/popular", defaultType = "movie") }
                val popularTvDef = async { fetchList("tv/popular", defaultType = "tv") }
                val nowPlayingDef = async { fetchList("movie/now_playing", defaultType = "movie") }
                val animeDef = async {
                    fetchList("discover/tv", defaultType = "tv", extra = mapOf(
                        "with_genres" to "16",
                        "sort_by" to "popularity.desc"
                    ))
                }
                val topMoviesDef = async { fetchList("movie/top_rated", defaultType = "movie") }
                val topTvDef = async { fetchList("tv/top_rated", defaultType = "tv") }
                val genreMovieDef = async { fetchGenres("genre/movie/list") }
                val genreTvDef = async { fetchGenres("genre/tv/list") }

                // Phase 1 results
                val trendingToday = trendingTodayDef.await()
                val trendingWeek = trendingWeekDef.await()
                val popularMovies = popularMoviesDef.await()
                val popularTv = popularTvDef.await()
                val nowPlaying = nowPlayingDef.await()
                val anime = animeDef.await()
                val topMovies = topMoviesDef.await()
                val topTv = topTvDef.await()
                val genreMovie = genreMovieDef.await()
                val genreTv = genreTvDef.await()
                val genres = (genreMovie + genreTv).toMap()

                // Phase 1.5: Fetch logos for hero banner items (top 10 trending today)
                val heroItems = trendingToday.take(10)
                val logoDefs = heroItems.map { item ->
                    item.id to async { fetchLogo(item.id, item.mediaType) }
                }
                val logoMap = mutableMapOf<Int, String>()
                logoDefs.forEach { (id, def) ->
                    def.await()?.let { logoMap[id] = it }
                }
                val trendingTodayWithLogos = trendingToday.mapIndexed { index, item ->
                    if (index < 10) item.copy(logoPath = logoMap[item.id]) else item
                }

                // Phase 2: Provider & Company content (parallel)
                val providerContentMap = mutableMapOf<Int, List<TmdbItem>>()
                val companyContentMap = mutableMapOf<Int, List<TmdbItem>>()
                val genreContentMap = mutableMapOf<Int, List<TmdbItem>>()

                val providerDefs = FEATURED_PROVIDERS.map { provider ->
                    provider.id to async {
                        fetchList("discover/movie", defaultType = "movie", extra = mapOf(
                            "with_watch_providers" to provider.id.toString(),
                            "watch_region" to "US",
                            "sort_by" to "popularity.desc"
                        ))
                    }
                }

                val companyDefs = FEATURED_COMPANIES.map { company ->
                    company.id to async {
                        fetchList("discover/movie", defaultType = "movie", extra = mapOf(
                            "with_companies" to company.id.toString(),
                            "sort_by" to "popularity.desc"
                        ))
                    }
                }

                val genreDefs = BROWSE_GENRES.map { genreId ->
                    genreId to async {
                        fetchList("discover/movie", defaultType = "movie", extra = mapOf(
                            "with_genres" to genreId.toString(),
                            "sort_by" to "popularity.desc"
                        ))
                    }
                }

                // Collect phase 2 results
                providerDefs.forEach { (id, def) ->
                    val items = def.await()
                    if (items.isNotEmpty()) providerContentMap[id] = items
                }
                companyDefs.forEach { (id, def) ->
                    val items = def.await()
                    if (items.isNotEmpty()) companyContentMap[id] = items
                }
                genreDefs.forEach { (id, def) ->
                    val items = def.await()
                    if (items.isNotEmpty()) genreContentMap[id] = items
                }

                // Resolve provider logos from discover results or use static fallbacks
                val resolvedProviders = FEATURED_PROVIDERS.mapNotNull { provider ->
                    val content = providerContentMap[provider.id]
                    if (content.isNullOrEmpty()) null else provider
                }

                val resolvedCompanies = FEATURED_COMPANIES.mapNotNull { company ->
                    val content = companyContentMap[company.id]
                    if (content.isNullOrEmpty()) null else company
                }

                emit(TmdbResult.Success(
                    trendingToday = trendingTodayWithLogos,
                    trendingWeek = trendingWeek,
                    popularMovies = popularMovies,
                    popularTv = popularTv,
                    nowPlaying = nowPlaying,
                    trendingAnime = anime,
                    topRatedMovies = topMovies,
                    topRatedTv = topTv,
                    genres = genres,
                    providers = resolvedProviders,
                    companies = resolvedCompanies,
                    providerContent = providerContentMap,
                    companyContent = companyContentMap,
                    genreContent = genreContentMap
                ))
            }
        } catch (e: Exception) {
            emit(TmdbResult.Error(e.message ?: "Failed to load TMDB content"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Legacy method for backward compatibility
     */
    fun loadTrendingAll(): Flow<TmdbResult> = loadAllContent()

    /**
     * Generic list fetch from TMDB API
     */
    private fun fetchList(
        path: String,
        defaultType: String = "movie",
        extra: Map<String, String> = emptyMap()
    ): List<TmdbItem> {
        val url = buildTmdbUrl(path, extra) ?: return emptyList()
        val response = OkHttp.newCall(url).execute()
        if (!response.isSuccessful || response.body == null) return emptyList()
        return parseTmdbResults(response.body!!.string(), defaultType)
    }

    /**
     * Fetch logo image path for a movie/tv item.
     * Prefers zh-CN logo, falls back to en, then null-language logos.
     */
    private fun fetchLogo(itemId: Int, mediaType: String): String? {
        val type = if (mediaType == "movie") "movie" else "tv"
        val url = buildTmdbUrl("$type/$itemId/images", mapOf(
            "include_image_language" to "zh,en,null"
        )) ?: return null
        return try {
            val response = OkHttp.newCall(url).execute()
            if (!response.isSuccessful || response.body == null) return null
            val root = JSONObject(response.body!!.string())
            val logos = root.optJSONArray("logos") ?: return null
            // Prefer zh logo, then en, then any
            var zhLogo: String? = null
            var enLogo: String? = null
            var anyLogo: String? = null
            for (i in 0 until logos.length()) {
                val obj = logos.optJSONObject(i) ?: continue
                val path = obj.optString("file_path", null)?.takeUnless { it == "null" } ?: continue
                val lang = obj.optString("iso_639_1", "")
                when (lang) {
                    "zh" -> if (zhLogo == null) zhLogo = path
                    "en" -> if (enLogo == null) enLogo = path
                    else -> if (anyLogo == null) anyLogo = path
                }
            }
            zhLogo ?: enLogo ?: anyLogo
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Fetch genre list from TMDB
     */
    private fun fetchGenres(path: String): List<Pair<Int, String>> {
        val url = buildTmdbUrl(path) ?: return emptyList()
        val response = OkHttp.newCall(url).execute()
        if (!response.isSuccessful || response.body == null) return emptyList()
        return try {
            val root = JSONObject(response.body!!.string())
            val arr = root.optJSONArray("genres") ?: return emptyList()
            (0 until arr.length()).mapNotNull { i ->
                val obj = arr.optJSONObject(i) ?: return@mapNotNull null
                obj.optInt("id") to obj.optString("name", "")
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun buildTmdbUrl(path: String, extraParams: Map<String, String> = emptyMap()): String? {
        val builder = "${Constant.TMDB_API_BASE_URL}$path".toHttpUrlOrNull()?.newBuilder()
            ?: return null
        builder.addQueryParameter("api_key", Constant.TMDB_API_KEY)
        builder.addQueryParameter("language", "zh-CN")
        extraParams.forEach { (key, value) -> builder.addQueryParameter(key, value) }
        return builder.build().toString()
    }

    private fun parseTmdbResults(json: String, defaultMediaType: String): List<TmdbItem> {
        return try {
            val root = JSONObject(json)
            val results = root.optJSONArray("results") ?: return emptyList()
            val items = mutableListOf<TmdbItem>()

            for (i in 0 until results.length()) {
                val obj = results.optJSONObject(i) ?: continue
                val mediaType = obj.optString("media_type", defaultMediaType)
                val isMovie = mediaType == "movie"

                val genreIds = mutableListOf<Int>()
                obj.optJSONArray("genre_ids")?.let { arr ->
                    for (j in 0 until arr.length()) genreIds.add(arr.optInt(j))
                }

                items.add(
                    TmdbItem(
                        id = obj.optInt("id", -1),
                        title = if (isMovie) obj.optString("title", "") else obj.optString("name", ""),
                        posterPath = obj.optString("poster_path", null).takeUnless { it == "null" },
                        backdropPath = obj.optString("backdrop_path", null).takeUnless { it == "null" },
                        overview = obj.optString("overview", null).takeUnless { it.isNullOrBlank() },
                        releaseDate = if (isMovie) obj.optString("release_date", null)
                                      else obj.optString("first_air_date", null),
                        voteAverage = obj.optDouble("vote_average", 0.0),
                        mediaType = mediaType,
                        genreIds = genreIds
                    )
                )
            }
            items
        } catch (e: Exception) {
            emptyList()
        }
    }
}
