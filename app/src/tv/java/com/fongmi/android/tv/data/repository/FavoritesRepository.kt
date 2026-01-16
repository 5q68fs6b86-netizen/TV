package com.fongmi.android.tv.data.repository

import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.bean.Keep
import com.fongmi.android.tv.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for favorites (Keep) data access
 * Provides a clean API for ViewModel to access favorite items
 */
@Singleton
class FavoritesRepository @Inject constructor() {

    private val keepDao get() = AppDatabase.get().keepDao

    /**
     * Get current config ID
     */
    private fun getCid(): Int = VodConfig.getCid()

    /**
     * Create key from siteKey and vodId
     */
    private fun createKey(siteKey: String, vodId: String): String {
        return "$siteKey${AppDatabase.SYMBOL}$vodId"
    }

    /**
     * Get all favorite VOD items
     */
    fun getFavorites(): Flow<FavoritesListResult> = flow {
        emit(FavoritesListResult.Loading)
        try {
            val items = keepDao.vod ?: emptyList()
            emit(FavoritesListResult.Success(items))
        } catch (e: Exception) {
            emit(FavoritesListResult.Error(e.message ?: "Failed to load favorites"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get favorites as a list (synchronous)
     */
    suspend fun getFavoritesList(): List<Keep> = withContext(Dispatchers.IO) {
        try {
            keepDao.vod ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Check if item is in favorites
     */
    suspend fun isFavorite(siteKey: String, vodId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val key = createKey(siteKey, vodId)
            keepDao.find(getCid(), key) != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Add item to favorites
     */
    suspend fun addFavorite(keep: Keep) = withContext(Dispatchers.IO) {
        try {
            keep.cid = getCid()
            keep.type = 0 // VOD type
            keepDao.insertOrUpdate(keep)
        } catch (e: Exception) {
            // Log error
        }
    }

    /**
     * Add VOD to favorites with details
     */
    suspend fun addToFavorites(
        siteKey: String,
        siteName: String,
        vodId: String,
        vodName: String,
        vodPic: String?
    ) = withContext(Dispatchers.IO) {
        try {
            val keep = Keep().apply {
                key = createKey(siteKey, vodId)
                this.siteName = siteName
                this.vodName = vodName
                this.vodPic = vodPic
                this.cid = getCid()
                this.type = 0 // VOD type
                this.createTime = System.currentTimeMillis()
            }
            keepDao.insertOrUpdate(keep)
        } catch (e: Exception) {
            // Log error
        }
    }

    /**
     * Remove item from favorites
     */
    suspend fun removeFavorite(keep: Keep) = withContext(Dispatchers.IO) {
        try {
            keepDao.delete(keep.cid, keep.key)
        } catch (e: Exception) {
            // Log error
        }
    }

    /**
     * Remove item from favorites by key
     */
    suspend fun removeFavoriteByKey(siteKey: String, vodId: String) = withContext(Dispatchers.IO) {
        try {
            val key = createKey(siteKey, vodId)
            keepDao.delete(getCid(), key)
        } catch (e: Exception) {
            // Log error
        }
    }

    /**
     * Toggle favorite status
     * Returns true if item was added to favorites, false if removed
     */
    suspend fun toggleFavorite(
        siteKey: String,
        siteName: String,
        vodId: String,
        vodName: String,
        vodPic: String?
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val key = createKey(siteKey, vodId)
            val existing = keepDao.find(getCid(), key)
            if (existing != null) {
                keepDao.delete(getCid(), key)
                false
            } else {
                val keep = Keep().apply {
                    this.key = key
                    this.siteName = siteName
                    this.vodName = vodName
                    this.vodPic = vodPic
                    this.cid = getCid()
                    this.type = 0 // VOD type
                    this.createTime = System.currentTimeMillis()
                }
                keepDao.insertOrUpdate(keep)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Clear all VOD favorites
     */
    suspend fun clearFavorites() = withContext(Dispatchers.IO) {
        try {
            keepDao.delete()
        } catch (e: Exception) {
            // Log error
        }
    }
}

// Result sealed classes for type-safe results
sealed class FavoritesListResult {
    data object Loading : FavoritesListResult()
    data class Success(val items: List<Keep>) : FavoritesListResult()
    data class Error(val message: String) : FavoritesListResult()
}

