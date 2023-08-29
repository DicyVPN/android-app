package com.dicyvpn.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dicyvpn.android.ui.theme.BrightGreen
import com.dicyvpn.android.ui.theme.DicyVPNTheme
import com.dicyvpn.android.ui.theme.Gray600
import com.dicyvpn.android.ui.theme.Gray800
import com.dicyvpn.android.ui.theme.Shapes
import com.dicyvpn.android.ui.theme.Typography
import com.dicyvpn.android.ui.theme.components.Button
import com.dicyvpn.android.ui.theme.components.ButtonColor
import com.dicyvpn.android.ui.theme.components.ButtonSize
import com.dicyvpn.android.ui.theme.components.ButtonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DicyVPNTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Home()
                }
            }
        }
    }
}

@Composable
fun Home(modifier: Modifier = Modifier) {
    Column(
        modifier
            .padding(top = 8.dp)
            .fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp), color = Gray600, contentColor = Color.White, shape = Shapes.medium, shadowElevation = 8.dp
        ) {
            Image(
                painter = painterResource(id = R.drawable.full_logo),
                contentDescription = stringResource(R.string.dicyvpn_logo),
                modifier = Modifier
                    .padding(16.dp, 12.dp)
                    .heightIn(max = 40.dp)
            )
        }
        Surface(
            modifier
                .fillMaxWidth()
                .weight(1f), color = Gray800
        ) {
            Image(
                painter = painterResource(id = R.drawable.world_map),
                contentDescription = stringResource(R.string.world_map),
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .fillMaxWidth()
                    .scale(1.5f)
            )
        }
        Surface(modifier.fillMaxWidth(), color = Gray800) {
            Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(style = Typography.bodyMedium, text = "Connesso")
                Surface(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(1.dp), color = BrightGreen
                ) {}
                Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Germania")
                    Spacer(modifier = modifier.weight(1f))
                    Text(fontFamily = FontFamily.Monospace, text = "DE_01")
                    Image(painterResource(id = R.drawable.flag_de), modifier = modifier.width(24.dp).clip(Shapes.small), contentDescription = null)
                }
                Button({}, ButtonTheme.DARK, ButtonColor.RED, ButtonSize.NORMAL, modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )
                        Text("Disconnetti")
                    }
                }
            }
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