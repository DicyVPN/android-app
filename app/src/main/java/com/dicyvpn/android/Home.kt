package com.dicyvpn.android

import android.net.VpnService
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.dicyvpn.android.api.API
import com.dicyvpn.android.ui.theme.BrightGreen
import com.dicyvpn.android.ui.theme.Gray600
import com.dicyvpn.android.ui.theme.Gray800
import com.dicyvpn.android.ui.theme.Red300
import com.dicyvpn.android.ui.theme.Shapes
import com.dicyvpn.android.ui.theme.Typography
import com.dicyvpn.android.ui.components.Button
import com.dicyvpn.android.ui.components.ButtonColor
import com.dicyvpn.android.ui.components.ButtonSize
import com.dicyvpn.android.ui.components.ButtonTheme
import com.dicyvpn.android.ui.components.Flag
import com.dicyvpn.android.ui.components.Server
import com.dicyvpn.android.vpn.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(modifier: Modifier = Modifier) {
    var loading by rememberSaveable { mutableStateOf(true) }
    var primaryServers by rememberSaveable { mutableStateOf<Map<String, List<API.ServerList.Server>>>(emptyMap()) }
    var secondaryServers by rememberSaveable { mutableStateOf<Map<String, List<API.ServerList.Server>>>(emptyMap()) }
    var expandedCountry by rememberSaveable { mutableStateOf<String?>(null) }

    val status by remember { DicyVPN.getStatus() }
    val isVPNLoading = status == Status.CONNECTING || status == Status.DISCONNECTING
    val connectButtonLabel = stringResource(
        when (status) {
            Status.CONNECTED -> R.string.label_disconnect
            Status.CONNECTING -> R.string.label_connecting
            Status.DISCONNECTING -> R.string.label_disconnecting
            Status.NOT_RUNNING -> R.string.label_connect
        }
    )

    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scrollState = rememberScrollState()
    val onServerClick = {
        scope.launch {
            scrollState.animateScrollTo(0)
        }
        scope.launch {
            scaffoldState.bottomSheetState.partialExpand()
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 288.dp,
        sheetShadowElevation = 8.dp,
        sheetContent = {
            Column(modifier.verticalScroll(scrollState), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(modifier.fillMaxWidth(), color = Gray800, shadowElevation = 4.dp) {
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
                        Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Germania")
                            Spacer(modifier = modifier.weight(1f))
                            Text(fontFamily = FontFamily.Monospace, text = "DE_01")
                            Image(
                                painterResource(id = R.drawable.flag_de), modifier = modifier
                                    .width(24.dp)
                                    .clip(Shapes.small), contentDescription = null
                            )
                        }
                        val launcherActivity = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult(), onResult = {
                            Log.i("DicyVPN/Home", "VPN permission granted, starting tunnel")
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    DicyVPN.setTunnelUp("") // TODO: Use config
                                }
                            }
                        })
                        Button(
                            {
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        if (status == Status.CONNECTED) {
                                            DicyVPN.setTunnelDown()
                                        } else {
                                            val intent = VpnService.prepare(DicyVPN.get())
                                            if (intent != null) {
                                                launcherActivity.launch(intent)
                                            } else {
                                                Log.i("DicyVPN/Home", "VPN permission already granted")
                                                DicyVPN.setTunnelUp("") // TODO: Use config
                                            }
                                        }
                                    }
                                }
                            },
                            ButtonTheme.DARK,
                            if (status == Status.CONNECTED || status == Status.DISCONNECTING) ButtonColor.RED else ButtonColor.GREEN,
                            ButtonSize.NORMAL,
                            modifier.fillMaxWidth(),
                            !isVPNLoading
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(connectButtonLabel)
                            }
                        }
                    }
                }
                Surface(modifier.fillMaxWidth(), color = Gray800, shadowElevation = 4.dp) {
                    if (loading) {
                        Row(
                            modifier
                                .fillMaxWidth()
                                .padding(top = 28.dp, bottom = 300.dp), horizontalArrangement = Arrangement.Center
                        ) {
                            LinearProgressIndicator()
                        }
                    } else {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(stringResource(R.string.recommended_servers), modifier.padding(12.dp, bottom = 4.dp))
                            primaryServers.forEach { (_, servers) ->
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    servers.forEach { server ->
                                        Server(modifier.clickable { onServerClick() }, server)
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
                                            .clickable {
                                                expandedCountry = if (expandedCountry == country) null else country
                                            }, color = Color.Transparent
                                    ) {
                                        Row(modifier = modifier.padding(16.dp, 8.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Flag(country = country)
                                                Text(country, modifier = modifier.padding(start = 8.dp), color = Color.White) // TODO: Use country name instead of code
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
                                                Server(modifier.clickable { onServerClick() }, server)
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
                }
            }
        }
    ) { innerPadding ->
        Surface(Modifier.padding(innerPadding), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier
                    .padding(top = 8.dp)
                    .fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    color = Gray600,
                    contentColor = Color.White,
                    shape = Shapes.medium,
                    shadowElevation = 8.dp
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.full_logo),
                        contentDescription = stringResource(R.string.dicyvpn_logo),
                        modifier = Modifier
                            .padding(16.dp, 10.dp)
                            .heightIn(max = 40.dp)
                    )
                }
                Surface(
                    modifier
                        .fillMaxWidth()
                        .weight(1f), color = Gray800
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.world_map),
                        contentDescription = stringResource(R.string.world_map),
                        contentScale = ContentScale.Crop,
                        modifier = modifier
                            .fillMaxWidth()
                            .scale(1.5f)
                    )
                }
            }
        }
    }

    if (loading) {
        LaunchedEffect(Unit) {
            API.get().getServersList().enqueue(object : Callback<API.ServerList> {
                override fun onResponse(call: Call<API.ServerList>, response: Response<API.ServerList>) {
                    loading = false
                    if (response.isSuccessful) {
                        val servers = response.body()!!
                        primaryServers = servers.primary
                        secondaryServers = servers.secondary
                    }
                }

                override fun onFailure(call: Call<API.ServerList>, t: Throwable) {
                    Log.e("DicyVPN/API", "Failed to get servers list", t)
                    loading = false
                }
            })
        }
    }
}
