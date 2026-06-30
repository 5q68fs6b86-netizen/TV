package com.fongmi.android.tv.ui.custom

import android.content.Context
import android.util.AttributeSet
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fongmi.android.tv.R

class JetStreamSettingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

    interface Listener {
        fun onSettingAction(key: String)
        fun onSettingLongAction(key: String)
    }

    private data class SectionSpec(
        val key: String,
        val label: String,
        val rows: List<RowSpec>
    )

    private data class RowSpec(
        val key: String,
        val label: String,
        val actions: List<ActionSpec> = emptyList()
    )

    private data class ActionSpec(
        val key: String,
        val label: String,
        val icon: ImageVector
    )

    private var listener: Listener? = null
    private var selectedSectionKey by mutableStateOf(SECTION_SOURCE)
    private var initialFocusRequest by mutableIntStateOf(0)
    private val rowValues = mutableStateMapOf<String, String>()
    private val rowVisible = mutableStateMapOf<String, Boolean>()

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
    }

    @Composable
    override fun Content() {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = Color.White,
                onSurface = Color.White,
                surface = Color.Transparent
            )
        ) {
            SettingsSurface()
        }
    }

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    fun setRowValue(key: String, value: CharSequence?) {
        rowValues[key] = value?.toString().orEmpty()
    }

    fun setRowVisible(key: String, visible: Boolean) {
        rowVisible[key] = visible
    }

    fun showSection(sectionKey: String) {
        selectedSectionKey = sectionKey
    }

    fun requestInitialFocus() {
        initialFocusRequest += 1
    }

    @Composable
    private fun SettingsSurface() {
        val sections = sections()
        val visibleSections = sections.filter { section -> section.rows.any { isRowVisible(it.key) } }
        val selectedSection = visibleSections.firstOrNull { it.key == selectedSectionKey } ?: visibleSections.firstOrNull()
        val selectedRows = selectedSection?.rows.orEmpty().filter { isRowVisible(it.key) }
        val firstRowFocusRequester = remember(selectedSection?.key, selectedRows.firstOrNull()?.key) { FocusRequester() }
        val listState = rememberLazyListState()

        LaunchedEffect(visibleSections.map { it.key }, selectedSectionKey) {
            if (selectedSection == null) return@LaunchedEffect
            if (selectedSection.key != selectedSectionKey) selectedSectionKey = selectedSection.key
        }
        LaunchedEffect(selectedSection?.key) {
            listState.scrollToItem(0)
        }
        LaunchedEffect(initialFocusRequest, selectedSection?.key, selectedRows.firstOrNull()?.key) {
            if (initialFocusRequest > 0 && selectedRows.isNotEmpty()) firstRowFocusRequester.requestFocus()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(pageScrim())
                .padding(horizontal = 48.dp, vertical = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                NavigationPanel(
                    sections = visibleSections,
                    selectedKey = selectedSection?.key.orEmpty(),
                    modifier = Modifier
                        .width(220.dp)
                        .fillMaxHeight()
                )
                ContentPanel(
                    section = selectedSection,
                    rows = selectedRows,
                    firstRowFocusRequester = firstRowFocusRequester,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    listState = listState
                )
            }
        }
    }

    @Composable
    private fun NavigationPanel(sections: List<SectionSpec>, selectedKey: String, modifier: Modifier) {
        Column(
            modifier = modifier
                .clip(RoundedCornerShape(28.dp))
                .background(Color.Black.copy(alpha = 0.42f))
                .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(28.dp))
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(21.dp),
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = context.getString(R.string.home_setting),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                sections.forEach { section ->
                    SectionButton(
                        section = section,
                        selected = section.key == selectedKey,
                        onClick = { selectedSectionKey = section.key }
                    )
                }
            }
        }
    }

    @Composable
    private fun ContentPanel(
        section: SectionSpec?,
        rows: List<RowSpec>,
        firstRowFocusRequester: FocusRequester,
        modifier: Modifier,
        contentPadding: PaddingValues,
        listState: androidx.compose.foundation.lazy.LazyListState
    ) {
        Column(
            modifier = modifier
                .clip(RoundedCornerShape(28.dp))
                .background(Color.Black.copy(alpha = 0.34f))
                .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(28.dp))
                .padding(horizontal = 22.dp, vertical = 18.dp)
        ) {
            Text(
                text = section?.label.orEmpty(),
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(12.dp))
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rows, key = { it.key }) { row ->
                    SettingRow(
                        row = row,
                        focusRequester = if (row.key == rows.firstOrNull()?.key) firstRowFocusRequester else null
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun SectionButton(section: SectionSpec, selected: Boolean, onClick: () -> Unit) {
        val interactionSource = remember { MutableInteractionSource() }
        val focused by interactionSource.collectIsFocusedAsState()
        val scale by animateFloatAsState(if (focused) 1.04f else 1.0f, label = "sectionScale")
        val background by animateColorAsState(
            targetValue = when {
                focused -> Color.White.copy(alpha = 0.24f)
                selected -> Color.White.copy(alpha = 0.18f)
                else -> Color.White.copy(alpha = 0.08f)
            },
            label = "sectionBackground"
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .clip(RoundedCornerShape(22.dp))
                .background(background)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = section.label,
                color = Color.White.copy(alpha = if (selected || focused) 1f else 0.72f),
                fontSize = 16.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun SettingRow(row: RowSpec, focusRequester: FocusRequester?) {
        val interactionSource = remember { MutableInteractionSource() }
        val focused by interactionSource.collectIsFocusedAsState()
        val scale by animateFloatAsState(if (focused) 1.018f else 1.0f, label = "rowScale")
        val background by animateColorAsState(
            targetValue = if (focused) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.10f),
            label = "rowBackground"
        )
        val value = rowValues[row.key].orEmpty()
        val requesterModifier = focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .clip(RoundedCornerShape(18.dp))
                .background(background)
                .then(requesterModifier)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { listener?.onSettingAction(row.key) },
                    onLongClick = { listener?.onSettingLongAction(row.key) }
                )
                .padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                Text(
                    text = row.label,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (value.isNotEmpty() && row.actions.isNotEmpty()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = value,
                        color = Color.White.copy(alpha = 0.68f),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (row.actions.isNotEmpty()) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.actions.forEach { ActionChip(it) }
                }
            } else if (value.isNotEmpty()) {
                Text(
                    text = value,
                    modifier = Modifier.widthIn(max = 320.dp),
                    color = Color.White.copy(alpha = 0.86f),
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun ActionChip(action: ActionSpec) {
        val interactionSource = remember { MutableInteractionSource() }
        val focused by interactionSource.collectIsFocusedAsState()
        val scale by animateFloatAsState(if (focused) 1.06f else 1.0f, label = "chipScale")
        val background by animateColorAsState(
            targetValue = if (focused) Color.White.copy(alpha = 0.26f) else Color.White.copy(alpha = 0.14f),
            label = "chipBackground"
        )
        Row(
            modifier = Modifier
                .height(38.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .clip(RoundedCornerShape(19.dp))
                .background(background)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { listener?.onSettingAction(action.key) },
                    onLongClick = { listener?.onSettingLongAction(action.key) }
                )
                .padding(start = 10.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.label,
                modifier = Modifier.size(19.dp),
                tint = Color.White
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = action.label,
                color = Color.White,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    private fun pageScrim(): Brush {
        return Brush.verticalGradient(
            listOf(
                Color.Black.copy(alpha = 0.72f),
                Color.Black.copy(alpha = 0.46f),
                Color.Black.copy(alpha = 0.78f)
            )
        )
    }

    private fun isRowVisible(key: String): Boolean {
        return rowVisible[key] ?: true
    }

    private fun sections(): List<SectionSpec> {
        return listOf(
            SectionSpec(
                key = SECTION_SOURCE,
                label = context.getString(R.string.setting_section_source),
                rows = listOf(
                    RowSpec(
                        key = KEY_VOD,
                        label = context.getString(R.string.setting_vod),
                        actions = listOf(
                            ActionSpec(KEY_VOD_HOME, context.getString(R.string.setting_home), Icons.Default.Home),
                            ActionSpec(KEY_VOD_HISTORY, context.getString(R.string.setting_history), Icons.Default.History)
                        )
                    ),
                    RowSpec(
                        key = KEY_LIVE,
                        label = context.getString(R.string.setting_live),
                        actions = listOf(
                            ActionSpec(KEY_LIVE_HOME, context.getString(R.string.setting_home), Icons.Default.Home),
                            ActionSpec(KEY_LIVE_HISTORY, context.getString(R.string.setting_history), Icons.Default.History)
                        )
                    ),
                    RowSpec(
                        key = KEY_WALL,
                        label = context.getString(R.string.setting_wall),
                        actions = listOf(
                            ActionSpec(KEY_WALL_DEFAULT, context.getString(R.string.setting_default), Icons.Default.Home),
                            ActionSpec(KEY_WALL_REFRESH, context.getString(R.string.setting_refresh), Icons.Default.Refresh)
                        )
                    )
                )
            ),
            SectionSpec(
                key = SECTION_PLAYBACK,
                label = context.getString(R.string.setting_section_playback),
                rows = listOf(
                    RowSpec(KEY_ENGINE, context.getString(R.string.player_engine)),
                    RowSpec(KEY_RENDER, context.getString(R.string.player_render)),
                    RowSpec(KEY_SCALE, context.getString(R.string.player_scale)),
                    RowSpec(KEY_SPEED, context.getString(R.string.player_speed)),
                    RowSpec(KEY_CAPTION, context.getString(R.string.player_caption)),
                    RowSpec(KEY_BACKGROUND, context.getString(R.string.player_background)),
                    RowSpec(KEY_UA, context.getString(R.string.player_ua)),
                    RowSpec(KEY_MPV_CONF, context.getString(R.string.player_mpv_conf)),
                    RowSpec(KEY_MPV_GPU_NEXT, context.getString(R.string.player_mpv_gpu_next)),
                    RowSpec(KEY_MPV_VULKAN, context.getString(R.string.player_mpv_vulkan)),
                    RowSpec(KEY_ADBLOCK, context.getString(R.string.player_adblock))
                )
            ),
            SectionSpec(
                key = SECTION_DECODE,
                label = context.getString(R.string.setting_section_decode),
                rows = listOf(
                    RowSpec(KEY_TUNNEL, context.getString(R.string.player_tunnel)),
                    RowSpec(KEY_AUDIO_PASS_THROUGH, context.getString(R.string.player_audio_pass_through)),
                    RowSpec(KEY_AUDIO_PREFER, context.getString(R.string.player_audio_decode)),
                    RowSpec(KEY_VIDEO_PREFER, context.getString(R.string.player_video_decode)),
                    RowSpec(KEY_AAC, context.getString(R.string.player_aac_track))
                )
            ),
            SectionSpec(
                key = SECTION_PRELOAD,
                label = context.getString(R.string.setting_section_preload),
                rows = listOf(
                    RowSpec(KEY_PRELOAD, context.getString(R.string.player_preload)),
                    RowSpec(KEY_PRELOAD_THREADS, context.getString(R.string.player_preload_threads)),
                    RowSpec(KEY_PRELOAD_SIZE, context.getString(R.string.player_preload_size)),
                    RowSpec(KEY_PRELOAD_TIME, context.getString(R.string.player_preload_time))
                )
            ),
            SectionSpec(
                key = SECTION_DANMAKU,
                label = context.getString(R.string.setting_section_danmaku),
                rows = listOf(
                    RowSpec(KEY_DANMAKU_LOAD, context.getString(R.string.danmaku_load)),
                    RowSpec(KEY_DANMAKU_API, context.getString(R.string.danmaku_api)),
                    RowSpec(KEY_DANMAKU_AUTO, context.getString(R.string.danmaku_auto_load)),
                    RowSpec(KEY_DANMAKU_SPIDER, context.getString(R.string.danmaku_spider_first))
                )
            ),
            SectionSpec(
                key = SECTION_APP,
                label = context.getString(R.string.setting_section_app),
                rows = listOf(
                    RowSpec(KEY_INCOGNITO, context.getString(R.string.setting_incognito)),
                    RowSpec(KEY_DOH, context.getString(R.string.setting_doh)),
                    RowSpec(KEY_SIZE, context.getString(R.string.setting_size)),
                    RowSpec(KEY_BACKUP, context.getString(R.string.setting_backup)),
                    RowSpec(KEY_RESTORE, context.getString(R.string.setting_restore)),
                    RowSpec(KEY_CACHE, context.getString(R.string.setting_cache), actions = listOf(ActionSpec(KEY_CACHE, context.getString(R.string.setting_clear), Icons.Default.Storage))),
                    RowSpec(KEY_VERSION, context.getString(R.string.setting_version))
                )
            )
        )
    }

    companion object {
        const val SECTION_SOURCE = "source"
        const val SECTION_PLAYBACK = "playback"
        const val SECTION_DECODE = "decode"
        const val SECTION_PRELOAD = "preload"
        const val SECTION_DANMAKU = "danmaku"
        const val SECTION_APP = "app"

        const val KEY_VOD = "vod"
        const val KEY_LIVE = "live"
        const val KEY_WALL = "wall"
        const val KEY_VOD_HOME = "vod_home"
        const val KEY_VOD_HISTORY = "vod_history"
        const val KEY_LIVE_HOME = "live_home"
        const val KEY_LIVE_HISTORY = "live_history"
        const val KEY_WALL_DEFAULT = "wall_default"
        const val KEY_WALL_REFRESH = "wall_refresh"

        const val KEY_ENGINE = "engine"
        const val KEY_RENDER = "render"
        const val KEY_SCALE = "scale"
        const val KEY_SPEED = "speed"
        const val KEY_CAPTION = "caption"
        const val KEY_BACKGROUND = "background"
        const val KEY_UA = "ua"
        const val KEY_MPV_CONF = "mpv_conf"
        const val KEY_MPV_GPU_NEXT = "mpv_gpu_next"
        const val KEY_MPV_VULKAN = "mpv_vulkan"
        const val KEY_ADBLOCK = "adblock"

        const val KEY_TUNNEL = "tunnel"
        const val KEY_AUDIO_PASS_THROUGH = "audio_pass_through"
        const val KEY_AUDIO_PREFER = "audio_prefer"
        const val KEY_VIDEO_PREFER = "video_prefer"
        const val KEY_AAC = "aac"

        const val KEY_PRELOAD = "preload_switch"
        const val KEY_PRELOAD_THREADS = "preload_threads"
        const val KEY_PRELOAD_SIZE = "preload_size"
        const val KEY_PRELOAD_TIME = "preload_time"

        const val KEY_DANMAKU_LOAD = "danmaku_load"
        const val KEY_DANMAKU_API = "danmaku_api"
        const val KEY_DANMAKU_AUTO = "danmaku_auto"
        const val KEY_DANMAKU_SPIDER = "danmaku_spider"

        const val KEY_INCOGNITO = "incognito"
        const val KEY_DOH = "doh"
        const val KEY_SIZE = "size"
        const val KEY_BACKUP = "backup"
        const val KEY_RESTORE = "restore"
        const val KEY_CACHE = "cache"
        const val KEY_VERSION = "version"
    }
}
