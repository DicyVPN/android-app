package com.dicyvpn.android

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dicyvpn.android.api.API
import com.dicyvpn.android.ui.components.Button
import com.dicyvpn.android.ui.components.ButtonColor
import com.dicyvpn.android.ui.components.ButtonSize
import com.dicyvpn.android.ui.components.ButtonTheme
import com.dicyvpn.android.ui.theme.DicyVPNTheme
import com.dicyvpn.android.ui.theme.Gray800
import com.dicyvpn.android.ui.theme.Typography
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun Logout(navController: NavHostController, modifier: Modifier = Modifier) {
    var loading by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val logoutAction = {
        loading = true
        logout(context, navController) {
            loading = false
        }
    }

    Surface(modifier, color = Gray800, contentColor = Color.White) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically), horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
            Text(text = stringResource(R.string.logout_title), style = Typography.headlineSmall)
            Text(text = stringResource(R.string.logout__message))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp)) {
                Button({ navController.popBackStack() }, ButtonTheme.DARK, ButtonColor.TRANSPARENT, ButtonSize.NORMAL) {
                    Text(stringResource(R.string.logout_label_go_back))
                }
                Button(logoutAction, ButtonTheme.DARK, ButtonColor.BLUE, ButtonSize.NORMAL, enabled = !loading) {
                    Text(stringResource(R.string.logout_label_sign_out))
                }
            }
        }
    }
}

private fun logout(context: Context, navController: NavHostController, finally: () -> Unit) {
    API.get().logout().enqueue(object : Callback<Unit> {
        override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
            try {
                API.removeAuthInfo()

                navController.navigate("login") {
                    popUpTo(0)
                }

                if (!response.isSuccessful) {
                    Log.e("Logout", "Received error: ${response.errorBody()!!.string()}")
                    Toast.makeText(context, JSONObject(response.errorBody()!!.string()).getString("message"), Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                finally()
            }
        }

        override fun onFailure(call: Call<Unit>, t: Throwable) {
            t.printStackTrace()
            Toast.makeText(context, "Unknown network error, please try again later\n\n${t.message}", Toast.LENGTH_LONG).show()
            finally()
        }
    })
}

@Preview
@Preview(name = "TV", device = "id:tv_1080p")
@Composable
fun LogoutPreview() {
    DicyVPNTheme {
        Logout(navController = NavHostController(LocalContext.current), Modifier.fillMaxSize())
    }
}
