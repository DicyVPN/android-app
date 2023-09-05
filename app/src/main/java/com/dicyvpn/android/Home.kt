package com.dicyvpn.android

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.navigation.NavHostController
import com.dicyvpn.android.api.API
import com.dicyvpn.android.ui.animateScrollTop
import com.dicyvpn.android.ui.components.ServerSelector
import com.dicyvpn.android.ui.components.StatusCard
import com.dicyvpn.android.ui.components.WorldMap
import com.dicyvpn.android.ui.rememberPreference
import com.dicyvpn.android.ui.theme.Gray500
import com.dicyvpn.android.ui.theme.Gray600
import com.dicyvpn.android.vpn.Status
import kotlinx.coroutines.CoroutineScope
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
    val fetchServers = {
        loading = true
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
    val status by remember { DicyVPN.getStatus() }

    val navigationItems = listOf(
        NavigationItem(
            icon = { Icon(Icons.Rounded.Home, contentDescription = stringResource(R.string.navigation_home)) },
            label = { Text(stringResource(R.string.navigation_home)) },
            selected = true,
            onClick = {}
        ),
        NavigationItem(
            icon = { Icon(Icons.Rounded.Logout, contentDescription = stringResource(R.string.navigation_logout)) },
            label = { Text(stringResource(R.string.navigation_logout)) },
            selected = false,
            onClick = {
                navController.navigate("logout")
            }
        )
    )
    var agreedToUseSecondaryServers by rememberPreference(booleanPreferencesKey("agreedToUseSecondaryServers"), false)
    val showSecondaryServersAgreement = rememberSaveable { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Spacer(Modifier.height(12.dp))
                    navigationItems.forEach { item ->
                        NavigationDrawerItem(
                            icon = { item.icon() },
                            label = { item.label() },
                            selected = item.selected,
                            onClick = {
                                scope.launch { drawerState.close() }
                                item.onClick()
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        ) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
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
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Rounded.Menu,
                                    contentDescription = stringResource(R.string.menu_label),
                                )
                            }
                        }
                    )
                }
            ) { innerPadding ->
                MainColumn(
                    scrollState,
                    scope,
                    status, loading, primaryServers,
                    secondaryServers,
                    agreedToUseSecondaryServers, showSecondaryServersAgreement,
                    fetchServers,
                    columnModifier = modifier.padding(top = innerPadding.calculateTopPadding())
                )
            }
        }
    } else {
        Row(modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
            NavigationRail {
                navigationItems.forEach { item ->
                    NavigationRailItem(
                        icon = item.icon,
                        label = item.label,
                        selected = item.selected,
                        onClick = item.onClick
                    )
                }
            }
            Column(modifier.weight(0.4f)) {
                WorldMap(verticalSpacing = false, modifier.fillMaxHeight())
            }
            MainColumn(
                scrollState,
                scope,
                status, loading, primaryServers,
                secondaryServers,
                agreedToUseSecondaryServers, showSecondaryServersAgreement,
                fetchServers,
                columnModifier = modifier.weight(0.6f)
            )
        }
    }

    if (showSecondaryServersAgreement.value) {
        AlertDialog(
            onDismissRequest = {
                showSecondaryServersAgreement.value = false
            },
            text = {
                Text(stringResource(R.string.secondary_servers_alert_message))
            },
            confirmButton = {
                TextButton({
                    agreedToUseSecondaryServers = true
                    showSecondaryServersAgreement.value = false
                }) {
                    Text(stringResource(R.string.secondary_servers_alert_agree))
                }
            },
            dismissButton = {
                TextButton({
                    showSecondaryServersAgreement.value = false
                }) {
                    Text(stringResource(R.string.secondary_servers_alert_close))
                }
            }
        )
    }

    if (loading) {
        LaunchedEffect(Unit) {
            fetchServers()
        }
    }
}

@Composable
fun MainColumn(
    scrollState: ScrollState,
    scope: CoroutineScope,
    status: Status,
    loading: Boolean,
    primaryServers: Map<String, List<API.ServerList.Server>>,
    secondaryServers: Map<String, List<API.ServerList.Server>>,
    agreedToUseSecondaryServers: Boolean,
    showSecondaryServersAgreement: MutableState<Boolean>,
    fetchServers: () -> Unit,
    modifier: Modifier = Modifier,
    columnModifier: Modifier = modifier
) {
    Column(
        columnModifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .height(IntrinsicSize.Max)
            .background(Gray500),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatusCard(status)
        ServerSelector(
            loading,
            primaryServers,
            secondaryServers,
            onServerClick = { server ->
                scope.launch {
                    scrollState.animateScrollTop(MutatePriority.PreventUserInput)
                }
                onServerClick(server, agreedToUseSecondaryServers, showSecondaryServersAgreement)
            },
            retry = fetchServers,
            surfaceModifier = modifier.fillMaxSize()
        )
    }
}

fun onServerClick(server: API.ServerList.Server, agreedToUseSecondaryServers: Boolean, showSecondaryServersAgreement: MutableState<Boolean>) {
    if (server.type == API.ServerList.Type.SECONDARY && !agreedToUseSecondaryServers) {
        showSecondaryServersAgreement.value = true
        return
    }
    // TODO: Connect to the server
}

private data class NavigationItem(
    val icon: @Composable () -> Unit,
    val label: @Composable () -> Unit,
    val selected: Boolean,
    val onClick: () -> Unit
)
