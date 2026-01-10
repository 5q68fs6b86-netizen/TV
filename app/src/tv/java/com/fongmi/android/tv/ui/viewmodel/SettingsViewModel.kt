package com.fongmi.android.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fongmi.android.tv.Setting
import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.bean.Site
import com.fongmi.android.tv.data.repository.VodRepository
import com.fongmi.android.tv.ui.state.DisplaySettings
import com.fongmi.android.tv.ui.state.PlayerSettings
import com.fongmi.android.tv.ui.state.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Settings Screen
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val vodRepository: VodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    /**
     * Load current settings
     */
    private fun loadSettings() {
        viewModelScope.launch {
            val sites = vodRepository.getSites()
            val currentSite = vodRepository.getHomeSite()

            _uiState.update {
                it.copy(
                    sites = sites,
                    currentSite = currentSite,
                    playerSettings = PlayerSettings(
                        defaultPlayer = Setting.getPlayer(),
                        decodeType = Setting.getDecode(Setting.getPlayer()),
                        bufferSize = Setting.getBuffer()
                    ),
                    displaySettings = DisplaySettings(
                        size = Setting.getSize(),
                        wallpaper = Setting.getWall(),
                        language = Setting.getLanguage()
                    )
                )
            }
        }
    }

    /**
     * Change home site
     */
    fun setHomeSite(site: Site) {
        vodRepository.setHomeSite(site)
        _uiState.update { it.copy(currentSite = site) }
    }

    /**
     * Update player setting
     */
    fun setPlayer(player: Int) {
        Setting.putPlayer(player)
        _uiState.update {
            it.copy(playerSettings = it.playerSettings.copy(defaultPlayer = player))
        }
    }

    /**
     * Update decode type
     */
    fun setDecodeType(decode: Int) {
        Setting.putDecode(Setting.getPlayer(), decode)
        _uiState.update {
            it.copy(playerSettings = it.playerSettings.copy(decodeType = decode))
        }
    }

    /**
     * Update buffer size
     */
    fun setBufferSize(buffer: Int) {
        Setting.putBuffer(buffer)
        _uiState.update {
            it.copy(playerSettings = it.playerSettings.copy(bufferSize = buffer))
        }
    }

    /**
     * Update display size
     */
    fun setDisplaySize(size: Int) {
        Setting.putSize(size)
        _uiState.update {
            it.copy(displaySettings = it.displaySettings.copy(size = size))
        }
    }

    /**
     * Update wallpaper
     */
    fun setWallpaper(wallpaper: Int) {
        Setting.putWall(wallpaper)
        _uiState.update {
            it.copy(displaySettings = it.displaySettings.copy(wallpaper = wallpaper))
        }
    }

    /**
     * Update language
     */
    fun setLanguage(language: Int) {
        Setting.putLanguage(language)
        _uiState.update {
            it.copy(displaySettings = it.displaySettings.copy(language = language))
        }
    }

    /**
     * Get player names
     */
    fun getPlayerNames(): List<String> = listOf("系统播放器", "ExoPlayer", "IJKPlayer")

    /**
     * Get decode type names
     */
    fun getDecodeTypeNames(): List<String> = listOf("软解码", "硬解码", "自动")

    /**
     * Get language names
     */
    fun getLanguageNames(): List<String> = listOf("简体中文", "繁體中文", "English")
}
