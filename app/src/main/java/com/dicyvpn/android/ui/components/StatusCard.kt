package com.dicyvpn.android.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dicyvpn.android.R
import com.dicyvpn.android.api.API
import com.dicyvpn.android.ui.theme.BrightGreen
import com.dicyvpn.android.ui.theme.DicyVPNTheme
import com.dicyvpn.android.ui.theme.Gray800
import com.dicyvpn.android.ui.theme.Red300
import com.dicyvpn.android.ui.theme.Typography
import com.dicyvpn.android.vpn.Status

@Composable
fun StatusCard(status: Status, lastServer: MutableState<API.ServerList.Server?>, connectToLast: () -> Unit, modifier: Modifier = Modifier) {
    val server = lastServer.value
    val isVPNLoading = status == Status.CONNECTING || status == Status.DISCONNECTING
    val connectButtonLabel = stringResource(
        when (status) {
            Status.CONNECTED -> R.string.label_disconnect
            Status.CONNECTING -> R.string.label_connecting
            Status.DISCONNECTING -> R.string.label_disconnecting
            Status.DISCONNECTED -> R.string.label_connect
        }
    )

    Surface(modifier.fillMaxWidth(), color = Gray800, shadowElevation = 4.dp) {
        if (server != null) {
            Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val notLoadingColor = if (status == Status.CONNECTED) BrightGreen else Red300
                        val loadingAlphaAnimated by rememberInfiniteTransition(label = "loadingAlphaInfinite")
                            .animateFloat(
                                initialValue = 1f, targetValue = 0.7f, animationSpec = infiniteRepeatable(
                                    animation = tween(800, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ), label = "loadingAlpha"
                            )
                        Surface(
                            color = if (isVPNLoading) notLoadingColor.copy(alpha = loadingAlphaAnimated) else notLoadingColor,
                            contentColor = Gray800,
                            modifier = modifier.clip(CircleShape)
                        ) {
                            Icon(
                                imageVector = if (status == Status.CONNECTED) Icons.Rounded.Check else Icons.Rounded.Close,
                                contentDescription = null,
                                modifier = modifier
                                    .padding(2.dp)
                                    .size(14.dp)
                            )
                        }
                    }
                    Text(style = Typography.bodyMedium, text = stringResource(if (status == Status.CONNECTED) R.string.connected else R.string.not_connected))
                }
                Surface(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(1.dp), color = BrightGreen
                ) {}
                Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(server.city)
                    Spacer(modifier = modifier.weight(1f))
                    Text(server.name, fontFamily = FontFamily.Monospace)
                    Flag(country = server.country)
                }
                Button(
                    connectToLast,
                    ButtonTheme.DARK,
                    if (status == Status.CONNECTED || status == Status.DISCONNECTING) ButtonColor.RED else ButtonColor.GREEN,
                    ButtonSize.NORMAL,
                    modifier = modifier.fillMaxWidth(),
                    enabled = !isVPNLoading,
                    focus = true,
                    scrollPadding = Rect(0f, LocalDensity.current.run { 120.dp.toPx() }, 0f, 0f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(connectButtonLabel)
                    }
                }
            }
        } else {
            Text(stringResource(R.string.choose_a_server_from_the_list), textAlign = TextAlign.Center, modifier = modifier.padding(16.dp))
        }
    }
}

@Preview
@Composable
fun StatusCardPreview() {
    DicyVPNTheme {
        StatusCard(Status.CONNECTED, remember { mutableStateOf(null) }, {})
    }
}
