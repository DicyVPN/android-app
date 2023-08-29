package com.dicyvpn.android

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.wireguard.android.backend.GoBackend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.Locale

class DicyVPN : Application() {
    private val coroutineScope = CoroutineScope(Job() + Dispatchers.Main.immediate)
    private var backend: GoBackend? = null
    private val preferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, USER_AGENT)
        coroutineScope.launch(Dispatchers.IO) {
            try {
                backend = GoBackend(applicationContext)
            } catch (e: Throwable) {
                Log.e("DicyVPN/Application", Log.getStackTraceString(e))
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

        fun getBackend() = get().backend

        fun getCoroutineScope() = get().coroutineScope
    }

    init {
        weakSelf = WeakReference(this)
    }
}