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
    val mediaType: String // "movie" or "tv"
) {
    /** Full poster image URL */
    val posterUrl: String?
        get() = posterPath?.let { "${Constant.TMDB_IMG_BASE_URL}w500$it" }

    /** Full backdrop image URL */
    val backdropUrl: String?
        get() = backdropPath?.let { "${Constant.TMDB_IMG_BASE_URL}w780$it" }

    /** Year extracted from release date */
    val year: String?
        get() = releaseDate?.takeIf { it.length >= 4 }?.substring(0, 4)
}

/**
 * Result type for TMDB API calls
 */
sealed class TmdbResult {
    data object Loading : TmdbResult()
    data class Success(
        val movies: List<TmdbItem>,
        val tvShows: List<TmdbItem>
    ) : TmdbResult()
    data class Error(val message: String) : TmdbResult()
}

/**
 * Repository for accessing TMDB (The Movie Database) API
 * Provides trending movies and TV shows data
 */
@Singleton
class TmdbRepository @Inject constructor() {

    /**
     * Load trending movies and TV shows in parallel
     */
    fun loadTrendingAll(): Flow<TmdbResult> = flow {
        emit(TmdbResult.Loading)
        try {
            coroutineScope {
                val moviesDeferred = async { fetchTrending("movie") }
                val tvDeferred = async { fetchTrending("tv") }

                val movies = moviesDeferred.await()
                val tvShows = tvDeferred.await()

                emit(TmdbResult.Success(movies = movies, tvShows = tvShows))
            }
        } catch (e: Exception) {
            emit(TmdbResult.Error(e.message ?: "Failed to load TMDB content"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Fetch trending items from TMDB API
     * @param mediaType "movie" or "tv"
     */
    private fun fetchTrending(mediaType: String): List<TmdbItem> {
        val url = buildTmdbUrl("trending/$mediaType/week") ?: return emptyList()

        val response = OkHttp.newCall(url).execute()
        if (!response.isSuccessful || response.body == null) return emptyList()

        val json = response.body!!.string()
        return parseTmdbResults(json, mediaType)
    }

    /**
     * Build TMDB API URL with required parameters
     */
    private fun buildTmdbUrl(path: String, extraParams: Map<String, String> = emptyMap()): String? {
        val builder = "${Constant.TMDB_API_BASE_URL}$path".toHttpUrlOrNull()?.newBuilder()
            ?: return null
        builder.addQueryParameter("api_key", Constant.TMDB_API_KEY)
        builder.addQueryParameter("language", "zh-CN")
        extraParams.forEach { (key, value) -> builder.addQueryParameter(key, value) }
        return builder.build().toString()
    }

    /**
     * Parse TMDB API response JSON into list of TmdbItem
     */
    private fun parseTmdbResults(json: String, defaultMediaType: String): List<TmdbItem> {
        return try {
            val root = JSONObject(json)
            val results = root.optJSONArray("results") ?: return emptyList()
            val items = mutableListOf<TmdbItem>()

            for (i in 0 until results.length()) {
                val obj = results.optJSONObject(i) ?: continue
                val mediaType = obj.optString("media_type", defaultMediaType)
                val isMovie = mediaType == "movie"

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
                        mediaType = mediaType
                    )
                )
            }
            items
        } catch (e: Exception) {
            emptyList()
        }
    }
}
