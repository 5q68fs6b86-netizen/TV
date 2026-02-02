package com.fongmi.android.tv.data.repository

import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.bean.History
import com.fongmi.android.tv.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for watch history data access
 * Provides a clean API for ViewModel to access watch history
 */
@Singleton
class HistoryRepository @Inject constructor() {

    private val historyDao get() = AppDatabase.get().historyDao

    /**
     * Get current config ID
     */
    private fun getCid(): Int = VodConfig.getCid()

    /**
     * Get all history items for current config
     */
    fun getHistory(): Flow<HistoryListResult> = flow {
        emit(HistoryListResult.Loading)
        try {
            val items = historyDao.find(getCid())
            emit(HistoryListResult.Success(items ?: emptyList()))
        } catch (e: Exception) {
            emit(HistoryListResult.Error(e.message ?: "Failed to load history"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get history items as a list (synchronous)
     */
    suspend fun getHistoryList(): List<History> = withContext(Dispatchers.IO) {
        try {
            historyDao.find(getCid()) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get a specific history item by key
     */
    suspend fun getHistoryItem(key: String): History? = withContext(Dispatchers.IO) {
        try {
            historyDao.find(getCid(), key)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Add or update a history item
     */
    suspend fun addOrUpdateHistory(history: History) = withContext(Dispatchers.IO) {
        try {
            history.cid = getCid()
            historyDao.insertOrUpdate(history)
        } catch (e: Exception) {
            // Log error
        }
    }

    /**
     * Delete a history item
     */
    suspend fun deleteHistory(history: History) = withContext(Dispatchers.IO) {
        try {
            historyDao.delete(getCid(), history.key)
        } catch (e: Exception) {
            // Log error
        }
    }

    /**
     * Delete history item by key
     */
    suspend fun deleteHistoryByKey(key: String) = withContext(Dispatchers.IO) {
        try {
            historyDao.delete(getCid(), key)
        } catch (e: Exception) {
            // Log error
        }
    }

    /**
     * Clear all history for current config
     */
    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        try {
            historyDao.delete(getCid())
        } catch (e: Exception) {
            // Log error
        }
    }

    /**
     * Clear history by time range
     */
    suspend fun clearHistoryByTime(beforeTimestamp: Long) = withContext(Dispatchers.IO) {
        try {
            val items = historyDao.find(getCid()) ?: return@withContext
            items.filter { it.createTime < beforeTimestamp }.forEach {
                historyDao.delete(getCid(), it.key)
            }
        } catch (e: Exception) {
            // Log error
        }
    }

    /**
     * Get history count
     */
    suspend fun getHistoryCount(): Int = withContext(Dispatchers.IO) {
        try {
            historyDao.find(getCid())?.size ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Update playback position for a history item
     */
    suspend fun updatePosition(key: String, position: Long, duration: Long) = withContext(Dispatchers.IO) {
        try {
            val history = historyDao.find(getCid(), key)
            if (history != null) {
                history.setPosition(position)
                history.setDuration(duration)
                historyDao.insertOrUpdate(history)
            }
        } catch (e: Exception) {
            // Log error
        }
    }
}

// Result sealed classes for type-safe results
sealed class HistoryListResult {
    data object Loading : HistoryListResult()
    data class Success(val items: List<History>) : HistoryListResult()
    data class Error(val message: String) : HistoryListResult()
}

