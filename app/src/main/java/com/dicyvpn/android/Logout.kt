package com.dicyvpn.android

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dicyvpn.android.ui.components.Button
import com.dicyvpn.android.ui.components.ButtonColor
import com.dicyvpn.android.ui.components.ButtonSize
import com.dicyvpn.android.ui.components.ButtonTheme
import com.dicyvpn.android.ui.theme.DicyVPNTheme
import com.dicyvpn.android.ui.theme.Gray800
import com.dicyvpn.android.ui.theme.Typography

@Composable
fun Logout(navController: NavHostController, modifier: Modifier = Modifier) {
    Surface(modifier, color = Gray800, contentColor = Color.White) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically), horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
            Text(text = "Sign out of DicyVPN", style = Typography.titleLarge)
            Text(text = "Are you sure you want to sign out of DicyVPN?")
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp)) {
                Button({ navController.popBackStack() }, ButtonTheme.DARK, ButtonColor.TRANSPARENT, ButtonSize.NORMAL) {
                    Text(text = "Go back")
                }
                Button({ /* TODO: handle */}, ButtonTheme.DARK, ButtonColor.BLUE, ButtonSize.NORMAL) {
                    Text(text = "Sign out")
                }
            }
        }
    }
}

@Preview
@Preview(name = "TV", device = "id:tv_1080p")
@Composable
fun LogoutPreview() {
    DicyVPNTheme {
        Logout(navController = NavHostController(LocalContext.current), Modifier.fillMaxSize())
    }
}
