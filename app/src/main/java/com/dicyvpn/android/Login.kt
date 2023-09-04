package com.dicyvpn.android

import android.content.Context
import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dicyvpn.android.api.API
import com.dicyvpn.android.api.PublicAPI
import com.dicyvpn.android.ui.components.Button
import com.dicyvpn.android.ui.components.ButtonColor
import com.dicyvpn.android.ui.components.ButtonSize
import com.dicyvpn.android.ui.components.ButtonTheme
import com.dicyvpn.android.ui.theme.DicyVPNTheme
import com.dicyvpn.android.ui.theme.Gray200
import com.dicyvpn.android.ui.theme.Gray400
import com.dicyvpn.android.ui.theme.Gray600
import com.dicyvpn.android.ui.theme.Shapes
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun Login(navController: NavHostController, modifier: Modifier = Modifier) {
    var loading by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var openDialog by rememberSaveable { mutableStateOf(false) }
    var dialogMessage by rememberSaveable { mutableStateOf("") }
    var dialogLink by rememberSaveable { mutableStateOf("") }
    var dialogLinkText by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current
    val loginAction = {
        loading = true
        login(context, email, password, navController) { message, link, linkText ->
            loading = false
            dialogMessage = message
            dialogLink = link ?: ""
            dialogLinkText = linkText ?: ""
            openDialog = true
        }
    }

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
        Row(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = modifier
                    .padding(48.dp)
                    .imePadding()
                    .widthIn(max = 480.dp),
                color = Gray600,
                contentColor = Color.White,
                shape = Shapes.large,
                shadowElevation = 8.dp
            ) {
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
                        singleLine = true,
                        shape = Shapes.medium,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        isError = email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()
                    )
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.password)) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = Shapes.medium, // TODO: add password visibility toggle
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        keyboardActions = KeyboardActions(
                            onDone = { loginAction() }
                        ),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                    if (passwordVisible) stringResource(R.string.hide_password) else stringResource(R.string.show_password)
                                )
                            }
                        }
                    )
                    Button(
                        onClick = loginAction,
                        theme = ButtonTheme.DARK,
                        color = ButtonColor.BLUE,
                        size = ButtonSize.BIG,
                        modifier = modifier.defaultMinSize(
                            minWidth = TextFieldDefaults.MinWidth
                        ),
                        enabled = !loading
                    ) {
                        Text(stringResource(R.string.login))
                    }
                    Column(
                        modifier = modifier
                            .defaultMinSize(
                                minWidth = TextFieldDefaults.MinWidth
                            )
                            .padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // TODO: make clickable
                        Text(stringResource(R.string.create_an_account), color = Gray200, style = TextStyle(textDecoration = TextDecoration.Underline))
                        // TODO: make clickable
                        Text(stringResource(R.string.recover_your_password), color = Gray200, style = TextStyle(textDecoration = TextDecoration.Underline))
                    }
                }
            }
        }
    }

    if (openDialog) {
        val uriHandler = LocalUriHandler.current
        AlertDialog(
            onDismissRequest = {
                openDialog = false
            },
            text = {
                Text(dialogMessage)
            },
            confirmButton = {
                TextButton({
                    if (dialogLink.isNotBlank()) {
                        uriHandler.openUri(dialogLink)
                    } else {
                        openDialog = false
                    }
                }) {
                    Text(dialogLinkText.ifBlank { stringResource(R.string.dialog_close) })
                }
            },
            dismissButton = if (dialogLink.isNotBlank()) {
                {
                    TextButton({
                        openDialog = false
                    }) {
                        Text(stringResource(R.string.dialog_close))
                    }
                }
            } else null,
        )
    }
}

fun login(context: Context, email: String, password: String, navController: NavHostController, onError: (message: String, link: String?, linkText: String?) -> Unit) {
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        onError("Invalid email address", null, null)
        return
    }

    if (password.length < 8) {
        onError("Password must be at least 8 characters long", null, null)
        return
    }

    PublicAPI.get().login(PublicAPI.LoginRequest(email, password)).enqueue(object : Callback<Unit> {
        override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
            if (response.isSuccessful) {
                try {
                    API.setAuthInfo(response.headers())

                    navController.navigate("home") {
                        popUpTo(0)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    onError("Error while parsing the response from the server, please try again later\n\n${e.message}", null, null)
                }
            } else {
                if (response.code() == 400 || response.code() == 401) {
                    onError("Invalid email or password", null, null)
                    return
                }

                val error = response.errorBody()?.string()
                try {
                    val json = JSONObject(error!!)
                    val reply = json.getJSONObject("reply")
                    when (reply.getString("code")) {
                        "NO_SUBSCRIPTION" -> onError(
                            context.getString(R.string.no_active_subscription), "https://dicyvpn.com/prices", context.getString(R.string.take_a_look_at_our_plans)
                        )

                        "DEVICES_LIMIT_REACHED" -> onError(
                            context.getString(R.string.reached_the_maximum_number_of_devices), "https://dicyvpn.com/account", context.getString(R.string.check_your_devices_list)
                        )

                        else -> onError(reply.getString("message"), null, null)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    onError("Unknown error, please try again later\n\n$error", null, null)
                }
            }
        }

        override fun onFailure(call: Call<Unit>, t: Throwable) {
            t.printStackTrace()
            onError("Unknown network error, please try again later\n\n${t.message}", null, null)
        }
    })
}

@Preview
@Preview(name = "TV", device = "id:tv_1080p")
@Composable
fun LoginPreview() {
    DicyVPNTheme {
        Login(navController = NavHostController(LocalContext.current))
    }
}
