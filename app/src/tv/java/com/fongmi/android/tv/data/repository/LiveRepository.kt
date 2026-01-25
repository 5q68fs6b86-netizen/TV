package com.fongmi.android.tv.data.repository

import com.fongmi.android.tv.api.config.LiveConfig
import com.fongmi.android.tv.bean.Channel
import com.fongmi.android.tv.bean.Epg
import com.fongmi.android.tv.bean.Group
import com.fongmi.android.tv.bean.Live
import com.fongmi.android.tv.ui.dialog.EpgProgram
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Live TV data access
 * Provides a clean API for ViewModel to access live TV content
 */
@Singleton
class LiveRepository @Inject constructor() {

    private val liveConfig: LiveConfig get() = LiveConfig.get()
    private val favoriteChannels = mutableListOf<Channel>()

    /**
     * Get all configured live sources
     */
    fun getLives(): List<Live> = liveConfig.lives ?: emptyList()

    /**
     * Get current home live source
     */
    fun getHomeLive(): Live? = liveConfig.home

    /**
     * Set home live source
     */
    fun setHomeLive(live: Live) {
        liveConfig.setHome(live)
    }

    /**
     * Get groups from current live source
     */
    fun getGroups(): List<Group> {
        return getHomeLive()?.groups ?: emptyList()
    }

    /**
     * Get channels from a specific group
     */
    fun getChannels(group: Group): List<Channel> {
        return group.channel ?: emptyList()
    }

    /**
     * Get all channels from all groups (flattened)
     */
    fun getAllChannels(): List<Channel> {
        return getGroups().flatMap { it.channel ?: emptyList() }
    }

    /**
     * Load live configuration
     */
    fun loadLiveConfig(): Flow<LiveConfigResult> = flow {
        emit(LiveConfigResult.Loading)
        try {
            val live = liveConfig.home
            if (live != null) {
                val groups = live.groups ?: emptyList()
                emit(LiveConfigResult.Success(
                    live = live,
                    groups = groups
                ))
            } else {
                emit(LiveConfigResult.Error("No live source configured"))
            }
        } catch (e: Exception) {
            emit(LiveConfigResult.Error(e.message ?: "Failed to load live config"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get EPG (Electronic Program Guide) for a channel
     */
    fun loadEpg(channel: Channel): Flow<EpgResult> = flow {
        emit(EpgResult.Loading)
        try {
            val epgData = channel.data
            if (epgData != null) {
                emit(EpgResult.Success(epgData))
            } else {
                emit(EpgResult.Empty)
            }
        } catch (e: Exception) {
            emit(EpgResult.Error(e.message ?: "Failed to load EPG"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get EPG programs for a channel
     */
    fun getEpgPrograms(channel: Channel): List<EpgProgram> {
        val epg = channel.data ?: return emptyList()
        val programs = mutableListOf<EpgProgram>()
        val now = System.currentTimeMillis()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        epg.list?.forEach { item ->
            val startTime = item.startTime ?: 0L
            val endTime = item.endTime ?: 0L
            val isLive = now in startTime..endTime
            val isPast = endTime < now

            programs.add(EpgProgram(
                title = item.title ?: "",
                startTime = timeFormat.format(Date(startTime)),
                endTime = timeFormat.format(Date(endTime)),
                isLive = isLive,
                isPast = isPast,
                catchupUrl = if (isPast && channel.catchup?.source != null) channel.catchup.source else null
            ))
        }
        return programs
    }

    /**
     * Get channel URL for playback
     */
    fun getChannelUrl(channel: Channel): String {
        return channel.current ?: ""
    }

    /**
     * Switch to next URL for a channel (for multi-source channels)
     */
    fun nextChannelUrl(channel: Channel) {
        channel.nextLine()
    }

    /**
     * Get channel by number
     */
    fun getChannelByNumber(number: Int): Channel? {
        val allChannels = getAllChannels()
        return allChannels.getOrNull(number - 1)
    }

    /**
     * Find channel index in flattened list
     */
    fun getChannelIndex(channel: Channel): Int {
        return getAllChannels().indexOf(channel)
    }

    // ========== Favorite Channels ==========

    /**
     * Get favorite channels
     */
    fun getFavoriteChannels(): List<Channel> = favoriteChannels.toList()

    /**
     * Add channel to favorites
     */
    fun addFavorite(channel: Channel) {
        if (!favoriteChannels.contains(channel)) {
            favoriteChannels.add(channel)
        }
    }

    /**
     * Remove channel from favorites
     */
    fun removeFavorite(channel: Channel) {
        favoriteChannels.remove(channel)
    }

    /**
     * Check if channel is favorite
     */
    fun isFavorite(channel: Channel): Boolean {
        return favoriteChannels.contains(channel)
    }

    /**
     * Clear all favorites
     */
    fun clearFavorites() {
        favoriteChannels.clear()
    }

    /**
     * Toggle favorite status
     */
    fun toggleFavorite(channel: Channel): Boolean {
        return if (isFavorite(channel)) {
            removeFavorite(channel)
            false
        } else {
            addFavorite(channel)
            true
        }
    }
}

// Result sealed classes for type-safe results
sealed class LiveConfigResult {
    data object Loading : LiveConfigResult()
    data class Success(val live: Live, val groups: List<Group>) : LiveConfigResult()
    data class Error(val message: String) : LiveConfigResult()
}

sealed class EpgResult {
    data object Loading : EpgResult()
    data object Empty : EpgResult()
    data class Success(val epg: Epg) : EpgResult()
    data class Error(val message: String) : EpgResult()
}
