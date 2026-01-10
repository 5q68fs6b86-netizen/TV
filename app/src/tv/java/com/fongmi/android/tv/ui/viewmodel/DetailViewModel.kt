package com.fongmi.android.tv.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.bean.Episode
import com.fongmi.android.tv.bean.Flag
import com.fongmi.android.tv.bean.Vod
import com.fongmi.android.tv.data.repository.DetailResult
import com.fongmi.android.tv.data.repository.PlayUrlResult
import com.fongmi.android.tv.data.repository.VodRepository
import com.fongmi.android.tv.ui.state.DetailUiState
import com.fongmi.android.tv.ui.state.PlayerStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Detail Screen
 * Manages video detail, episodes, and playback preparation
 */
@HiltViewModel
class DetailViewModel @Inject constructor(
    private val vodRepository: VodRepository,
    private val playerStateHolder: PlayerStateHolder,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    // Selected flag (play source) and episode
    private val _selectedFlag = MutableStateFlow<Flag?>(null)
    val selectedFlag: StateFlow<Flag?> = _selectedFlag.asStateFlow()

    private val _selectedEpisode = MutableStateFlow<Episode?>(null)
    val selectedEpisode: StateFlow<Episode?> = _selectedEpisode.asStateFlow()

    // Play URL result
    private val _playUrlState = MutableStateFlow<PlayUrlState>(PlayUrlState.Idle)
    val playUrlState: StateFlow<PlayUrlState> = _playUrlState.asStateFlow()

    // Get site key and vod id from navigation arguments
    private val siteKey: String? = savedStateHandle["siteKey"]
    private val vodId: String? = savedStateHandle["vodId"]

    init {
        if (siteKey != null && vodId != null) {
            loadDetail(siteKey, vodId)
        }
    }

    /**
     * Load video detail
     */
    fun loadDetail(siteKey: String, vodId: String) {
        val site = VodConfig.get().getSite(siteKey) ?: return

        viewModelScope.launch {
            vodRepository.loadDetail(site, vodId).collect { result ->
                when (result) {
                    is DetailResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is DetailResult.Success -> {
                        val vod = result.vod
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                vod = vod,
                                error = null
                            )
                        }
                        // Auto-select first flag and episode
                        vod.vodFlags?.firstOrNull()?.let { flag ->
                            selectFlag(flag)
                        }
                    }
                    is DetailResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }
        }
    }

    /**
     * Get all available flags (play sources)
     */
    fun getFlags(): List<Flag> = _uiState.value.vod?.vodFlags ?: emptyList()

    /**
     * Get episodes for current selected flag
     */
    fun getEpisodes(): List<Episode> = _selectedFlag.value?.episodes ?: emptyList()

    /**
     * Select a flag (play source)
     */
    fun selectFlag(flag: Flag) {
        _selectedFlag.update { flag }

        // Auto-select first episode
        flag.episodes?.firstOrNull()?.let { episode ->
            selectEpisode(episode)
        }
    }

    /**
     * Select an episode
     */
    fun selectEpisode(episode: Episode) {
        _selectedEpisode.update { episode }
    }

    /**
     * Play selected episode
     */
    fun play() {
        val vod = _uiState.value.vod ?: return
        val flag = _selectedFlag.value ?: return
        val episode = _selectedEpisode.value ?: return
        val site = vod.site ?: return

        // Calculate current indices
        val flags = vod.vodFlags ?: emptyList()
        val flagIndex = flags.indexOf(flag).coerceAtLeast(0)
        val episodeIndex = (flag.episodes?.indexOf(episode) ?: 0).coerceAtLeast(0)

        viewModelScope.launch {
            _playUrlState.update { PlayUrlState.Loading }

            vodRepository.getPlayUrl(site, flag.flag ?: "", episode.url ?: "").collect { result ->
                when (result) {
                    is PlayUrlResult.Loading -> {
                        _playUrlState.update { PlayUrlState.Loading }
                    }
                    is PlayUrlResult.Success -> {
                        // Set player state for sharing with PlayerScreen
                        playerStateHolder.setPlaybackData(
                            vodName = vod.vodName ?: "",
                            vodPic = vod.vodPic ?: "",
                            siteKey = site.key ?: "",
                            vodId = vod.vodId ?: "",
                            flags = flags,
                            currentFlagIndex = flagIndex,
                            currentEpisodeIndex = episodeIndex,
                            url = result.url,
                            headers = result.headers
                        )

                        _playUrlState.update {
                            PlayUrlState.Ready(
                                url = result.url,
                                headers = result.headers,
                                vodName = vod.vodName ?: "",
                                episodeName = episode.name ?: ""
                            )
                        }
                    }
                    is PlayUrlResult.Error -> {
                        _playUrlState.update { PlayUrlState.Error(result.message) }
                    }
                }
            }
        }
    }

    /**
     * Clear play URL state
     */
    fun clearPlayState() {
        _playUrlState.update { PlayUrlState.Idle }
    }

    /**
     * Retry loading detail
     */
    fun retry() {
        if (siteKey != null && vodId != null) {
            loadDetail(siteKey, vodId)
        }
    }
}

/**
 * Play URL state
 */
sealed class PlayUrlState {
    data object Idle : PlayUrlState()
    data object Loading : PlayUrlState()
    data class Ready(
        val url: String,
        val headers: Map<String, String>?,
        val vodName: String,
        val episodeName: String
    ) : PlayUrlState()
    data class Error(val message: String) : PlayUrlState()
}
