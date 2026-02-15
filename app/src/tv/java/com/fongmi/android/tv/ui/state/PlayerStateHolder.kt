package com.fongmi.android.tv.ui.state

import com.fongmi.android.tv.bean.Channel
import com.fongmi.android.tv.bean.Episode
import com.fongmi.android.tv.bean.Flag
import com.fongmi.android.tv.bean.Group
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        val headers: Map<String, String>? = null,
        val danmuUrl: String = "",
        // Live TV specific fields
        val isLive: Boolean = false,
        val liveGroups: List<Group> = emptyList(),
        val currentGroupIndex: Int = 0,
        val currentChannelIndex: Int = 0
    ) {
        val currentFlag: Flag?
            get() = flags.getOrNull(currentFlagIndex)

        val currentEpisode: Episode?
            get() = currentFlag?.episodes?.getOrNull(currentEpisodeIndex)

        val episodeName: String
            get() = currentEpisode?.name ?: ""

        val hasMultipleEpisodes: Boolean
            get() = flags.any { (it.episodes?.size ?: 0) > 1 }

        // Live TV helpers
        val currentGroup: Group?
            get() = liveGroups.getOrNull(currentGroupIndex)

        val currentChannel: Channel?
            get() = currentGroup?.channel?.getOrNull(currentChannelIndex)

        val allChannels: List<Channel>
            get() = liveGroups.flatMap { it.channel ?: emptyList() }

        val flatChannelIndex: Int
            get() {
                var index = 0
                for (i in 0 until currentGroupIndex) {
                    index += liveGroups.getOrNull(i)?.channel?.size ?: 0
                }
                return index + currentChannelIndex
            }
    }

    private var _playbackData: PlaybackData? = null

    /**
     * Current playback data
     */
    val playbackData: PlaybackData?
        get() = _playbackData

    /**
     * Observable danmu URL flow for cross-ViewModel communication
     */
    private val _danmuUrlFlow = MutableStateFlow("")
    val danmuUrlFlow: StateFlow<String> = _danmuUrlFlow.asStateFlow()

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
        headers: Map<String, String>?,
        danmuUrl: String = ""
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
            headers = headers,
            danmuUrl = danmuUrl
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
    fun updateUrl(url: String, headers: Map<String, String>? = null, danmuUrl: String = "") {
        _playbackData = _playbackData?.copy(
            url = url,
            headers = headers,
            danmuUrl = danmuUrl
        )
        if (danmuUrl.isNotEmpty()) {
            _danmuUrlFlow.value = danmuUrl
        }
    }

    /**
     * Clear playback data when leaving player
     */
    fun clear() {
        _playbackData = null
        _danmuUrlFlow.value = ""
    }

    /**
     * Set playback data for live TV (no episode list)
     */
    fun setLivePlayback(
        channelName: String,
        url: String,
        headers: Map<String, String>? = null,
        groups: List<Group> = emptyList(),
        groupIndex: Int = 0,
        channelIndex: Int = 0
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
            headers = headers,
            isLive = true,
            liveGroups = groups,
            currentGroupIndex = groupIndex,
            currentChannelIndex = channelIndex
        )
    }

    /**
     * Switch to next channel (within current group or across groups)
     * Returns true if switch was successful
     */
    fun nextChannel(): Boolean {
        val data = _playbackData ?: return false
        if (!data.isLive || data.liveGroups.isEmpty()) return false

        val currentGroup = data.currentGroup ?: return false
        val channels = currentGroup.channel ?: return false

        return if (data.currentChannelIndex < channels.size - 1) {
            // Next channel in current group
            val nextChannel = channels[data.currentChannelIndex + 1]
            val url = nextChannel.urls?.firstOrNull() ?: return false
            _playbackData = data.copy(
                currentChannelIndex = data.currentChannelIndex + 1,
                vodName = nextChannel.name ?: "",
                url = url
            )
            true
        } else if (data.currentGroupIndex < data.liveGroups.size - 1) {
            // First channel of next group
            val nextGroup = data.liveGroups[data.currentGroupIndex + 1]
            val nextChannel = nextGroup.channel?.firstOrNull() ?: return false
            val url = nextChannel.urls?.firstOrNull() ?: return false
            _playbackData = data.copy(
                currentGroupIndex = data.currentGroupIndex + 1,
                currentChannelIndex = 0,
                vodName = nextChannel.name ?: "",
                url = url
            )
            true
        } else {
            false
        }
    }

    /**
     * Switch to previous channel
     * Returns true if switch was successful
     */
    fun previousChannel(): Boolean {
        val data = _playbackData ?: return false
        if (!data.isLive || data.liveGroups.isEmpty()) return false

        return if (data.currentChannelIndex > 0) {
            // Previous channel in current group
            val currentGroup = data.currentGroup ?: return false
            val channels = currentGroup.channel ?: return false
            val prevChannel = channels[data.currentChannelIndex - 1]
            val url = prevChannel.urls?.firstOrNull() ?: return false
            _playbackData = data.copy(
                currentChannelIndex = data.currentChannelIndex - 1,
                vodName = prevChannel.name ?: "",
                url = url
            )
            true
        } else if (data.currentGroupIndex > 0) {
            // Last channel of previous group
            val prevGroup = data.liveGroups[data.currentGroupIndex - 1]
            val channels = prevGroup.channel ?: return false
            val prevChannel = channels.lastOrNull() ?: return false
            val url = prevChannel.urls?.firstOrNull() ?: return false
            _playbackData = data.copy(
                currentGroupIndex = data.currentGroupIndex - 1,
                currentChannelIndex = channels.size - 1,
                vodName = prevChannel.name ?: "",
                url = url
            )
            true
        } else {
            false
        }
    }

    /**
     * Switch to a specific channel by index within all channels
     */
    fun switchToChannel(flatIndex: Int): Boolean {
        val data = _playbackData ?: return false
        if (!data.isLive || data.liveGroups.isEmpty()) return false

        var currentIndex = 0
        for ((groupIndex, group) in data.liveGroups.withIndex()) {
            val channels = group.channel ?: continue
            for ((channelIndex, channel) in channels.withIndex()) {
                if (currentIndex == flatIndex) {
                    val url = channel.urls?.firstOrNull() ?: return false
                    _playbackData = data.copy(
                        currentGroupIndex = groupIndex,
                        currentChannelIndex = channelIndex,
                        vodName = channel.name ?: "",
                        url = url
                    )
                    return true
                }
                currentIndex++
            }
        }
        return false
    }
}
