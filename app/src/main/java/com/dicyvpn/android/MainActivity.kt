package com.dicyvpn.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dicyvpn.android.ui.theme.DicyVPNTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            DicyVPNTheme {
                val navController = rememberNavController()

                LaunchedEffect(Unit) {
                    DicyVPN.getPreferencesDataStore().data.collect {
                        Log.i("DicyVPN/MainActivity", "data.collect - " + it[stringPreferencesKey("auth.refreshToken")])
                        if (it[stringPreferencesKey("auth.refreshToken")].isNullOrEmpty()) {
                            navController.navigate("login") {
                                popUpTo(0)
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = "startup") {
                        composable("startup") { Startup(navController) }
                        composable("login") { Login(navController) }
                        composable("home") { Home() }
                        // TODO: add logout
                    }
                }
            }
        }
    }

    @Composable
    fun Startup(navController: NavHostController) {
        val refreshToken = runBlocking {
            DicyVPN.getPreferencesDataStore().data.map {
                it[stringPreferencesKey("auth.refreshToken")] ?: ""
            }.first()
        }

        if (refreshToken.isNotEmpty()) {
            navController.navigate("home") {
                popUpTo(0)
            }
        } else {
            navController.navigate("login") {
                popUpTo(0)
            }
        }
    }
}
