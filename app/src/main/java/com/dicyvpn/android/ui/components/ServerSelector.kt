package com.dicyvpn.android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dicyvpn.android.R
import com.dicyvpn.android.api.API
import com.dicyvpn.android.ui.dpadFocusable
import com.dicyvpn.android.ui.theme.DicyVPNTheme
import com.dicyvpn.android.ui.theme.Gray600
import com.dicyvpn.android.ui.theme.Gray800
import java.util.Locale

@Composable
fun ServerSelector(
    loading: Boolean,
    primaryServers: Map<String, List<API.ServerList.Server>>,
    secondaryServers: Map<String, List<API.ServerList.Server>>,
    onServerClick: () -> Unit,
    modifier: Modifier = Modifier,
    fillLoadingHeight: Boolean = false
) {
    var expandedCountry by rememberSaveable { mutableStateOf<String?>(null) }
    val scrollPaddingVertical = LocalDensity.current.run { 48.dp.toPx() }
    val scrollPadding = Rect(0f, scrollPaddingVertical, 0f, scrollPaddingVertical)

    Surface(modifier.fillMaxWidth(), color = Gray800, shadowElevation = 4.dp) {
        if (loading) {
            if (fillLoadingHeight) {
                Column(modifier.heightIn(min = 64.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    LinearProgressIndicator()
                }
            } else {
                Row(
                    modifier
                        .fillMaxWidth()
                        .padding(top = 38.dp, bottom = 300.dp), horizontalArrangement = Arrangement.Center
                ) {
                    LinearProgressIndicator()
                }
            }
        } else {
            Column(
                modifier
                    .fillMaxWidth()
                    .padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(R.string.recommended_servers), modifier.padding(12.dp, bottom = 4.dp))
                primaryServers.forEach { (_, servers) ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        servers.forEach { server ->
                            Server(modifier.dpadFocusable(onClick = onServerClick, scrollPadding = scrollPadding), server)
                        }
                    }
                }
                Text(stringResource(R.string.other_servers), modifier.padding(12.dp, 4.dp))
                secondaryServers.forEach { (country, servers) ->
                    val rotation by animateFloatAsState(if (expandedCountry == country) 180f else 0f, label = "rotation")
                    Column { // wrap in another column to prevent 8.dp spacing from being applied to the animated visibility
                        Surface(
                            modifier
                                .padding(bottom = 8.dp)
                                .dpadFocusable(onClick = {
                                    expandedCountry = if (expandedCountry == country) null else country
                                }, scrollPadding = scrollPadding), color = Color.Transparent
                        ) {
                            Row(modifier = modifier.padding(16.dp, 8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Flag(country = country)
                                    Text(Locale("", country).displayCountry, modifier = modifier.padding(start = 8.dp), color = Color.White)
                                }
                                Spacer(modifier = modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = stringResource(if (expandedCountry == country) R.string.collapse else R.string.expand),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = modifier
                                        .scale(1.2f)
                                        .rotate(rotation)
                                )
                            }
                        }
                        AnimatedVisibility(expandedCountry == country) {
                            Column(modifier.padding(bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                servers.forEach { server ->
                                    Server(modifier.dpadFocusable(onClick = onServerClick, scrollPadding = scrollPadding), server)
                                }
                            }
                        }
                        Surface(
                            modifier = modifier
                                .fillMaxWidth()
                                .height(1.dp), color = Gray600
                        ) {}
                    }
                }
            }
        }
        // TODO: if loading = false and primaryServers and secondaryServers are empty, show retry button
    }
}

@Preview
@Composable
fun ServerSelectorPreview() {
    val servers = mapOf(
        "US" to listOf(
            API.ServerList.Server("US_1", "US", "primary", "US", "New York", 0.5),
            API.ServerList.Server("US_2", "US", "primary", "US", "Washington", 0.17),
            API.ServerList.Server("US_3", "US", "primary", "US", "Los Angeles", 0.33)
        ),
        "DE" to listOf(
            API.ServerList.Server("DE_1", "DE", "primary", "DE", "Frankfurt", 0.5),
            API.ServerList.Server("DE_2", "DE", "primary", "DE", "Berlin", 0.17),
            API.ServerList.Server("DE_3", "DE", "primary", "DE", "Munich", 0.33)
        )
    )

    DicyVPNTheme {
        ServerSelector(false, servers, servers, {})
    }
}
