package com.fongmi.android.tv.data.repository

import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.bean.Class
import com.fongmi.android.tv.bean.Result
import com.fongmi.android.tv.bean.Site
import com.fongmi.android.tv.bean.Sub
import com.fongmi.android.tv.bean.Vod
import com.fongmi.android.tv.impl.ParseCallback
import com.fongmi.android.tv.player.ParseJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Repository for VOD (Video on Demand) data access
 * Provides a clean API for ViewModel to access video content
 */
@Singleton
class VodRepository @Inject constructor() {

    private val vodConfig: VodConfig get() = VodConfig.get()

    /**
     * Get all configured sites
     */
    fun getSites(): List<Site> = vodConfig.sites ?: emptyList()

    /**
     * Get current home site
     */
    fun getHomeSite(): Site? = vodConfig.home

    /**
     * Set home site
     */
    fun setHomeSite(site: Site) {
        vodConfig.setHome(site)
    }

    /**
     * Load home content for a site
     * Returns categories and featured content
     */
    fun loadHomeContent(site: Site? = null): Flow<HomeContentResult> = flow {
        emit(HomeContentResult.Loading)
        try {
            val targetSite = site ?: vodConfig.home ?: return@flow
            val spider = targetSite.recent().spider()

            // Get home content from spider
            val result = spider?.homeContent(false)
            if (result != null) {
                val parsed = Result.fromJson(result)
                val featured = parsed.list ?: emptyList()
                emit(HomeContentResult.Success(
                    categories = parsed.types ?: emptyList(),
                    featured = featured
                ))
            } else {
                emit(HomeContentResult.Error("Failed to load content"))
            }
        } catch (e: Exception) {
            emit(HomeContentResult.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Load category content
     */
    fun loadCategoryContent(
        site: Site,
        categoryId: String,
        page: Int = 1,
        extend: HashMap<String, String> = HashMap()
    ): Flow<CategoryContentResult> = flow {
        emit(CategoryContentResult.Loading)
        try {
            val spider = site.recent().spider()

            val result = spider?.categoryContent(categoryId, page.toString(), false, extend)
            if (result != null) {
                val parsed = Result.fromJson(result)
                val items = parsed.list ?: emptyList()
                emit(CategoryContentResult.Success(
                    content = items,
                    pageCount = parsed.pageCount ?: 1,
                    currentPage = page
                ))
            } else {
                emit(CategoryContentResult.Error("Failed to load category"))
            }
        } catch (e: Exception) {
            emit(CategoryContentResult.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Load video detail
     */
    fun loadDetail(site: Site, vodId: String): Flow<DetailResult> = flow {
        emit(DetailResult.Loading)
        try {
            val spider = site.recent().spider()
            val ids = listOf(vodId)

            val result = spider?.detailContent(ids)

            if (result != null) {
                val parsed = Result.fromJson(result)
                val vod = parsed.list?.firstOrNull()
                if (vod != null) {
                    // Set vod flags from play info
                    vod.setVodFlags()
                    vod.site = site
                    emit(DetailResult.Success(vod))
                } else {
                    emit(DetailResult.Error("Video not found"))
                }
            } else {
                emit(DetailResult.Error("Failed to load detail"))
            }
        } catch (e: Exception) {
            emit(DetailResult.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Search content
     */
    fun search(keyword: String, page: Int = 1): Flow<SearchResult> = flow {
        emit(SearchResult.Loading)
        try {
            val site = vodConfig.home ?: return@flow
            val spider = site.recent().spider()

            val result = spider?.searchContent(keyword, false, page.toString())
            if (result != null) {
                val parsed = Result.fromJson(result)
                emit(SearchResult.Success(
                    results = parsed.list ?: emptyList(),
                    pageCount = parsed.pageCount ?: 1,
                    currentPage = page
                ))
            } else {
                emit(SearchResult.Error("Search failed"))
            }
        } catch (e: Exception) {
            emit(SearchResult.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Multi-site aggregated search - streams results as each site completes
     */
    fun searchMultiSite(keyword: String, sites: List<Site>? = null): Flow<AggregatedSearchResult> = flow {
        emit(AggregatedSearchResult.Loading)
        try {
            val searchSites = sites?.filter { it.searchable == 1 }
                ?: vodConfig.sites?.filter { it.searchable == 1 }
                ?: emptyList()

            if (searchSites.isEmpty()) {
                emit(AggregatedSearchResult.Error("No searchable sites"))
                return@flow
            }

            val collectedResults = java.util.concurrent.CopyOnWriteArrayList<SiteSearchResult>()
            val resultChannel = kotlinx.coroutines.channels.Channel<SiteSearchResult>(kotlinx.coroutines.channels.Channel.UNLIMITED)

            coroutineScope {
                // Launch all site searches concurrently
                val jobs = searchSites.map { site ->
                    launch {
                        val siteResult = try {
                            val spider = site.recent().spider()
                            val result = spider?.searchContent(keyword, false, "1")
                            if (result != null) {
                                val parsed = Result.fromJson(result)
                                val vods = parsed.list ?: emptyList()
                                vods.forEach { it.site = site }
                                SiteSearchResult(site, vods, null)
                            } else {
                                SiteSearchResult(site, emptyList(), "No results")
                            }
                        } catch (e: Exception) {
                            SiteSearchResult(site, emptyList(), e.message)
                        }
                        resultChannel.send(siteResult)
                    }
                }

                // Collect results as they arrive
                launch {
                    var received = 0
                    for (siteResult in resultChannel) {
                        collectedResults.add(siteResult)
                        received++
                        // Emit partial results (still searching)
                        emit(AggregatedSearchResult.Partial(collectedResults.toList(), received < searchSites.size))
                        if (received >= searchSites.size) {
                            resultChannel.close()
                        }
                    }
                }

                // Wait for all to finish
                jobs.forEach { it.join() }
            }

            emit(AggregatedSearchResult.Success(collectedResults.toList()))
        } catch (e: Exception) {
            emit(AggregatedSearchResult.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get searchable sites
     */
    fun getSearchableSites(): List<Site> {
        return vodConfig.sites?.filter { it.searchable == 1 } ?: emptyList()
    }

    /**
     * Quick search across all searchable sites, return the first valid result
     * Used for TMDB poster click -> search -> play flow
     */
    fun searchFirstResult(keyword: String): Flow<FirstSearchResult> = flow {
        emit(FirstSearchResult.Loading)
        try {
            val searchSites = vodConfig.sites?.filter { it.searchable == 1 } ?: emptyList()
            if (searchSites.isEmpty()) {
                emit(FirstSearchResult.NotFound(keyword))
                return@flow
            }

            var found = false
            coroutineScope {
                val deferredResults = searchSites.map { site ->
                    async {
                        try {
                            val spider = site.recent().spider()
                            val result = spider?.searchContent(keyword, false, "1")
                            if (result != null) {
                                val parsed = Result.fromJson(result)
                                val vod = parsed.list?.firstOrNull()
                                if (vod != null) {
                                    Triple(site.key, vod.vodId ?: "", vod)
                                } else null
                            } else null
                        } catch (e: Exception) {
                            null
                        }
                    }
                }

                for (deferred in deferredResults) {
                    val result = deferred.await()
                    if (result != null && result.second.isNotEmpty() && !found) {
                        found = true
                        emit(FirstSearchResult.Success(
                            siteKey = result.first,
                            vodId = result.second,
                            vodName = result.third.vodName ?: keyword,
                            vodPic = result.third.vodPic
                        ))
                        // Cancel remaining
                        deferredResults.forEach { it.cancel() }
                        return@coroutineScope
                    }
                }
            }

            if (!found) {
                emit(FirstSearchResult.NotFound(keyword))
            }
        } catch (e: Exception) {
            emit(FirstSearchResult.Error(e.message ?: "Search failed"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get play URL for an episode
     * Returns needParse=true if the URL requires web parsing (jx=1 or parse=1)
     */
    fun getPlayUrl(site: Site, flag: String, episodeId: String): Flow<PlayUrlResult> = flow {
        emit(PlayUrlResult.Loading)
        try {
            val spider = site.recent().spider()
            val result = spider?.playerContent(flag, episodeId, vodConfig.flags)

            if (result != null) {
                val parsed = Result.fromJson(result)
                val url = parsed.url?.v() ?: parsed.playUrl

                if (!url.isNullOrEmpty()) {
                    // Check if parsing is needed:
                    // 1. jx == 1 (explicit request to parse)
                    // 2. playUrl is empty AND flag is in configured parse flags
                    val hasParsers = vodConfig.parses?.isNotEmpty() == true
                    val needParse = hasParsers && (
                        parsed.jx == 1 ||
                        (parsed.playUrl.isNullOrEmpty() && vodConfig.flags?.contains(flag) == true)
                    )

                    emit(PlayUrlResult.Success(
                        url = url,
                        headers = parsed.headers,
                        subtitles = parsed.subs ?: emptyList(),
                        needParse = needParse,
                        parseFlag = if (needParse) parsed.flag else null
                    ))
                } else {
                    emit(PlayUrlResult.Error("No play URL found"))
                }
            } else {
                emit(PlayUrlResult.Error("Failed to get play URL"))
            }
        } catch (e: Exception) {
            emit(PlayUrlResult.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Parse a URL that requires web parsing (jx/parse)
     * Uses ParseJob to sniff/parse the actual video URL
     */
    fun parseUrl(
        siteKey: String,
        url: String,
        flag: String
    ): Flow<ParseUrlResult> = flow {
        emit(ParseUrlResult.Loading)
        try {
            // Create Result object for ParseJob
            val result = Result.empty().apply {
                setUrl(url)
                setFlag(flag)
                key = siteKey
            }

            // Use suspendCancellableCoroutine to bridge callback to coroutine
            val parseResult = suspendCancellableCoroutine { continuation ->
                var parseJob: ParseJob? = null

                parseJob = ParseJob.create(object : ParseCallback {
                    override fun onParseSuccess(headers: MutableMap<String, String>?, parsedUrl: String?, from: String?) {
                        if (continuation.isActive) {
                            if (!parsedUrl.isNullOrEmpty()) {
                                continuation.resume(ParseUrlResult.Success(
                                    url = parsedUrl,
                                    headers = headers,
                                    from = from
                                ))
                            } else {
                                continuation.resume(ParseUrlResult.Error("Parse returned empty URL"))
                            }
                        }
                    }

                    override fun onParseError() {
                        if (continuation.isActive) {
                            continuation.resume(ParseUrlResult.Error("Parse failed"))
                        }
                    }
                })

                // Start parsing (useParse=true to force parsing)
                parseJob.start(result, true)

                // Cancel ParseJob if coroutine is cancelled
                continuation.invokeOnCancellation {
                    parseJob.stop()
                }
            }

            emit(parseResult)
        } catch (e: Exception) {
            emit(ParseUrlResult.Error(e.message ?: "Parse error"))
        }
    }.flowOn(Dispatchers.Main) // ParseJob uses WebView which needs main thread
}

// Result sealed classes for type-safe results
sealed class HomeContentResult {
    data object Loading : HomeContentResult()
    data class Success(val categories: List<Class>, val featured: List<Vod>) : HomeContentResult()
    data class Error(val message: String) : HomeContentResult()
}

sealed class CategoryContentResult {
    data object Loading : CategoryContentResult()
    data class Success(val content: List<Vod>, val pageCount: Int, val currentPage: Int) : CategoryContentResult()
    data class Error(val message: String) : CategoryContentResult()
}

sealed class DetailResult {
    data object Loading : DetailResult()
    data class Success(val vod: Vod) : DetailResult()
    data class Error(val message: String) : DetailResult()
}

sealed class SearchResult {
    data object Loading : SearchResult()
    data class Success(val results: List<Vod>, val pageCount: Int, val currentPage: Int) : SearchResult()
    data class Error(val message: String) : SearchResult()
}

sealed class PlayUrlResult {
    data object Loading : PlayUrlResult()
    data class Success(
        val url: String,
        val headers: Map<String, String>?,
        val subtitles: List<Sub>,
        val needParse: Boolean = false,  // Whether URL needs web parsing
        val parseFlag: String? = null    // Flag for parsing service selection
    ) : PlayUrlResult()
    data class Error(val message: String) : PlayUrlResult()
}

/**
 * Result for URL parsing (web sniffing/jx parsing)
 */
sealed class ParseUrlResult {
    data object Loading : ParseUrlResult()
    data class Success(
        val url: String,
        val headers: Map<String, String>?,
        val from: String?  // Parser name that succeeded
    ) : ParseUrlResult()
    data class Error(val message: String) : ParseUrlResult()
}

data class SiteSearchResult(
    val site: Site,
    val results: List<Vod>,
    val error: String?
)

sealed class AggregatedSearchResult {
    data object Loading : AggregatedSearchResult()
    data class Partial(val siteResults: List<SiteSearchResult>, val stillSearching: Boolean) : AggregatedSearchResult()
    data class Success(val siteResults: List<SiteSearchResult>) : AggregatedSearchResult()
    data class Error(val message: String) : AggregatedSearchResult()
}

/**
 * Result for quick search (first result only)
 */
sealed class FirstSearchResult {
    data object Loading : FirstSearchResult()
    data class Success(
        val siteKey: String,
        val vodId: String,
        val vodName: String,
        val vodPic: String?
    ) : FirstSearchResult()
    data class NotFound(val keyword: String) : FirstSearchResult()
    data class Error(val message: String) : FirstSearchResult()
}
