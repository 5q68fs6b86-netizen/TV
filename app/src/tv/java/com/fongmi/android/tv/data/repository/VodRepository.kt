package com.fongmi.android.tv.data.repository

import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.bean.Class
import com.fongmi.android.tv.bean.Result
import com.fongmi.android.tv.bean.Site
import com.fongmi.android.tv.bean.Sub
import com.fongmi.android.tv.bean.Vod
import com.fongmi.android.tv.impl.ParseCallback
import com.fongmi.android.tv.player.ParseJob
import com.github.catvod.Proxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import android.app.Activity
import java.io.File
import java.lang.reflect.Field
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
            dumpProxyDiagnostics("BEFORE homeContent site=${targetSite.key} type=${targetSite.type}")
            val spider = targetSite.recent().spider()

            // Get home content from spider
            val result = spider?.homeContent(false)
            System.out.println("TV_Proxy_Debug: homeContent returned, length=${result?.length ?: -1}")
            dumpProxyDiagnostics("AFTER homeContent site=${targetSite.key}")
            if (result != null) {
                val parsed = Result.fromJson(result)
                emit(HomeContentResult.Success(
                    categories = parsed.types ?: emptyList(),
                    featured = parsed.list ?: emptyList()
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
                emit(CategoryContentResult.Success(
                    content = parsed.list ?: emptyList(),
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
     * Simulate JAR plugin's Init.getActivity() reflection to diagnose if it works
     */
    private fun testReflectiveGetActivity(label: String) {
        try {
            val tag = "TV_Dialog_Debug"
            val activityThreadClass = java.lang.Class.forName("android.app.ActivityThread")
            val currentActivityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null)
            val activitiesField = activityThreadClass.getDeclaredField("mActivities")
            activitiesField.isAccessible = true
            val activities = activitiesField.get(currentActivityThread) as? Map<*, *>
            System.out.println("$tag: [$label] mActivities count=${activities?.size ?: -1}")
            if (activities != null) {
                for ((key, activityRecord) in activities) {
                    val recordClass = activityRecord!!.javaClass
                    val pausedField = recordClass.getDeclaredField("paused")
                    pausedField.isAccessible = true
                    val paused = pausedField.getBoolean(activityRecord)
                    val activityField = recordClass.getDeclaredField("activity")
                    activityField.isAccessible = true
                    val activity = activityField.get(activityRecord) as? Activity
                    System.out.println("$tag: [$label] ActivityRecord key=$key paused=$paused activity=${activity?.javaClass?.simpleName ?: "null"}")
                }
            }
            // Also log App.activity() for comparison
            val appActivity = com.fongmi.android.tv.App.activity()
            System.out.println("$tag: [$label] App.activity()=${appActivity?.javaClass?.simpleName ?: "null"}")
        } catch (e: Exception) {
            System.out.println("TV_Dialog_Debug: [$label] reflective getActivity FAILED: ${e.javaClass.simpleName}: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Dump diagnostic info about proxy and go proxy state
     */
    private fun dumpProxyDiagnostics(label: String) {
        try {
            val tag = "TV_Proxy_Debug"
            System.out.println("$tag: === $label ===")
            System.out.println("$tag: Proxy.getPort()=${Proxy.getPort()}")
            System.out.println("$tag: Proxy.getUrl(true)=${Proxy.getUrl(true)}")
            System.out.println("$tag: Thread=${Thread.currentThread().name}")

            // Check cache directory for go proxy files
            val cacheDir = com.github.catvod.Init.context()?.cacheDir
            System.out.println("$tag: cacheDir=${cacheDir?.absolutePath}")
            if (cacheDir != null) {
                val files = cacheDir.listFiles()
                System.out.println("$tag: cacheDir files: ${files?.map { it.name }?.joinToString()}")
                // Check for any go proxy or port files
                files?.filter { it.name.contains("go") || it.name.contains("proxy") || it.name.contains("port") }
                    ?.forEach { f ->
                        System.out.println("$tag: special file: ${f.name}, size=${f.length()}, canExec=${f.canExecute()}")
                        if (f.length() < 100 && f.isFile) {
                            try {
                                System.out.println("$tag:   content='${f.readText().trim()}'")
                            } catch (_: Exception) {}
                        }
                    }
            }

            // Check for running go proxy processes
            try {
                val proc = Runtime.getRuntime().exec("ps")
                val output = proc.inputStream.bufferedReader().readText()
                val goLines = output.lines().filter { it.contains("go_proxy") || it.contains("new_go") || it.contains("wex") }
                System.out.println("$tag: go proxy processes: ${goLines.size}")
                goLines.forEach { System.out.println("$tag:   $it") }
            } catch (e: Exception) {
                System.out.println("$tag: ps failed: ${e.message}")
            }
        } catch (e: Exception) {
            System.out.println("TV_Proxy_Debug: diagnostics error: ${e.message}")
        }
    }

    /**
     * Load video detail
     */
    fun loadDetail(site: Site, vodId: String): Flow<DetailResult> = flow {
        emit(DetailResult.Loading)
        try {
            dumpProxyDiagnostics("BEFORE detailContent vodId=$vodId")
            // Simulate JAR plugin's Init.getActivity() reflection to diagnose
            testReflectiveGetActivity("BEFORE detailContent")
            val spider = site.recent().spider()
            val ids = listOf(vodId)

            val result = spider?.detailContent(ids)
            System.out.println("TV_Proxy_Debug: detailContent returned, length=${result?.length ?: -1}")
            // Test again after detailContent (dialog should have been posted by now)
            testReflectiveGetActivity("AFTER detailContent")
            dumpProxyDiagnostics("AFTER detailContent vodId=$vodId")

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
     * Multi-site aggregated search
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

            val allResults = mutableListOf<SiteSearchResult>()

            coroutineScope {
                val deferredResults = searchSites.map { site ->
                    async {
                        try {
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
                    }
                }
                allResults.addAll(deferredResults.awaitAll())
            }

            emit(AggregatedSearchResult.Success(allResults))
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
