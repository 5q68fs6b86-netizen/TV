package com.fongmi.android.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fongmi.android.tv.api.config.LiveConfig
import com.fongmi.android.tv.bean.Channel
import com.fongmi.android.tv.bean.Group
import com.fongmi.android.tv.bean.Live
import com.fongmi.android.tv.impl.Callback
import com.fongmi.android.tv.ui.state.PlayerStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * UI State for Live Screen
 */
data class LiveUiState(
    val isLoading: Boolean = true,
    val groups: List<Group> = emptyList(),
    val selectedGroup: Group? = null,
    val selectedChannel: Channel? = null,
    val playUrl: String? = null,
    val error: String? = null
)

/**
 * ViewModel for Live Screen
 * Manages live TV data and channel selection
 */
@HiltViewModel
class LiveViewModel @Inject constructor(
    private val playerStateHolder: PlayerStateHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveUiState())
    val uiState: StateFlow<LiveUiState> = _uiState.asStateFlow()

    init {
        loadLive()
    }

    /**
     * Load live configuration
     */
    fun loadLive() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Check if LiveConfig is already loaded
                val home = LiveConfig.get().home
                if (home != null && home.groups?.isNotEmpty() == true) {
                    val groups = home.groups ?: emptyList()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            groups = groups,
                            selectedGroup = groups.firstOrNull()
                        )
                    }
                } else {
                    // Load live config
                    LiveConfig.get().load(object : Callback() {
                        override fun success() {
                            val liveHome = LiveConfig.get().home
                            val groups = liveHome?.groups ?: emptyList()
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    groups = groups,
                                    selectedGroup = groups.firstOrNull()
                                )
                            }
                        }

                        override fun error(msg: String?) {
                            _uiState.update {
                                it.copy(isLoading = false, error = msg ?: "加载失败")
                            }
                        }
                    })
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "加载失败")
                }
            }
        }
    }

    /**
     * Select a group
     */
    fun selectGroup(group: Group) {
        _uiState.update {
            it.copy(selectedGroup = group, selectedChannel = null)
        }
    }

    /**
     * Select a channel
     */
    fun selectChannel(channel: Channel) {
        _uiState.update { it.copy(selectedChannel = channel) }
    }

    /**
     * Get channel URL for playback
     */
    fun getChannelUrl(channel: Channel): String {
        return channel.urls?.firstOrNull() ?: ""
    }

    /**
     * Prepare channel for playback (sets PlayerStateHolder)
     * Returns URL if valid, empty string otherwise
     */
    fun prepareChannelForPlayback(channel: Channel): String {
        val url = channel.urls?.firstOrNull() ?: return ""
        if (url.isNotEmpty()) {
            playerStateHolder.setLivePlayback(
                channelName = channel.name ?: "直播",
                url = url,
                headers = null
            )
        }
        return url
    }

    /**
     * Get channels for current selected group
     */
    fun getChannels(): List<Channel> {
        return _uiState.value.selectedGroup?.channel ?: emptyList()
    }

    /**
     * Refresh live data
     */
    fun refresh() {
        LiveConfig.get().clear()
        loadLive()
    }
}
