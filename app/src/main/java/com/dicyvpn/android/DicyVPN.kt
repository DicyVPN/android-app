package com.dicyvpn.android

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.dicyvpn.android.vpn.Status
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.Locale

class DicyVPN : Application() {
    private val coroutineScope = CoroutineScope(Job() + Dispatchers.Main.immediate)
    private var status: MutableState<Status> = mutableStateOf(Status.CONNECTING)
    private var backend: GoBackend? = null
    private val tunnel: Tunnel? = null
    private lateinit var preferencesDataStore: DataStore<Preferences>

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, USER_AGENT)
        preferencesDataStore = PreferenceDataStoreFactory.create {
            applicationContext.preferencesDataStoreFile("settings")
        }
        coroutineScope.launch(Dispatchers.IO) {
            try {
                backend = GoBackend(applicationContext)
            } catch (e: Throwable) {
                Log.e("DicyVPN/Application", Log.getStackTraceString(e))
            }
        }
        coroutineScope.launch(Dispatchers.IO) {
            while (true) {
                status.value = when (status.value) {
                    Status.CONNECTING -> Status.CONNECTED
                    Status.CONNECTED -> Status.DISCONNECTING
                    Status.DISCONNECTING -> Status.NOT_RUNNING
                    Status.NOT_RUNNING -> Status.CONNECTING
                }
                Thread.sleep(2000)
            }
        }
    }

    override fun onTerminate() {
        coroutineScope.cancel()
        super.onTerminate()
    }

    companion object {
        val USER_AGENT = String.format(
            Locale.ENGLISH,
            "DicyVPN Android v%s (SDK %d; %s; %s; %s %s)",
            BuildConfig.VERSION_NAME,
            Build.VERSION.SDK_INT,
            if (Build.SUPPORTED_ABIS.isNotEmpty()) Build.SUPPORTED_ABIS[0] else "Unknown ABI",
            Build.BOARD,
            Build.MANUFACTURER,
            Build.MODEL
        )
        private const val TAG = "DicyVPN/Application"
        private lateinit var weakSelf: WeakReference<DicyVPN>

        fun get(): DicyVPN {
            return weakSelf.get()!!
        }

        fun getPreferencesDataStore() = get().preferencesDataStore

        fun getStatus() = get().status

        fun getBackend() = get().backend

        fun getTunnel() = get().tunnel
    }

    init {
        weakSelf = WeakReference(this)
    }
}