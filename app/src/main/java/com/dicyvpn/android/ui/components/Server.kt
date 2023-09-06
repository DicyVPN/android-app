package com.dicyvpn.android.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dicyvpn.android.api.API
import com.dicyvpn.android.ui.theme.Gray300
import com.dicyvpn.android.ui.theme.Gray900
import com.dicyvpn.android.ui.theme.LoadGreen
import com.dicyvpn.android.ui.theme.LoadOrange
import com.dicyvpn.android.ui.theme.LoadRed
import com.dicyvpn.android.ui.theme.LoadYellow
import com.dicyvpn.android.ui.theme.Shapes

@Composable
fun Server(server: API.ServerList.Server, modifier: Modifier = Modifier, surfaceModifier: Modifier = modifier) {
    Surface(surfaceModifier, color = Gray900, contentColor = Color.White, shape = Shapes.medium) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(server.city)
            Spacer(modifier = modifier.weight(1f))
            Surface(
                modifier.size(6.dp), shape = Shapes.extraSmall,
                color = when {
                    server.load > 0.85 -> LoadRed
                    server.load > 0.65 -> LoadOrange
                    server.load > 0.45 -> LoadYellow
                    else -> LoadGreen
                }
            ) {}
            Text(server.name, fontFamily = FontFamily.Monospace, color = Gray300)
            Flag(country = server.country)
        }
    }
}

@SuppressLint("DiscouragedApi")
@Composable
fun Flag(country: String, modifier: Modifier = Modifier, width: Dp = 24.dp) {
    val flagId = LocalContext.current.resources.getIdentifier(
        "flag_${country.lowercase()}",
        "drawable",
        LocalContext.current.packageName
    )
    Image(
        painterResource(id = flagId),
        modifier = modifier
            .width(width)
            .clip(Shapes.small),
        contentDescription = null
    )
}
