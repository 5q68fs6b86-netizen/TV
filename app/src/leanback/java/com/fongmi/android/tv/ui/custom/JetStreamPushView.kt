package com.fongmi.android.tv.ui.custom

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fongmi.android.tv.R
import com.fongmi.android.tv.ui.theme.JetStreamAnimations
import com.fongmi.android.tv.ui.theme.JetStreamTheme
import kotlinx.coroutines.delay

class JetStreamPushView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

    interface Listener {
        fun onCopyAddress(): Boolean
        fun onPushClipboard(): Boolean
        fun onOpenAddress()
    }

    private var addressText by mutableStateOf("")
    private var qrImage by mutableStateOf<Bitmap?>(null)
    private var copied by mutableStateOf(false)
    private var listener: Listener? = null
    private val hideCopied = Runnable { copied = false }

    init {
        isFocusable = false
        isFocusableInTouchMode = false
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
    }

    fun setAddress(address: String) {
        addressText = address
    }

    fun setQrBitmap(bitmap: Bitmap?) {
        qrImage = bitmap
    }

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    fun showCopied() {
        removeCallbacks(hideCopied)
        copied = true
        postDelayed(hideCopied, 2000L)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(hideCopied)
        super.onDetachedFromWindow()
    }

    @Composable
    override fun Content() {
        JetStreamTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp, vertical = 48.dp)
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                ) {
                    if (maxWidth < 920.dp) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .widthIn(max = 520.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(48.dp)
                        ) {
                            QrPanel(Modifier.fillMaxWidth())
                            PushInfoPanel(Modifier.fillMaxWidth())
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth()
                                .widthIn(max = 1152.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(96.dp)
                        ) {
                            PushInfoPanel(
                                modifier = Modifier
                                    .weight(1f)
                                    .widthIn(max = 520.dp)
                            )
                            QrPanel(Modifier.width(496.dp))
                        }
                    }
                }
                CopyToast(
                    visible = copied,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }

    @Composable
    private fun PushInfoPanel(modifier: Modifier) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            PushHeader()
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                AddressBlock()
                PushClipboardButton()
            }
            HintRow()
        }
    }

    @Composable
    private fun PushHeader() {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_push_cast),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Accent
                )
                Text(
                    text = stringResource(id = R.string.push_service).uppercase(),
                    color = Accent,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 1.4.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = stringResource(id = R.string.push_title),
                color = OnSurface,
                fontSize = 60.sp,
                lineHeight = 64.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(id = R.string.push_desc),
                modifier = Modifier.widthIn(max = 448.dp),
                color = OnSurfaceVariant,
                fontSize = 20.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }

    @Composable
    private fun AddressBlock() {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_push_monitor),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MutedText
                )
                Text(
                    text = stringResource(id = R.string.push_browser_address),
                    color = MutedText,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Surface)
                    .border(1.dp, Outline.copy(alpha = 0.52f), RoundedCornerShape(20.dp))
                    .padding(start = 24.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = addressText,
                    modifier = Modifier.weight(1f),
                    color = Accent,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(8.dp))
                CopyAddressButton()
            }
        }
    }

    @Composable
    private fun CopyAddressButton() {
        val interactionSource = remember { MutableInteractionSource() }
        val focused by interactionSource.collectIsFocusedAsState()
        val scale by animateFloatAsState(
            targetValue = if (focused) 1.08f else 1f,
            animationSpec = JetStreamAnimations.ScaleSpring,
            label = "pushCopyScale"
        )
        val background by animateColorAsState(
            targetValue = if (focused) Outline else Color.Transparent,
            animationSpec = JetStreamAnimations.ColorTween,
            label = "pushCopyBackground"
        )
        Box(
            modifier = Modifier
                .size(52.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .clip(RoundedCornerShape(12.dp))
                .background(background)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        if (listener?.onCopyAddress() == true) showCopied()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = if (copied) R.drawable.msr_check else R.drawable.msr_content_copy),
                contentDescription = stringResource(id = R.string.push_copy_url),
                modifier = Modifier.size(22.dp),
                tint = if (copied) Success else OnSurfaceVariant
            )
        }
    }

    @Composable
    private fun PushClipboardButton() {
        var pushed by remember { mutableStateOf(false) }
        val interactionSource = remember { MutableInteractionSource() }
        val focused by interactionSource.collectIsFocusedAsState()
        val scale by animateFloatAsState(
            targetValue = when {
                pushed -> 0.95f
                focused -> 1.02f
                else -> 1f
            },
            animationSpec = JetStreamAnimations.ScaleSpring,
            label = "pushClipboardScale"
        )
        val background by animateColorAsState(
            targetValue = when {
                pushed -> SuccessContainer
                focused -> AccentFocused
                else -> Accent
            },
            animationSpec = JetStreamAnimations.ColorTween,
            label = "pushClipboardBackground"
        )
        val contentColor by animateColorAsState(
            targetValue = if (pushed) SuccessOnContainer else AccentOn,
            animationSpec = JetStreamAnimations.ColorTween,
            label = "pushClipboardContent"
        )

        LaunchedEffect(pushed) {
            if (pushed) {
                delay(2000L)
                pushed = false
            }
        }

        Row(
            modifier = Modifier
                .height(56.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .clip(CircleShape)
                .background(background)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        if (listener?.onPushClipboard() == true) pushed = true
                    }
                )
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = if (pushed) R.drawable.msr_check else R.drawable.msr_content_paste),
                contentDescription = null,
                modifier = Modifier.size(25.dp),
                tint = contentColor
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(id = if (pushed) R.string.push_clip_sent else R.string.push_clip),
                color = contentColor,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    @Composable
    private fun HintRow() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 0.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(Outline.copy(alpha = 0.42f))
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_push_info),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MutedText
                )
                Text(
                    text = stringResource(id = R.string.push_wifi_hint),
                    color = MutedText,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }

    @Composable
    private fun QrPanel(modifier: Modifier) {
        val panelShape = RoundedCornerShape(40.dp)
        Column(
            modifier = modifier
                .shadow(22.dp, panelShape, clip = false)
                .clip(panelShape)
                .background(Surface)
                .border(1.dp, Outline.copy(alpha = 0.32f), panelShape)
                .padding(horizontal = 56.dp, vertical = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            QrImageCard()
            Spacer(Modifier.height(32.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.msr_smartphone),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    tint = Accent
                )
                Text(
                    text = stringResource(id = R.string.push_scan_title),
                    color = OnSurface,
                    fontSize = 24.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.push_scan_desc),
                color = MutedText,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    @Composable
    private fun QrImageCard() {
        val interactionSource = remember { MutableInteractionSource() }
        val focused by interactionSource.collectIsFocusedAsState()
        val shape = RoundedCornerShape(28.dp)
        val scale by animateFloatAsState(
            targetValue = if (focused) 1.05f else 1f,
            animationSpec = JetStreamAnimations.ScaleSpring,
            label = "pushQrScale"
        )
        val borderColor by animateColorAsState(
            targetValue = if (focused) Accent else Color.Transparent,
            animationSpec = JetStreamAnimations.ColorTween,
            label = "pushQrBorder"
        )
        Box(
            modifier = Modifier
                .size(288.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .shadow(14.dp, shape, clip = false)
                .clip(shape)
                .background(Color.White)
                .border(3.dp, borderColor, shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { listener?.onOpenAddress() }
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            val bitmap = qrImage
            if (bitmap == null) {
                Text(
                    text = "QR",
                    color = Color.Black,
                    fontSize = 22.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }

    @Composable
    private fun CopyToast(visible: Boolean, modifier: Modifier) {
        AnimatedVisibility(
            visible = visible,
            modifier = modifier,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
        ) {
            Row(
                modifier = Modifier
                    .shadow(12.dp, CircleShape, clip = false)
                    .clip(CircleShape)
                    .background(OnSurface)
                    .padding(horizontal = 22.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.msr_check),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = SuccessOnContainer
                )
                Text(
                    text = stringResource(id = R.string.push_copied_url),
                    color = Background,
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    private companion object {
        val Background = Color(0xFF131314)
        val Surface = Color(0xFF1E1F22)
        val Outline = Color(0xFF444746)
        val OnSurface = Color(0xFFE2E2E2)
        val OnSurfaceVariant = Color(0xFFC4C7C5)
        val MutedText = Color(0xFF8E918F)
        val Accent = Color(0xFFA8C7FA)
        val AccentFocused = Color(0xFFD3E3FD)
        val AccentOn = Color(0xFF062E6F)
        val Success = Color(0xFF4ADE80)
        val SuccessContainer = Color(0xFFBBF7D0)
        val SuccessOnContainer = Color(0xFF14532D)
    }
}
