package com.fongmi.android.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.fongmi.android.tv.App
import com.fongmi.android.tv.R
import com.fongmi.android.tv.Setting
import com.fongmi.android.tv.event.RefreshEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale
import javax.inject.Inject

data class CustomSettingsUiState(
    val qualityText: String = "",
    val sizeText: String = "",
    val episodeText: String = "",
    val speedText: String = "",
    val homeUIText: String = "",
    val fullscreenMenuKeyText: String = "",
    val smallWindowBackKeyText: String = "",
    val homeMenuKeyText: String = "",
    val homeSiteLockText: String = "",
    val homeHistoryText: String = "",
    val aggregatedSearchText: String = "",
    val incognitoText: String = "",
    val removeAdText: String = "",
    val parseWebViewText: String = "",
    val configCacheText: String = "",
    val languageText: String = "",
    val showResetDialog: Boolean = false
)

@HiltViewModel
class CustomSettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CustomSettingsUiState())
    val uiState: StateFlow<CustomSettingsUiState> = _uiState.asStateFlow()

    private val quality: Array<String> = getStringArray(R.array.select_quality)
    private val size: Array<String> = getStringArray(R.array.select_size)
    private val episode: Array<String> = getStringArray(R.array.select_episode)
    private val homeUI: Array<String> = getStringArray(R.array.select_home_ui)
    private val fullscreenMenuKey: Array<String> = getStringArray(R.array.select_fullscreen_menu_key)
    private val smallWindowBackKey: Array<String> = getStringArray(R.array.select_small_window_back_key)
    private val homeMenuKey: Array<String> = getStringArray(R.array.select_home_menu_key)
    private val parseWebView: Array<String> = getStringArray(R.array.select_parse_webview)
    private val configCache: Array<String> = getStringArray(R.array.select_config_cache)
    private val language: Array<String> = getStringArray(R.array.select_language)

    init {
        loadSettings()
    }

    private fun getStringArray(resId: Int): Array<String> {
        return App.get().resources.getStringArray(resId)
    }

    private fun getSwitch(value: Boolean): String {
        return App.get().getString(if (value) R.string.setting_on else R.string.setting_off)
    }

    private fun loadSettings() {
        _uiState.update {
            it.copy(
                qualityText = quality.getOrElse(Setting.getQuality()) { quality[0] },
                sizeText = size.getOrElse(Setting.getSize()) { size[0] },
                episodeText = episode.getOrElse(Setting.getEpisode()) { episode[0] },
                speedText = String.format(Locale.getDefault(), "%.2f", Setting.getPlaySpeed()),
                homeUIText = homeUI.getOrElse(Setting.getHomeUI()) { homeUI[0] },
                fullscreenMenuKeyText = fullscreenMenuKey.getOrElse(Setting.getFullscreenMenuKey()) { fullscreenMenuKey[0] },
                smallWindowBackKeyText = smallWindowBackKey.getOrElse(Setting.getSmallWindowBackKey()) { smallWindowBackKey[0] },
                homeMenuKeyText = homeMenuKey.getOrElse(Setting.getHomeMenuKey()) { homeMenuKey[0] },
                homeSiteLockText = getSwitch(Setting.isHomeSiteLock()),
                homeHistoryText = getSwitch(Setting.isHomeHistory()),
                aggregatedSearchText = getSwitch(Setting.isAggregatedSearch()),
                incognitoText = getSwitch(Setting.isIncognito()),
                removeAdText = getSwitch(Setting.isRemoveAd()),
                parseWebViewText = parseWebView.getOrElse(Setting.getParseWebView()) { parseWebView[0] },
                configCacheText = configCache.getOrElse(Setting.getConfigCache()) { configCache[0] },
                languageText = language.getOrElse(Setting.getLanguage()) { language[0] }
            )
        }
    }

    fun toggleQuality() {
        val index = (Setting.getQuality() + 1) % quality.size
        Setting.putQuality(index)
        _uiState.update { it.copy(qualityText = quality[index]) }
        RefreshEvent.image()
    }

    fun toggleSize() {
        val index = (Setting.getSize() + 1) % size.size
        Setting.putSize(index)
        _uiState.update { it.copy(sizeText = size[index]) }
        RefreshEvent.size()
    }

    fun toggleEpisode() {
        val index = (Setting.getEpisode() + 1) % episode.size
        Setting.putEpisode(index)
        _uiState.update { it.copy(episodeText = episode[index]) }
    }

    fun toggleSpeed() {
        var speed = Setting.getPlaySpeed()
        val addon = if (speed >= 2) 1.0f else 0.1f
        speed = if (speed >= 5) 0.2f else minOf(speed + addon, 5.0f)
        Setting.putPlaySpeed(speed)
        _uiState.update {
            it.copy(speedText = String.format(Locale.getDefault(), "%.2f", speed))
        }
    }

    fun resetSpeed() {
        Setting.putPlaySpeed(1.0f)
        _uiState.update {
            it.copy(speedText = String.format(Locale.getDefault(), "%.2f", 1.0f))
        }
    }

    fun toggleHomeUI() {
        val index = (Setting.getHomeUI() + 1) % homeUI.size
        Setting.putHomeUI(index)
        _uiState.update { it.copy(homeUIText = homeUI[index]) }
    }

    fun toggleFullscreenMenuKey() {
        val index = (Setting.getFullscreenMenuKey() + 1) % fullscreenMenuKey.size
        Setting.putFullscreenMenuKey(index)
        _uiState.update { it.copy(fullscreenMenuKeyText = fullscreenMenuKey[index]) }
    }

    fun toggleSmallWindowBackKey() {
        val index = (Setting.getSmallWindowBackKey() + 1) % smallWindowBackKey.size
        Setting.putSmallWindowBackKey(index)
        _uiState.update { it.copy(smallWindowBackKeyText = smallWindowBackKey[index]) }
    }

    fun toggleHomeMenuKey() {
        val index = (Setting.getHomeMenuKey() + 1) % homeMenuKey.size
        Setting.putHomeMenuKey(index)
        _uiState.update { it.copy(homeMenuKeyText = homeMenuKey[index]) }
    }

    fun toggleHomeSiteLock() {
        Setting.putHomeSiteLock(!Setting.isHomeSiteLock())
        _uiState.update { it.copy(homeSiteLockText = getSwitch(Setting.isHomeSiteLock())) }
    }

    fun toggleHomeHistory() {
        Setting.putHomeHistory(!Setting.isHomeHistory())
        _uiState.update { it.copy(homeHistoryText = getSwitch(Setting.isHomeHistory())) }
    }

    fun toggleAggregatedSearch() {
        Setting.putAggregatedSearch(!Setting.isAggregatedSearch())
        _uiState.update { it.copy(aggregatedSearchText = getSwitch(Setting.isAggregatedSearch())) }
    }

    fun toggleIncognito() {
        Setting.putIncognito(!Setting.isIncognito())
        _uiState.update { it.copy(incognitoText = getSwitch(Setting.isIncognito())) }
    }

    fun toggleRemoveAd() {
        Setting.putRemoveAd(!Setting.isRemoveAd())
        _uiState.update { it.copy(removeAdText = getSwitch(Setting.isRemoveAd())) }
    }

    fun toggleParseWebView() {
        val index = (Setting.getParseWebView() + 1) % parseWebView.size
        Setting.putParseWebView(index)
        _uiState.update { it.copy(parseWebViewText = parseWebView[index]) }
    }

    fun toggleConfigCache() {
        val index = (Setting.getConfigCache() + 1) % configCache.size
        Setting.putConfigCache(index)
        _uiState.update { it.copy(configCacheText = configCache[index]) }
    }

    fun toggleLanguage() {
        val index = (Setting.getLanguage() + 1) % language.size
        Setting.putLanguage(index)
        _uiState.update { it.copy(languageText = language[index]) }
    }

    fun showResetDialog() {
        _uiState.update { it.copy(showResetDialog = true) }
    }

    fun dismissResetDialog() {
        _uiState.update { it.copy(showResetDialog = false) }
    }

    fun resetApp() {
        dismissResetDialog()
        Thread {
            com.github.catvod.utils.Shell.exec("pm clear ${App.get().packageName}")
        }.start()
    }
}
