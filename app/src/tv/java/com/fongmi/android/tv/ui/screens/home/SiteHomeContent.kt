package com.fongmi.android.tv.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.bean.Class
import com.fongmi.android.tv.bean.Vod
import com.fongmi.android.tv.ui.components.BannerItem
import com.fongmi.android.tv.ui.components.ContentItem
import com.fongmi.android.tv.ui.components.ContentRow
import com.fongmi.android.tv.ui.components.HeroBanner

/**
 * Site content view - displays content from the current site source
 * Extracted from HomeScreen for use in dual-mode switching
 */
@Composable
fun SiteHomeContent(
    siteName: String,
    categories: List<Class>,
    featuredVods: List<Vod>,
    recommendedVods: List<Vod>,
    onCategoryClick: (String) -> Unit,
    onVodClick: (String, String) -> Unit,
    onActionClick: ((String) -> Unit)? = null
) {
    val scrollState = rememberScrollState()

    // Helper to handle vod click with action check
    val handleVodClick: (Vod) -> Unit = { vod ->
        if (vod.isAction && onActionClick != null) {
            onActionClick(vod.action ?: "")
        } else {
            onVodClick("", vod.vodId ?: "")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Hero Banner (featured content)
        if (featuredVods.isNotEmpty()) {
            // Keep reference to original vods for action check
            val bannerVods = featuredVods.take(5)
            HeroBanner(
                items = bannerVods.map { vod ->
                    BannerItem(
                        id = vod.vodId ?: "",
                        title = vod.vodName ?: "",
                        subtitle = vod.vodYear ?: vod.typeName,
                        description = vod.vodContent,
                        imageUrl = vod.vodPic,
                        vodId = vod.vodId
                    )
                },
                onItemClick = { banner ->
                    val vod = bannerVods.find { it.vodId == banner.vodId }
                    if (vod != null) {
                        handleVodClick(vod)
                    } else {
                        banner.vodId?.let { vodId -> onVodClick("", vodId) }
                    }
                },
                height = 380.dp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Categories Row (horizontal scrollable categories)
        if (categories.isNotEmpty()) {
            ContentRow(
                title = "分类",
                items = categories.take(10).map { cls ->
                    ContentItem(
                        id = cls.typeId ?: "",
                        title = cls.typeName ?: "",
                        imageUrl = null,
                        siteKey = cls.typeId
                    )
                },
                onItemClick = { item ->
                    item.siteKey?.let { onCategoryClick(it) }
                },
                onSeeAllClick = { /* Navigate to all categories */ },
                cardWidth = 120.dp,
                cardAspectRatio = 1f
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recommended Content Row
        if (recommendedVods.isNotEmpty()) {
            val recVods = recommendedVods.take(15)
            ContentRow(
                title = "推荐",
                items = recVods.map { vod ->
                    ContentItem(
                        id = vod.vodId ?: "",
                        title = vod.vodName ?: "",
                        imageUrl = vod.vodPic,
                        subtitle = vod.vodYear ?: vod.vodRemarks,
                        badge = vod.vodRemarks,
                        vodId = vod.vodId
                    )
                },
                onItemClick = { item ->
                    val vod = recVods.find { it.vodId == item.vodId }
                    if (vod != null) {
                        handleVodClick(vod)
                    } else {
                        item.vodId?.let { vodId -> onVodClick(item.siteKey ?: "", vodId) }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Additional content rows based on categories
        categories.take(3).forEach { category ->
            val categoryVods = recommendedVods
                .filter { it.typeName == category.typeName }
                .take(10)

            if (categoryVods.isNotEmpty()) {
                ContentRow(
                    title = category.typeName ?: "",
                    items = categoryVods.map { vod ->
                        ContentItem(
                            id = vod.vodId ?: "",
                            title = vod.vodName ?: "",
                            imageUrl = vod.vodPic,
                            subtitle = vod.vodYear,
                            badge = vod.vodRemarks,
                            vodId = vod.vodId
                        )
                    },
                    onItemClick = { item ->
                        val vod = categoryVods.find { it.vodId == item.vodId }
                        if (vod != null) {
                            handleVodClick(vod)
                        } else {
                            item.vodId?.let { vodId -> onVodClick(item.siteKey ?: "", vodId) }
                        }
                    },
                    onSeeAllClick = {
                        category.typeId?.let { onCategoryClick(it) }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}
