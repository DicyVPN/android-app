package com.dicyvpn.android

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dicyvpn.android.ui.theme.Gray200
import com.dicyvpn.android.ui.theme.Gray400
import com.dicyvpn.android.ui.theme.Gray600
import com.dicyvpn.android.ui.theme.Shapes
import com.dicyvpn.android.ui.theme.Typography
import com.dicyvpn.android.ui.theme.components.Button
import com.dicyvpn.android.ui.theme.components.ButtonColor
import com.dicyvpn.android.ui.theme.components.ButtonSize
import com.dicyvpn.android.ui.theme.components.ButtonTheme

@Composable
fun Login(navController: NavHostController, modifier: Modifier = Modifier) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.world_map),
            contentDescription = stringResource(R.string.world_map),
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(Gray400),
            modifier = modifier
                .fillMaxSize()
                .scale(1.5f)
                .alpha(0.7f)
        )
        Row(modifier = modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = modifier.padding(48.dp), color = Gray600, contentColor = Color.White, shape = Shapes.large, shadowElevation = 8.dp) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = modifier.padding(24.dp, 16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.full_logo),
                        contentDescription = stringResource(R.string.dicyvpn_logo),
                        modifier = Modifier
                            .padding(16.dp, 12.dp)
                            .heightIn(max = 52.dp)
                    )
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(R.string.email)) },
                        shape = Shapes.medium,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                    )
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.password)) },
                        shape = Shapes.medium, // TODO: add password visibility toggle
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    Button(
                        onClick = {
                            navController.navigate("home")
                        },
                        theme = ButtonTheme.DARK,
                        color = ButtonColor.BLUE,
                        size = ButtonSize.BIG,
                        modifier = modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.login))
                    }
                    Column(modifier = modifier.fillMaxWidth()) {
                        Text("Create an account", color = Gray200, style = Typography.bodySmall) // TODO: should be underlined
                        Text("Recover your password", color = Gray200, style = Typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = false, showBackground = false)
@Composable
fun LoginPreview() {
    Login(navController = NavHostController(LocalContext.current))
}
