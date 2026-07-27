package com.example.modifier

import android.view.KeyEvent
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type

/**
 * Custom modifier intercepting D-pad / Volume / Keyboard navigation keypresses
 * allowing two-wheeler handlebar or vehicle steering wheel control switches
 * to cycle through UI cards and trigger actions without screen touches.
 */
fun Modifier.interceptHardwareKeyEvents(
    onPreviousCard: () -> Unit,
    onNextCard: () -> Unit,
    onSelectCard: () -> Unit
): Modifier = this.onKeyEvent { event ->
    if (event.type == KeyEventType.KeyDown) {
        when (event.nativeKeyEvent.keyCode) {
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_DPAD_LEFT -> {
                onPreviousCard()
                true
            }
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT -> {
                onNextCard()
                true
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_SPACE -> {
                onSelectCard()
                true
            }
            else -> false
        }
    } else {
        false
    }
}

