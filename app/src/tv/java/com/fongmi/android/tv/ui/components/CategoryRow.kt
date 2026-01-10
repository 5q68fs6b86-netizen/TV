package com.fongmi.android.tv.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.ui.theme.TvColors
import com.fongmi.android.tv.ui.theme.TvDimens
import com.fongmi.android.tv.ui.theme.TvTypography

/**
 * Category chip data class
 */
data class CategoryItem(
    val id: String,
    val name: String,
    val isSelected: Boolean = false
)

/**
 * Video item data class for rows
 */
data class VodItem(
    val id: String,
    val siteKey: String,
    val title: String,
    val imageUrl: String?,
    val subtitle: String? = null,
    val badge: String? = null,
    val progress: Float? = null
)

/**
 * Horizontal row of category chips.
 */
@Composable
fun CategoryChipRow(
    categories: List<CategoryItem>,
    onCategoryClick: (CategoryItem) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = TvDimens.ScreenPaddingHorizontal)
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = categories,
            key = { it.id }
        ) { category ->
            CategoryChip(
                text = category.name,
                isSelected = category.isSelected,
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

/**
 * Single category chip component.
 */
@Composable
fun CategoryChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    FocusableChip(
        onClick = onClick,
        modifier = modifier,
        isSelected = isSelected
    ) { isFocused ->
        Text(
            text = text,
            style = TvTypography.CategoryChip,
            color = when {
                isSelected -> TvColors.OnPrimary
                isFocused -> TvColors.TextPrimary
                else -> TvColors.TextSecondary
            },
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        )
    }
}

/**
 * Content row with title and horizontal scrolling video cards.
 */
@Composable
fun ContentRow(
    title: String,
    items: List<VodItem>,
    onItemClick: (VodItem) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = TvDimens.ScreenPaddingHorizontal),
    itemWidth: Dp = TvDimens.CardWidthMedium
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row title
        Text(
            text = title,
            style = TvTypography.TitleLarge,
            color = TvColors.TextPrimary,
            modifier = Modifier.padding(horizontal = TvDimens.ScreenPaddingHorizontal)
        )

        // Horizontal scrolling cards
        LazyRow(
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = items,
                key = { "${it.siteKey}_${it.id}" }
            ) { item ->
                VodCard(
                    title = item.title,
                    imageUrl = item.imageUrl,
                    subtitle = item.subtitle,
                    badge = item.badge,
                    progress = item.progress,
                    width = itemWidth,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

/**
 * Wide content row for landscape videos (16:9).
 */
@Composable
fun WideContentRow(
    title: String,
    items: List<VodItem>,
    onItemClick: (VodItem) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = TvDimens.ScreenPaddingHorizontal)
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row title
        Text(
            text = title,
            style = TvTypography.TitleLarge,
            color = TvColors.TextPrimary,
            modifier = Modifier.padding(horizontal = TvDimens.ScreenPaddingHorizontal)
        )

        // Horizontal scrolling wide cards
        LazyRow(
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = items,
                key = { "${it.siteKey}_${it.id}" }
            ) { item ->
                WideVodCard(
                    title = item.title,
                    imageUrl = item.imageUrl,
                    subtitle = item.subtitle,
                    badge = item.badge,
                    progress = item.progress,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}
