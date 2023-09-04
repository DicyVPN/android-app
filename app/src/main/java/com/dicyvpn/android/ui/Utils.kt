package com.dicyvpn.android.ui

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.unit.IntSize
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.dicyvpn.android.DicyVPN
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun <T> rememberPreference(
    key: Preferences.Key<T>,
    defaultValue: T,
): MutableState<T> {
    val coroutineScope = rememberCoroutineScope()
    val state = remember {
        DicyVPN.getPreferencesDataStore().data
            .map {
                it[key] ?: defaultValue
            }
    }.collectAsState(initial = defaultValue)

    return remember {
        object : MutableState<T> {
            override var value: T
                get() = state.value
                set(value) {
                    coroutineScope.launch {
                        DicyVPN.getPreferencesDataStore().edit {
                            it[key] = value
                        }
                    }
                }

            override fun component1() = value
            override fun component2(): (T) -> Unit = { value = it }
        }
    }
}

private suspend fun ScrollableState.animateScrollBy(
    value: Float,
    scrollPriority: MutatePriority = MutatePriority.Default,
    animationSpec: AnimationSpec<Float> = spring()
): Float {
    var previousValue = 0f
    scroll(scrollPriority) {
        animate(0f, value, animationSpec = animationSpec) { currentValue, _ ->
            previousValue += scrollBy(currentValue - previousValue)
        }
    }
    return previousValue
}

suspend fun ScrollState.animateScrollTop(
    scrollPriority: MutatePriority = MutatePriority.Default,
    animationSpec: AnimationSpec<Float> = SpringSpec()
) {
    this.animateScrollBy((-this.value).toFloat(), scrollPriority, animationSpec)
}

// from https://github.com/thesauri/dpad-compose
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.dpadFocusable(
    onClick: () -> Unit,
    enabled: Boolean = true,
    indication: Indication? = null,
    scrollPadding: Rect = Rect.Zero,
    isDefault: Boolean = false
) = composed {
    if (!enabled) {
        return@composed this
    }

    val focusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val boxInteractionSource = remember { MutableInteractionSource() }
    val isItemFocused by boxInteractionSource.collectIsFocusedAsState()
    var previousFocus: FocusInteraction.Focus? by remember {
        mutableStateOf(null)
    }
    var previousPress: PressInteraction.Press? by remember {
        mutableStateOf(null)
    }
    val scope = rememberCoroutineScope()
    var boxSize by remember {
        mutableStateOf(IntSize(0, 0))
    }
    val inputMode = LocalInputModeManager.current

    LaunchedEffect(inputMode.inputMode) {
        when (inputMode.inputMode) {
            InputMode.Keyboard -> {
                if (isDefault) {
                    focusRequester.requestFocus()
                }
            }

            InputMode.Touch -> {}
        }
    }
    LaunchedEffect(isItemFocused) {
        previousPress?.let {
            if (!isItemFocused) {
                boxInteractionSource.emit(
                    PressInteraction.Release(
                        press = it
                    )
                )
            }
        }
    }

    if (inputMode.inputMode == InputMode.Touch)
        this.clickable(
            interactionSource = boxInteractionSource,
            indication = indication ?: rememberRipple()
        ) {
            onClick()
        }
    else
        this
            .bringIntoViewRequester(bringIntoViewRequester)
            .onSizeChanged {
                boxSize = it
            }
            .indication(
                interactionSource = boxInteractionSource,
                indication = indication ?: rememberRipple()
            )
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    val newFocusInteraction = FocusInteraction.Focus()
                    scope.launch {
                        boxInteractionSource.emit(newFocusInteraction)
                    }
                    scope.launch {
                        val visibilityBounds = Rect(
                            left = -1f * scrollPadding.left,
                            top = -1f * scrollPadding.top,
                            right = boxSize.width + scrollPadding.right,
                            bottom = boxSize.height + scrollPadding.bottom
                        )
                        bringIntoViewRequester.bringIntoView(visibilityBounds)
                    }
                    previousFocus = newFocusInteraction
                } else {
                    previousFocus?.let {
                        scope.launch {
                            boxInteractionSource.emit(FocusInteraction.Unfocus(it))
                        }
                    }
                }
            }
            .onKeyEvent {
                if (!listOf(Key.DirectionCenter, Key.Enter).contains(it.key)) {
                    return@onKeyEvent false
                }
                when (it.type) {
                    KeyEventType.KeyDown -> {
                        val press =
                            PressInteraction.Press(
                                pressPosition = Offset(
                                    x = boxSize.width / 2f,
                                    y = boxSize.height / 2f
                                )
                            )
                        scope.launch {
                            boxInteractionSource.emit(press)
                        }
                        previousPress = press
                        true
                    }

                    KeyEventType.KeyUp -> {
                        previousPress?.let { previousPress ->
                            onClick()
                            scope.launch {
                                boxInteractionSource.emit(
                                    PressInteraction.Release(
                                        press = previousPress
                                    )
                                )
                            }
                        }
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
            .focusRequester(focusRequester)
            .focusTarget()
}
