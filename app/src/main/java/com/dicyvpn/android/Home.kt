package com.dicyvpn.android

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dicyvpn.android.api.API
import com.dicyvpn.android.ui.animateScrollTop
import com.dicyvpn.android.ui.components.ServerSelector
import com.dicyvpn.android.ui.components.StatusCard
import com.dicyvpn.android.ui.components.WorldMap
import com.dicyvpn.android.ui.theme.Gray600
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(navController: NavHostController, windowSizeClass: WindowSizeClass, modifier: Modifier = Modifier) {
    var loading by rememberSaveable { mutableStateOf(true) }
    var primaryServers by rememberSaveable { mutableStateOf<Map<String, List<API.ServerList.Server>>>(emptyMap()) }
    var secondaryServers by rememberSaveable { mutableStateOf<Map<String, List<API.ServerList.Server>>>(emptyMap()) }
    val onServerClick = {
    }
    val status by remember { DicyVPN.getStatus() }

    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scrollState = rememberScrollState()

    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 300.dp,
            sheetShadowElevation = 8.dp,
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Gray600),
                    title = {
                        Image(
                            painter = painterResource(id = R.drawable.full_logo),
                            contentDescription = stringResource(R.string.dicyvpn_logo),
                            modifier = Modifier
                                .padding(16.dp, 10.dp)
                                .heightIn(max = 40.dp)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { /* doSomething() */ }) {
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                contentDescription = stringResource(R.string.menu_label),
                            )
                        }
                    }
                )
            },
            sheetContent = {
                Column(modifier.verticalScroll(scrollState), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusCard(status)
                    ServerSelector(
                        loading,
                        primaryServers,
                        secondaryServers,
                        onServerClick = {
                            scope.launch {
                                scrollState.animateScrollTop(MutatePriority.PreventUserInput)
                            }
                            scope.launch {
                                scaffoldState.bottomSheetState.partialExpand()
                            }
                            onServerClick()
                        }
                    )
                }
            }
        ) { innerPadding ->
            WorldMap(verticalSpacing = true, modifier.padding(innerPadding))
        }
    } else {
        Row {
            NavigationRail {
                NavigationRailItem(
                    icon = { Icon(Icons.Rounded.Home, contentDescription = stringResource(R.string.navigation_home)) },
                    label = { Text(stringResource(R.string.navigation_home)) },
                    selected = true,
                    onClick = {}
                )
                NavigationRailItem(
                    icon = { Icon(Icons.Rounded.Logout, contentDescription = stringResource(R.string.navigation_logout)) },
                    label = { Text(stringResource(R.string.navigation_logout)) },
                    selected = false,
                    onClick = {
                        navController.navigate("logout")
                    }
                )
            }
            Column(modifier.weight(0.4f)) {
                WorldMap(verticalSpacing = false, modifier.fillMaxHeight())
            }
            Column(
                modifier
                    .weight(0.6f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusCard(status)
                ServerSelector(
                    loading,
                    primaryServers,
                    secondaryServers,
                    onServerClick,
                    fillLoadingHeight = true
                )
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
