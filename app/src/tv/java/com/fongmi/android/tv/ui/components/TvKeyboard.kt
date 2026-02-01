package com.fongmi.android.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.ui.theme.TvTypography

/**
 * TV keyboard key types
 */
enum class KeyType { DELETE, SEARCH }

/**
 * Sealed class representing keyboard key items
 */
sealed class KeyItem {
    data class Text(val char: String) : KeyItem()
    data class Icon(val type: KeyType) : KeyItem()
}

/**
 * Keyboard data: function keys + A-Z + 0-9
 */
private val KEYBOARD_KEYS = listOf(
    KeyItem.Icon(KeyType.DELETE),
    KeyItem.Icon(KeyType.SEARCH),
    KeyItem.Text("A"), KeyItem.Text("B"), KeyItem.Text("C"), KeyItem.Text("D"),
    KeyItem.Text("E"), KeyItem.Text("F"), KeyItem.Text("G"), KeyItem.Text("H"),
    KeyItem.Text("I"), KeyItem.Text("J"), KeyItem.Text("K"), KeyItem.Text("L"),
    KeyItem.Text("M"), KeyItem.Text("N"), KeyItem.Text("O"), KeyItem.Text("P"),
    KeyItem.Text("Q"), KeyItem.Text("R"), KeyItem.Text("S"), KeyItem.Text("T"),
    KeyItem.Text("U"), KeyItem.Text("V"), KeyItem.Text("W"), KeyItem.Text("X"),
    KeyItem.Text("Y"), KeyItem.Text("Z"),
    KeyItem.Text("0"), KeyItem.Text("1"), KeyItem.Text("2"), KeyItem.Text("3"),
    KeyItem.Text("4"), KeyItem.Text("5"), KeyItem.Text("6"), KeyItem.Text("7"),
    KeyItem.Text("8"), KeyItem.Text("9"),
)

/**
 * TV-optimized A-Z keyboard for remote control input.
 * 6-column grid layout with function keys and alphanumeric characters.
 *
 * @param onCharClick Called when a character key (A-Z, 0-9) is pressed
 * @param onDeleteClick Called when the delete key is pressed
 * @param onDeleteLongClick Called when the delete key is long-pressed (clear all)
 * @param onSearchClick Called when the search key is pressed
 * @param modifier Modifier for the keyboard container
 */
@Composable
fun TvKeyboard(
    onCharClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onDeleteLongClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        items(KEYBOARD_KEYS) { key ->
            KeyButton(
                key = key,
                onCharClick = onCharClick,
                onDeleteClick = onDeleteClick,
                onDeleteLongClick = onDeleteLongClick,
                onSearchClick = onSearchClick
            )
        }
    }
}

@Composable
private fun KeyButton(
    key: KeyItem,
    onCharClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onDeleteLongClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    FocusableItem(
        onClick = {
            when (key) {
                is KeyItem.Text -> onCharClick(key.char)
                is KeyItem.Icon -> when (key.type) {
                    KeyType.DELETE -> onDeleteClick()
                    KeyType.SEARCH -> onSearchClick()
                }
            }
        },
        onLongClick = if (key is KeyItem.Icon && key.type == KeyType.DELETE) {
            { onDeleteLongClick() }
        } else null,
        focusScale = 1.15f,
        focusBorderWidth = 1.5.dp,
        focusElevation = 4.dp
    ) { isFocused ->
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    color = if (isFocused) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            when (key) {
                is KeyItem.Text -> Text(
                    text = key.char,
                    style = TvTypography.TitleMedium,
                    color = if (isFocused) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface
                )
                is KeyItem.Icon -> Icon(
                    imageVector = when (key.type) {
                        KeyType.DELETE -> Icons.AutoMirrored.Filled.Backspace
                        KeyType.SEARCH -> Icons.Default.Search
                    },
                    contentDescription = when (key.type) {
                        KeyType.DELETE -> "Delete"
                        KeyType.SEARCH -> "Search"
                    },
                    tint = if (isFocused) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
