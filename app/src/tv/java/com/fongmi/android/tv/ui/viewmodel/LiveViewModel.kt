package com.fongmi.android.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fongmi.android.tv.api.config.LiveConfig
import com.fongmi.android.tv.bean.Channel
import com.fongmi.android.tv.bean.Group
import com.fongmi.android.tv.bean.Live
import com.fongmi.android.tv.data.repository.LiveConfigResult
import com.fongmi.android.tv.data.repository.LiveRepository
import com.fongmi.android.tv.impl.Callback
import com.fongmi.android.tv.ui.state.PlayerStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    val error: String? = null,
    val channelNumber: String = "",
    val showChannelInfo: Boolean = false,
    val currentLineIndex: Int = 0,
    val totalLines: Int = 0,
    // EPG
    val showEpgDialog: Boolean = false,
    val epgPrograms: List<com.fongmi.android.tv.ui.dialog.EpgProgram> = emptyList(),
    // Favorites
    val showFavoriteDialog: Boolean = false,
    val favoriteChannels: List<Channel> = emptyList(),
    val isCurrentChannelFavorite: Boolean = false
)

/**
 * ViewModel for Live Screen
 * Manages live TV data and channel selection
 */
@HiltViewModel
class LiveViewModel @Inject constructor(
    private val playerStateHolder: PlayerStateHolder,
    private val liveRepository: LiveRepository
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
                val groups = liveRepository.getGroups()
                if (groups.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            groups = groups,
                            selectedGroup = groups.firstOrNull()
                        )
                    }
                } else {
                    // Load live config via Flow
                    liveRepository.loadLiveConfig().collect { result ->
                        when (result) {
                            is LiveConfigResult.Loading -> {
                                _uiState.update { it.copy(isLoading = true, error = null) }
                            }
                            is LiveConfigResult.Success -> {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        groups = result.groups,
                                        selectedGroup = result.groups.firstOrNull()
                                    )
                                }
                            }
                            is LiveConfigResult.Error -> {
                                _uiState.update {
                                    it.copy(isLoading = false, error = result.message)
                                }
                            }
                        }
                    }
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
        val isFavorite = liveRepository.isFavorite(channel)
        _uiState.update { it.copy(selectedChannel = channel, isCurrentChannelFavorite = isFavorite) }
    }

    /**
     * Get channel URL for playback
     */
    fun getChannelUrl(channel: Channel): String {
        return liveRepository.getChannelUrl(channel)
    }

    /**
     * Prepare channel for playback (sets PlayerStateHolder)
     * Returns URL if valid, empty string otherwise
     */
    fun prepareChannelForPlayback(channel: Channel): String {
        val url = liveRepository.getChannelUrl(channel)
        if (url.isNotEmpty()) {
            val state = _uiState.value
            val groupIndex = state.groups.indexOf(state.selectedGroup).coerceAtLeast(0)
            val channelIndex = state.selectedGroup?.channel?.indexOf(channel)?.coerceAtLeast(0) ?: 0

            playerStateHolder.setLivePlayback(
                channelName = channel.name ?: "直播",
                url = url,
                headers = null,
                groups = state.groups,
                groupIndex = groupIndex,
                channelIndex = channelIndex
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
     * Get channel by number (1-indexed)
     */
    fun getChannelByNumber(number: Int): Channel? {
        return liveRepository.getChannelByNumber(number)
    }

    /**
     * Switch to next URL for current channel
     */
    fun switchChannelUrl(channel: Channel) {
        liveRepository.nextChannelUrl(channel)
    }

    /**
     * Refresh live data
     */
    fun refresh() {
        LiveConfig.get().clear()
        loadLive()
    }

    /**
     * 输入数字选择频道
     */
    fun inputNumber(digit: Int) {
        val newNumber = _uiState.value.channelNumber + digit.toString()
        _uiState.update { it.copy(channelNumber = newNumber, showChannelInfo = true) }
    }

    /**
     * 清除数字输入
     */
    fun clearNumber() {
        _uiState.update { it.copy(channelNumber = "", showChannelInfo = false) }
    }

    /**
     * 确认数字输入，跳转到对应频道
     */
    fun confirmNumber(): Channel? {
        val number = _uiState.value.channelNumber.toIntOrNull() ?: return null
        clearNumber()
        return getChannelByNumber(number)
    }

    /**
     * 切换到上一个频道
     */
    fun previousChannel(): Channel? {
        val state = _uiState.value
        val channels = state.selectedGroup?.channel ?: return null
        val currentIndex = channels.indexOf(state.selectedChannel)
        val newIndex = if (currentIndex <= 0) channels.size - 1 else currentIndex - 1
        val channel = channels.getOrNull(newIndex) ?: return null
        selectChannel(channel)
        return channel
    }

    /**
     * 切换到下一个频道
     */
    fun nextChannel(): Channel? {
        val state = _uiState.value
        val channels = state.selectedGroup?.channel ?: return null
        val currentIndex = channels.indexOf(state.selectedChannel)
        val newIndex = if (currentIndex >= channels.size - 1) 0 else currentIndex + 1
        val channel = channels.getOrNull(newIndex) ?: return null
        selectChannel(channel)
        return channel
    }

    /**
     * 切换到上一个分组
     */
    fun previousGroup(): Group? {
        val state = _uiState.value
        val groups = state.groups
        val currentIndex = groups.indexOf(state.selectedGroup)
        val newIndex = if (currentIndex <= 0) groups.size - 1 else currentIndex - 1
        val group = groups.getOrNull(newIndex) ?: return null
        selectGroup(group)
        return group
    }

    /**
     * 切换到下一个分组
     */
    fun nextGroup(): Group? {
        val state = _uiState.value
        val groups = state.groups
        val currentIndex = groups.indexOf(state.selectedGroup)
        val newIndex = if (currentIndex >= groups.size - 1) 0 else currentIndex + 1
        val group = groups.getOrNull(newIndex) ?: return null
        selectGroup(group)
        return group
    }

    /**
     * 切换线路
     */
    fun switchLine(): String {
        val channel = _uiState.value.selectedChannel ?: return ""
        liveRepository.nextChannelUrl(channel)
        val url = liveRepository.getChannelUrl(channel)
        val lineIndex = channel.line
        val totalLines = channel.urls?.size ?: 1
        _uiState.update { it.copy(currentLineIndex = lineIndex, totalLines = totalLines) }
        return url
    }

    /**
     * 显示/隐藏频道信息
     */
    fun toggleChannelInfo() {
        _uiState.update { it.copy(showChannelInfo = !it.showChannelInfo) }
    }

    /**
     * 隐藏频道信息
     */
    fun hideChannelInfo() {
        _uiState.update { it.copy(showChannelInfo = false) }
    }

    // ========== EPG Methods ==========

    fun showEpgDialog() {
        val channel = _uiState.value.selectedChannel ?: return
        val programs = liveRepository.getEpgPrograms(channel)
        _uiState.update { it.copy(showEpgDialog = true, epgPrograms = programs) }
    }

    fun dismissEpgDialog() {
        _uiState.update { it.copy(showEpgDialog = false) }
    }

    fun playCatchup(program: com.fongmi.android.tv.ui.dialog.EpgProgram): String? {
        return program.catchupUrl
    }

    // ========== Favorite Methods ==========

    fun showFavoriteDialog() {
        val favorites = liveRepository.getFavoriteChannels()
        _uiState.update { it.copy(showFavoriteDialog = true, favoriteChannels = favorites) }
    }

    fun dismissFavoriteDialog() {
        _uiState.update { it.copy(showFavoriteDialog = false) }
    }

    fun toggleFavorite() {
        val channel = _uiState.value.selectedChannel ?: return
        val isFavorite = liveRepository.toggleFavorite(channel)
        _uiState.update { it.copy(isCurrentChannelFavorite = isFavorite) }
    }

    fun removeFavorite(channel: Channel) {
        liveRepository.removeFavorite(channel)
        _uiState.update { it.copy(favoriteChannels = liveRepository.getFavoriteChannels()) }
    }

    fun clearFavorites() {
        liveRepository.clearFavorites()
        _uiState.update { it.copy(favoriteChannels = emptyList()) }
    }

    fun isFavorite(channel: Channel): Boolean {
        return liveRepository.isFavorite(channel)
    }

    private fun updateFavoriteStatus() {
        val channel = _uiState.value.selectedChannel
        val isFavorite = channel?.let { liveRepository.isFavorite(it) } ?: false
        _uiState.update { it.copy(isCurrentChannelFavorite = isFavorite) }
    }
}

