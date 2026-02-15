package com.fongmi.android.tv.data.repository

import com.github.catvod.net.OkHttp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Danmu match result from CF Worker
 */
data class DanmuMatchResult(
    val success: Boolean,
    val danmuUrl: String = "",
    val matchInfo: String = "",
    val error: String = ""
)

/**
 * Danmu search anime result
 */
data class DanmuSearchAnime(
    val animeId: Int,
    val animeTitle: String,
    val type: String = "",
    val episodes: List<DanmuSearchEpisode> = emptyList()
)

/**
 * Danmu search episode result
 */
data class DanmuSearchEpisode(
    val episodeId: Int,
    val episodeTitle: String
)

/**
 * Search result state
 */
sealed class DanmuSearchState {
    data object Idle : DanmuSearchState()
    data object Loading : DanmuSearchState()
    data class Success(val animeList: List<DanmuSearchAnime>) : DanmuSearchState()
    data class Error(val message: String) : DanmuSearchState()
}

/**
 * Episodes result state
 */
sealed class DanmuEpisodesState {
    data object Idle : DanmuEpisodesState()
    data object Loading : DanmuEpisodesState()
    data class Success(val episodes: List<DanmuSearchEpisode>) : DanmuEpisodesState()
    data class Error(val message: String) : DanmuEpisodesState()
}

/**
 * Repository for danmu (barrage) matching via CF Worker
 */
@Singleton
class DanmuRepository @Inject constructor() {

    companion object {
        private const val WORKER_BASE = "https://aiapi.114514heihei.eu.org"
        private const val DANMU_API_BASE = "https://d2.114514heihei.eu.org/mapiwbhpass"
    }

    /**
     * Auto-match danmu via CF Worker (AI-powered)
     */
    suspend fun matchDanmu(title: String, episode: String): DanmuMatchResult = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("title", title)
                put("episode", episode)
            }

            val requestBody = RequestBody.create(
                MediaType.get("application/json; charset=utf-8"),
                jsonBody.toString()
            )
            val headers = Headers.of("Content-Type", "application/json")
            val response = OkHttp.newCall(
                WORKER_BASE + "/api/danmu/match",
                headers,
                requestBody
            ).execute()

            val json = JSONObject(response.body()?.string() ?: "{}")

            if (json.optBoolean("success")) {
                DanmuMatchResult(
                    success = true,
                    danmuUrl = json.optString("danmuUrl", ""),
                    matchInfo = json.optJSONObject("matchInfo")?.let {
                        "${it.optString("animeTitle")} - ${it.optString("episodeTitle")}"
                    } ?: ""
                )
            } else {
                DanmuMatchResult(
                    success = false,
                    error = json.optString("error", "match failed")
                )
            }
        } catch (e: Exception) {
            DanmuMatchResult(success = false, error = e.message ?: "network error")
        }
    }

    /**
     * Search anime by keyword (for manual search)
     * Calls danmu_api directly through CF Worker
     */
    fun searchDanmu(keyword: String): Flow<DanmuSearchState> = flow {
        emit(DanmuSearchState.Loading)
        try {
            val url = "$WORKER_BASE/api/danmu/search?keyword=${java.net.URLEncoder.encode(keyword, "UTF-8")}"
            val response = OkHttp.string(url)
            val json = JSONObject(response)

            if (json.optBoolean("success")) {
                val data = json.optJSONObject("data")
                val animes = data?.optJSONArray("animes") ?: run {
                    emit(DanmuSearchState.Success(emptyList()))
                    return@flow
                }

                val list = mutableListOf<DanmuSearchAnime>()
                for (i in 0 until animes.length()) {
                    val anime = animes.getJSONObject(i)
                    list.add(
                        DanmuSearchAnime(
                            animeId = anime.optInt("animeId"),
                            animeTitle = anime.optString("animeTitle", ""),
                            type = anime.optString("type", "")
                        )
                    )
                }
                emit(DanmuSearchState.Success(list))
            } else {
                emit(DanmuSearchState.Error(json.optString("error", "search failed")))
            }
        } catch (e: Exception) {
            emit(DanmuSearchState.Error(e.message ?: "network error"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get episodes for a specific anime (for manual selection)
     */
    fun getEpisodes(animeId: Int): Flow<DanmuEpisodesState> = flow {
        emit(DanmuEpisodesState.Loading)
        try {
            val url = "$WORKER_BASE/api/danmu/bangumi/$animeId"
            val response = OkHttp.string(url)
            val json = JSONObject(response)

            if (!json.optBoolean("success")) {
                emit(DanmuEpisodesState.Error(json.optString("error", "load failed")))
                return@flow
            }

            val data = json.optJSONObject("data")
            val bangumi = data?.optJSONObject("bangumi")
            val episodes = bangumi?.optJSONArray("episodes") ?: run {
                emit(DanmuEpisodesState.Success(emptyList()))
                return@flow
            }

            val list = mutableListOf<DanmuSearchEpisode>()
            for (i in 0 until episodes.length()) {
                val ep = episodes.getJSONObject(i)
                list.add(
                    DanmuSearchEpisode(
                        episodeId = ep.optInt("episodeId"),
                        episodeTitle = ep.optString("episodeTitle", "")
                    )
                )
            }
            emit(DanmuEpisodesState.Success(list))
        } catch (e: Exception) {
            emit(DanmuEpisodesState.Error(e.message ?: "network error"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Build danmu XML URL from episodeId
     */
    fun buildDanmuUrl(episodeId: Int): String {
        return "$DANMU_API_BASE/api/v2/comment/$episodeId?format=xml"
    }
}
