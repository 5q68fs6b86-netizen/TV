package com.fongmi.android.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fongmi.android.tv.Setting
import com.fongmi.android.tv.api.config.LiveConfig
import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.api.config.WallConfig
import com.fongmi.android.tv.bean.Config
import com.fongmi.android.tv.bean.Site
import com.fongmi.android.tv.event.RefreshEvent
import com.fongmi.android.tv.impl.Callback
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

    // ========== Additional methods for complete settings ==========

    fun getProxyText(): String = Setting.getProxy().ifEmpty { "无" }

    fun getCacheText(): String {
        // Return cache size, you may want to calculate this dynamically
        return "0 MB"
    }

    fun getDohText(): String {
        val dohSetting = Setting.getDoh()
        return if (dohSetting.isEmpty()) "关闭" else "开启"
    }

    fun getVersionText(): String {
        return try {
            com.fongmi.android.tv.BuildConfig.VERSION_NAME
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    fun getBackupModeText(): String {
        val mode = Setting.getBackupMode()
        return when (mode) {
            0 -> "关闭"
            1 -> "每日"
            2 -> "每周"
            else -> "关闭"
        }
    }

    fun toggleBackupMode() {
        val current = Setting.getBackupMode()
        val next = (current + 1) % 3
        Setting.putBackupMode(next)
    }

    fun clearCache() {
        viewModelScope.launch {
            // Clear Glide cache
            try {
                com.bumptech.glide.Glide.get(com.fongmi.android.tv.TvApp.get()).clearDiskCache()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun showDohDialog() {
        // This would typically emit an event to show dialog
    }

    fun showProxyDialog() {
        // This would typically emit an event to show dialog
    }

    fun restore() {
        // This would typically trigger restore flow
    }

    fun checkUpdate() {
        // This would check for updates
    }

    // ========== Dialog control methods ==========

    fun showVodConfigDialog() {
        _uiState.update { it.copy(showVodConfigDialog = true) }
    }

    fun dismissVodConfigDialog() {
        _uiState.update { it.copy(showVodConfigDialog = false) }
    }

    fun showLiveConfigDialog() {
        _uiState.update { it.copy(showLiveConfigDialog = true) }
    }

    fun dismissLiveConfigDialog() {
        _uiState.update { it.copy(showLiveConfigDialog = false) }
    }

    fun showWallConfigDialog() {
        _uiState.update { it.copy(showWallConfigDialog = true) }
    }

    fun dismissWallConfigDialog() {
        _uiState.update { it.copy(showWallConfigDialog = false) }
    }

    fun showSiteDialog() {
        _uiState.update { it.copy(showSiteDialog = true) }
    }

    fun dismissSiteDialog() {
        _uiState.update { it.copy(showSiteDialog = false) }
    }

    fun showProxyDialogAction() {
        _uiState.update { it.copy(showProxyDialog = true) }
    }

    fun dismissProxyDialog() {
        _uiState.update { it.copy(showProxyDialog = false) }
    }

    fun showDohDialogAction() {
        _uiState.update { it.copy(showDohDialog = true) }
    }

    fun dismissDohDialog() {
        _uiState.update { it.copy(showDohDialog = false) }
    }

    fun showBackupDialogAction() {
        _uiState.update { it.copy(showBackupDialog = true) }
    }

    fun dismissBackupDialog() {
        _uiState.update { it.copy(showBackupDialog = false) }
    }

    fun showVodHistoryDialog() {
        _uiState.update { it.copy(showVodHistoryDialog = true) }
    }

    fun dismissVodHistoryDialog() {
        _uiState.update { it.copy(showVodHistoryDialog = false) }
    }

    fun showLiveHistoryDialog() {
        _uiState.update { it.copy(showLiveHistoryDialog = true) }
    }

    fun dismissLiveHistoryDialog() {
        _uiState.update { it.copy(showLiveHistoryDialog = false) }
    }

    fun showLiveDialogAction() {
        _uiState.update { it.copy(showLiveDialog = true) }
    }

    fun dismissLiveDialog() {
        _uiState.update { it.copy(showLiveDialog = false) }
    }

    fun showUaDialog() {
        _uiState.update { it.copy(showUaDialog = true) }
    }

    fun dismissUaDialog() {
        _uiState.update { it.copy(showUaDialog = false) }
    }

    fun setUa(ua: String) {
        Setting.putUa(ua)
    }

    fun setProxy(proxy: String) {
        Setting.putProxy(proxy)
    }

    fun setDoh(doh: com.github.catvod.bean.Doh) {
        Setting.putDoh(doh.toString())
    }

    /**
     * Load VOD config
     */
    fun loadVodConfig(config: Config) {
        // Skip if URL is empty
        if (config.url.isNullOrEmpty()) {
            return
        }
        _uiState.update { it.copy(isLoading = true) }
        VodConfig.load(config, object : Callback() {
            override fun success() {
                _uiState.update { it.copy(isLoading = false) }
                RefreshEvent.history()
                RefreshEvent.config()
                RefreshEvent.video()
                // Reload settings to update UI
                loadSettings()
            }

            override fun error(msg: String?) {
                _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
            }
        })
    }

    /**
     * Load Live config
     */
    fun loadLiveConfig(config: Config) {
        // Skip if URL is empty
        if (config.url.isNullOrEmpty()) {
            return
        }
        _uiState.update { it.copy(isLoading = true) }
        LiveConfig.load(config, object : Callback() {
            override fun success() {
                _uiState.update { it.copy(isLoading = false) }
                RefreshEvent.config()
            }

            override fun error(msg: String?) {
                _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
            }
        })
    }

    /**
     * Load Wall config
     */
    fun loadWallConfig(config: Config) {
        // Skip if URL is empty
        if (config.url.isNullOrEmpty()) {
            return
        }
        _uiState.update { it.copy(isLoading = true) }
        WallConfig.load(config, object : Callback() {
            override fun success() {
                _uiState.update { it.copy(isLoading = false) }
                RefreshEvent.config()
            }

            override fun error(msg: String?) {
                _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
            }
        })
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun restoreBackup(file: java.io.File) {
        viewModelScope.launch {
            try {
                com.fongmi.android.tv.db.AppDatabase.restore(file, object : com.fongmi.android.tv.impl.Callback() {
                    override fun success() {
                        // Restore completed
                    }
                })
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

