package com.fongmi.android.tv.ui.state

import com.fongmi.android.tv.bean.Episode
import com.fongmi.android.tv.bean.Flag
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds player state for sharing between DetailScreen and PlayerScreen
 * This allows passing complex objects (flags, episodes) that can't go through Navigation
 */
@Singleton
class PlayerStateHolder @Inject constructor() {

    /**
     * Current playback data
     */
    data class PlaybackData(
        val vodName: String = "",
        val vodPic: String = "",
        val siteKey: String = "",
        val vodId: String = "",
        val flags: List<Flag> = emptyList(),
        val currentFlagIndex: Int = 0,
        val currentEpisodeIndex: Int = 0,
        val url: String = "",
        val headers: Map<String, String>? = null
    ) {
        val currentFlag: Flag?
            get() = flags.getOrNull(currentFlagIndex)

        val currentEpisode: Episode?
            get() = currentFlag?.episodes?.getOrNull(currentEpisodeIndex)

        val episodeName: String
            get() = currentEpisode?.name ?: ""

        val hasMultipleEpisodes: Boolean
            get() = flags.any { (it.episodes?.size ?: 0) > 1 }
    }

    private var _playbackData: PlaybackData? = null

    /**
     * Current playback data
     */
    val playbackData: PlaybackData?
        get() = _playbackData

    /**
     * Set playback data when starting playback from DetailScreen
     */
    fun setPlaybackData(
        vodName: String,
        vodPic: String,
        siteKey: String,
        vodId: String,
        flags: List<Flag>,
        currentFlagIndex: Int,
        currentEpisodeIndex: Int,
        url: String,
        headers: Map<String, String>?
    ) {
        _playbackData = PlaybackData(
            vodName = vodName,
            vodPic = vodPic,
            siteKey = siteKey,
            vodId = vodId,
            flags = flags,
            currentFlagIndex = currentFlagIndex,
            currentEpisodeIndex = currentEpisodeIndex,
            url = url,
            headers = headers
        )
    }

    /**
     * Update current flag and episode index
     */
    fun updateSelection(flagIndex: Int, episodeIndex: Int) {
        _playbackData = _playbackData?.copy(
            currentFlagIndex = flagIndex,
            currentEpisodeIndex = episodeIndex
        )
    }

    /**
     * Update URL for new episode
     */
    fun updateUrl(url: String, headers: Map<String, String>? = null) {
        _playbackData = _playbackData?.copy(
            url = url,
            headers = headers
        )
    }

    /**
     * Clear playback data when leaving player
     */
    fun clear() {
        _playbackData = null
    }

    /**
     * Set playback data for live TV (no episode list)
     */
    fun setLivePlayback(
        channelName: String,
        url: String,
        headers: Map<String, String>? = null
    ) {
        _playbackData = PlaybackData(
            vodName = channelName,
            vodPic = "",
            siteKey = "",
            vodId = "",
            flags = emptyList(),
            currentFlagIndex = 0,
            currentEpisodeIndex = 0,
            url = url,
            headers = headers
        )
    }
}
