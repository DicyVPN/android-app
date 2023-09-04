package com.dicyvpn.android.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dicyvpn.android.ui.dpadFocusable
import com.dicyvpn.android.ui.theme.Blue100
import com.dicyvpn.android.ui.theme.Blue500
import com.dicyvpn.android.ui.theme.Blue600
import com.dicyvpn.android.ui.theme.Green100
import com.dicyvpn.android.ui.theme.Green500
import com.dicyvpn.android.ui.theme.Green600
import com.dicyvpn.android.ui.theme.Red100
import com.dicyvpn.android.ui.theme.Red500
import com.dicyvpn.android.ui.theme.Red600
import com.dicyvpn.android.ui.theme.Shapes

enum class ButtonTheme {
    DARK,
    LIGHT
}

enum class ButtonColor(val darkColor: Color, val lightColor: Color, val darkTextColor: Color, val lightTextColor: Color) {
    BLUE(Blue500, Blue100, Color.White, Blue600),
    RED(Red500, Red100, Color.White, Red600),
    GREEN(Green500, Green100, Color.White, Green600),
    TRANSPARENT(Color.Transparent, Color.Transparent, Color.White, Color.Black)
}

enum class ButtonSize {
    NORMAL,
    BIG
}

@Composable
fun Button(
    onClick: () -> Unit,
    theme: ButtonTheme,
    color: ButtonColor,
    size: ButtonSize,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onFocused: () -> Unit = {},
    focus: Boolean = false,
    scrollPadding: Rect = Rect.Zero,
    content: @Composable () -> Unit
) {
    var bgColor = if (theme == ButtonTheme.DARK) color.darkColor else color.lightColor
    var contentColor = if (theme == ButtonTheme.DARK) color.darkTextColor else color.lightTextColor
    if (!enabled) {
        bgColor = bgColor.copy(alpha = 0.5f)
        contentColor = contentColor.copy(alpha = 0.5f)
    }

    val horizontalPadding = if (size == ButtonSize.NORMAL) 24.dp else 32.dp
    val verticalPadding = if (size == ButtonSize.NORMAL) 8.dp else 12.dp

    val shadowElevation by animateDpAsState(
        if (color == ButtonColor.TRANSPARENT || !enabled) {
            0.dp
        } else {
            8.dp
        }, label = "shadowElevation"
    )

    Surface(
        modifier = modifier
            .dpadFocusable(
                onClick = onClick,
                isDefault = focus,
                enabled = enabled,
                scrollPadding = scrollPadding
            )
            .onFocusChanged {
                if (it.isFocused) {
                    onFocused()
                }
            }
            .focusable(enabled),
        shape = Shapes.medium,
        color = bgColor,
        contentColor = contentColor,
        tonalElevation = shadowElevation,
        shadowElevation = if (color == ButtonColor.TRANSPARENT) 0.dp else 8.dp,
    ) {
        val density = LocalDensity.current

        CompositionLocalProvider(LocalContentColor provides contentColor) {
            ProvideTextStyle(value = if (size == ButtonSize.NORMAL) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleSmall) {
                Box(
                    modifier
                        .drawWithCache {
                            onDrawWithContent {
                                if (enabled && color != ButtonColor.TRANSPARENT) {
                                    // draw a top inner shadow with the same shape as the button
                                    val innerHeight = 2.dp.toPx()
                                    drawRoundRect(
                                        color = Color.White,
                                        size = drawContext.size,
                                        cornerRadius = CornerRadius(Shapes.medium.topStart.toPx(drawContext.size, density), Shapes.medium.topEnd.toPx(drawContext.size, density)),
                                        alpha = 0.25f
                                    )
                                    drawRoundRect(
                                        color = bgColor,
                                        size = drawContext.size.copy(height = drawContext.size.height - innerHeight),
                                        cornerRadius = CornerRadius(Shapes.medium.topStart.toPx(drawContext.size, density), Shapes.medium.topEnd.toPx(drawContext.size, density)),
                                        topLeft = Offset(0f, innerHeight),
                                    )
                                }
                                drawContent()
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier
                            .defaultMinSize(
                                minWidth = ButtonDefaults.MinWidth,
                                minHeight = ButtonDefaults.MinHeight
                            )
                            .padding(
                                start = horizontalPadding,
                                end = horizontalPadding,
                                top = verticalPadding,
                                bottom = verticalPadding
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ButtonPreview() {
    Column() {
        Button(onClick = { }, theme = ButtonTheme.DARK, color = ButtonColor.BLUE, size = ButtonSize.NORMAL) {
            Text(text = "Click me")
        }
        Button(onClick = { }, theme = ButtonTheme.DARK, color = ButtonColor.RED, size = ButtonSize.NORMAL) {
            Text(text = "Click me")
        }
        Button(onClick = { }, theme = ButtonTheme.DARK, color = ButtonColor.GREEN, size = ButtonSize.NORMAL) {
            Text(text = "Click me")
        }
        Button(onClick = { }, theme = ButtonTheme.LIGHT, color = ButtonColor.BLUE, size = ButtonSize.NORMAL) {
            Text(text = "Click me")
        }
        Button(onClick = { }, theme = ButtonTheme.LIGHT, color = ButtonColor.RED, size = ButtonSize.NORMAL) {
            Text(text = "Click me")
        }
        Button(onClick = { }, theme = ButtonTheme.LIGHT, color = ButtonColor.GREEN, size = ButtonSize.NORMAL) {
            Text(text = "Click me")
        }
        Button(onClick = { }, theme = ButtonTheme.LIGHT, color = ButtonColor.TRANSPARENT, size = ButtonSize.NORMAL) {
            Text(text = "Click me")
        }
        Button(onClick = { }, theme = ButtonTheme.DARK, color = ButtonColor.BLUE, size = ButtonSize.BIG) {
            Text(text = "Click me")
        }
        Button(onClick = { }, theme = ButtonTheme.DARK, color = ButtonColor.RED, size = ButtonSize.BIG) {
            Text(text = "Click me")
        }
        Button(onClick = { }, theme = ButtonTheme.DARK, color = ButtonColor.GREEN, size = ButtonSize.BIG) {
            Text(text = "Click me")
        }
        Button(onClick = { }, theme = ButtonTheme.LIGHT, color = ButtonColor.BLUE, size = ButtonSize.BIG) {
            Text(text = "Click me")
        }
        Button(onClick = { }, theme = ButtonTheme.LIGHT, color = ButtonColor.RED, size = ButtonSize.BIG) {
            Text(text = "Click me")
        }
        Button(onClick = { }, theme = ButtonTheme.LIGHT, color = ButtonColor.GREEN, size = ButtonSize.BIG) {
            Text(text = "Click me")
        }
        Button(onClick = { }, theme = ButtonTheme.LIGHT, color = ButtonColor.TRANSPARENT, size = ButtonSize.BIG) {
            Text(text = "Click me")
        }
    }
}
