package com.fongmi.android.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * UI State for Danmu Settings
 */
data class DanmuSettingsUiState(
    val fontSize: Int = 1,       // 0=小, 1=中, 2=大, 3=特大
    val speed: Int = 1,          // 0=慢, 1=中, 2=快, 3=极快
    val maxLines: Int = 1,       // 0=3行, 1=5行, 2=8行, 3=不限制
    val alpha: Int = 2,          // 0=25%, 1=50%, 2=75%, 3=100%
    val area: Int = 2,           // 0=1/4屏, 1=1/2屏, 2=3/4屏, 3=全屏
    val showScrolling: Boolean = true,
    val showTop: Boolean = true,
    val showBottom: Boolean = true
)

/**
 * ViewModel for Danmu Settings Screen
 */
@HiltViewModel
class DanmuSettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(DanmuSettingsUiState())
    val uiState: StateFlow<DanmuSettingsUiState> = _uiState.asStateFlow()

    init {
        // Load saved settings (would normally load from SharedPreferences or DataStore)
        loadSettings()
    }

    private fun loadSettings() {
        // TODO: Load from persistent storage
        // For now, use defaults
    }

    fun setFontSize(index: Int) {
        _uiState.update { it.copy(fontSize = index) }
        saveSettings()
    }

    fun setSpeed(index: Int) {
        _uiState.update { it.copy(speed = index) }
        saveSettings()
    }

    fun setMaxLines(index: Int) {
        _uiState.update { it.copy(maxLines = index) }
        saveSettings()
    }

    fun setAlpha(index: Int) {
        _uiState.update { it.copy(alpha = index) }
        saveSettings()
    }

    fun setArea(index: Int) {
        _uiState.update { it.copy(area = index) }
        saveSettings()
    }

    fun setShowScrolling(show: Boolean) {
        _uiState.update { it.copy(showScrolling = show) }
        saveSettings()
    }

    fun setShowTop(show: Boolean) {
        _uiState.update { it.copy(showTop = show) }
        saveSettings()
    }

    fun setShowBottom(show: Boolean) {
        _uiState.update { it.copy(showBottom = show) }
        saveSettings()
    }

    private fun saveSettings() {
        // TODO: Save to persistent storage
        // Preload.putDanmuSize(_uiState.value.fontSize)
        // etc.
    }

    /**
     * Get actual font size value in pixels based on index
     */
    fun getFontSizeValue(): Float {
        return when (_uiState.value.fontSize) {
            0 -> 18f  // 小
            1 -> 24f  // 中
            2 -> 32f  // 大
            3 -> 40f  // 特大
            else -> 24f
        }
    }

    /**
     * Get speed duration multiplier based on index
     */
    fun getSpeedMultiplier(): Float {
        return when (_uiState.value.speed) {
            0 -> 1.5f  // 慢
            1 -> 1.0f  // 中
            2 -> 0.7f  // 快
            3 -> 0.5f  // 极快
            else -> 1.0f
        }
    }

    /**
     * Get max lines value based on index
     */
    fun getMaxLinesValue(): Int {
        return when (_uiState.value.maxLines) {
            0 -> 3
            1 -> 5
            2 -> 8
            3 -> Int.MAX_VALUE  // 不限制
            else -> 5
        }
    }

    /**
     * Get alpha value (0.0 - 1.0) based on index
     */
    fun getAlphaValue(): Float {
        return when (_uiState.value.alpha) {
            0 -> 0.25f
            1 -> 0.5f
            2 -> 0.75f
            3 -> 1.0f
            else -> 0.75f
        }
    }

    /**
     * Get display area ratio based on index
     */
    fun getAreaRatio(): Float {
        return when (_uiState.value.area) {
            0 -> 0.25f  // 1/4屏
            1 -> 0.5f   // 1/2屏
            2 -> 0.75f  // 3/4屏
            3 -> 1.0f   // 全屏
            else -> 0.75f
        }
    }
}
