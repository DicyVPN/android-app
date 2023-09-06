package com.dicyvpn.android

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.dicyvpn.android.api.API
import com.dicyvpn.android.vpn.Status
import com.dicyvpn.android.vpn.VPNTunnel
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.StringReader
import java.lang.ref.WeakReference
import java.util.Locale

class DicyVPN : Application() {
    private var userAgent = "DicyVPN/" + BuildConfig.VERSION_NAME + " (Android)"
    private val coroutineScope = CoroutineScope(Job() + Dispatchers.Main.immediate)
    private val status: MutableState<Status> = mutableStateOf(Status.NOT_RUNNING)
    private val lastServer: MutableState<API.ServerList.Server?> = mutableStateOf(null)
    private var backend: GoBackend? = null
    private val tunnel: VPNTunnel = VPNTunnel(status)
    private lateinit var preferencesDataStore: DataStore<Preferences>

    override fun onCreate() {
        super.onCreate()
        val isTV = packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
        userAgent = String.format(
            Locale.ENGLISH,
            "DicyVPN/%s (Android%s %s; SDK %d; %s; %s; %s %s)",
            BuildConfig.VERSION_NAME,
            if (isTV) " TV" else "",
            Build.VERSION.RELEASE,
            Build.VERSION.SDK_INT,
            if (Build.SUPPORTED_ABIS.isNotEmpty()) Build.SUPPORTED_ABIS[0] else "Unknown ABI",
            Build.BOARD,
            Build.MANUFACTURER,
            Build.MODEL
        )
        Log.i(TAG, userAgent)

        preferencesDataStore = PreferenceDataStoreFactory.create {
            applicationContext.preferencesDataStoreFile("settings")
        }

        coroutineScope.launch(Dispatchers.IO) {
            try {
                backend = GoBackend(applicationContext)

                lastServer.value = preferencesDataStore.data.map { preferences ->
                    val id = preferences[stringPreferencesKey("lastServer.id")]
                    if (id == null) {
                        return@map null
                    } else {
                        val name = preferences[stringPreferencesKey("lastServer.name")]!!
                        val type = API.ServerList.Type.valueOf(preferences[stringPreferencesKey("lastServer.type")]!!)
                        val country = preferences[stringPreferencesKey("lastServer.country")]!!
                        val city = preferences[stringPreferencesKey("lastServer.city")]!!
                        Log.d(TAG, "Loaded last server: $id, $name, $type, $country, $city")
                        return@map API.ServerList.Server(id, name, type, country, city, 0.0)
                    }
                }.first()

                snapshotFlow { lastServer.value }
                    .onEach {
                        preferencesDataStore.edit { preferences ->
                            if (it == null) {
                                Log.d(TAG, "Removing last server")
                                preferences.remove(stringPreferencesKey("lastServer.id"))
                                preferences.remove(stringPreferencesKey("lastServer.name"))
                                preferences.remove(stringPreferencesKey("lastServer.type"))
                                preferences.remove(stringPreferencesKey("lastServer.country"))
                                preferences.remove(stringPreferencesKey("lastServer.city"))
                            } else {
                                Log.d(TAG, "Saving last server: ${it.id}")
                                preferences[stringPreferencesKey("lastServer.id")] = it.id
                                preferences[stringPreferencesKey("lastServer.name")] = it.name
                                preferences[stringPreferencesKey("lastServer.type")] = it.type.name
                                preferences[stringPreferencesKey("lastServer.country")] = it.country
                                preferences[stringPreferencesKey("lastServer.city")] = it.city
                            }
                        }
                    }.launchIn(this)
            } catch (e: Throwable) {
                Log.e("DicyVPN/Application", Log.getStackTraceString(e))
            }
        }
    }

    override fun onTerminate() {
        coroutineScope.cancel()
        super.onTerminate()
    }

    fun getUserAgent(): String {
        return userAgent
    }

    companion object {
        private const val TAG = "DicyVPN/Application"
        private lateinit var weakSelf: WeakReference<DicyVPN>

        fun get(): DicyVPN {
            return weakSelf.get()!!
        }

        fun getPreferencesDataStore() = get().preferencesDataStore

        fun getStatus() = get().status

        fun getLastServer() = get().lastServer

        fun setTunnelUp(config: String) {
            val instance = get()
            instance.backend?.setState(instance.tunnel, Tunnel.State.UP, Config.parse(StringReader(config).buffered()))
        }

        fun setTunnelDown() {
            val instance = get()
            instance.backend?.setState(instance.tunnel, Tunnel.State.DOWN, null)
        }
    }

    init {
        weakSelf = WeakReference(this)
    }
}