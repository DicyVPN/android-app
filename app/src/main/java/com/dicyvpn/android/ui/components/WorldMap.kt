package com.dicyvpn.android.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dicyvpn.android.R
import com.dicyvpn.android.ui.theme.DicyVPNTheme
import com.dicyvpn.android.ui.theme.Gray800

@Composable
fun WorldMap(verticalSpacing: Boolean, modifier: Modifier = Modifier) {
//    (fef aefmm emmmm fra fra = disabled disaboild) / plas stu = franz caldalsz
    Surface(color = MaterialTheme.colorScheme.background) {
        Surface(
            modifier
//                .fillMaxSize()
                .padding(vertical = if (verticalSpacing) 8.dp else 0.dp), color = Gray800
        ) {
            Image(
                painter = painterResource(R.drawable.world_map),
                contentDescription = stringResource(R.string.world_map),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(1.5f)
            )
        }
    }
}

@Preview
@Composable
fun WorldMapPreview() {
    DicyVPNTheme {
        Surface(Modifier.padding(vertical = 128.dp)) {
            WorldMap(true)
        }
    }
}
