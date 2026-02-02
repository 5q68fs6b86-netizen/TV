package com.fongmi.android.tv.data.repository

import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.bean.Class
import com.fongmi.android.tv.bean.Result
import com.fongmi.android.tv.bean.Site
import com.fongmi.android.tv.bean.Sub
import com.fongmi.android.tv.bean.Vod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

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
            val spider = targetSite.spider()

            // Get home content from spider
            val result = spider?.homeContent(false)
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
            val spider = site.spider()

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
     * Load video detail
     */
    fun loadDetail(site: Site, vodId: String): Flow<DetailResult> = flow {
        emit(DetailResult.Loading)
        try {
            val spider = site.spider()
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
            val spider = site.spider()

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
                            val spider = site.spider()
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
                            val spider = site.spider()
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
     */
    fun getPlayUrl(site: Site, flag: String, episodeId: String): Flow<PlayUrlResult> = flow {
        emit(PlayUrlResult.Loading)
        try {
            val spider = site.spider()
            val result = spider?.playerContent(flag, episodeId, vodConfig.flags)

            if (result != null) {
                val parsed = Result.fromJson(result)
                val url = parsed.url?.v() ?: parsed.playUrl
                if (!url.isNullOrEmpty()) {
                    emit(PlayUrlResult.Success(
                        url = url,
                        headers = parsed.headers,
                        subtitles = parsed.subs ?: emptyList()
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
    data class Success(val url: String, val headers: Map<String, String>?, val subtitles: List<Sub>) : PlayUrlResult()
    data class Error(val message: String) : PlayUrlResult()
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
