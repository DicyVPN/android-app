package com.dicyvpn.android

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dicyvpn.android.ui.theme.DicyVPNTheme
import com.dicyvpn.android.ui.theme.rememberPreference

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            DicyVPNTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize().imePadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = "startup") {
                        composable("startup") { Startup(navController) }
                        composable("login") { Login(navController) }
                        composable("home") { Home() }
                    }
                }
            }
        }
    }
}

@Composable
fun Startup(navController: NavHostController) {
    val refreshToken by rememberPreference(stringPreferencesKey("auth.refreshToken"), "")
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

@Preview(showSystemUi = true)
@Preview(name = "TV", device = "id:tv_1080p")
@Composable
fun GreetingPreview() {
    DicyVPNTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Home()
        }
    }
}
