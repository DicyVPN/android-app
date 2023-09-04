package com.dicyvpn.android.ui

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
