package com.dessalines.thumbkey.ui.components.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.emoji2.emojipicker.EmojiPickerView
import com.dessalines.thumbkey.IMEService
import com.dessalines.thumbkey.db.AppSettings
import com.dessalines.thumbkey.db.DEFAULT_ANIMATION_HELPER_SPEED
import com.dessalines.thumbkey.db.DEFAULT_ANIMATION_SPEED
import com.dessalines.thumbkey.db.DEFAULT_AUTO_CAPITALIZE
import com.dessalines.thumbkey.db.DEFAULT_HIDE_LETTERS
import com.dessalines.thumbkey.db.DEFAULT_HIDE_SYMBOLS
import com.dessalines.thumbkey.db.DEFAULT_KEYBOARD_LAYOUT
import com.dessalines.thumbkey.db.DEFAULT_KEY_BORDERS
import com.dessalines.thumbkey.db.DEFAULT_KEY_SIZE
import com.dessalines.thumbkey.db.DEFAULT_MIN_SWIPE_LENGTH
import com.dessalines.thumbkey.db.DEFAULT_POSITION
import com.dessalines.thumbkey.db.DEFAULT_PUSHUP_SIZE
import com.dessalines.thumbkey.db.DEFAULT_SLIDE_ENABLED
import com.dessalines.thumbkey.db.DEFAULT_SLIDE_SENSITIVITY
import com.dessalines.thumbkey.db.DEFAULT_SOUND_ON_TAP
import com.dessalines.thumbkey.db.DEFAULT_SPACEBAR_MULTITAPS
import com.dessalines.thumbkey.db.DEFAULT_VIBRATE_ON_TAP
import com.dessalines.thumbkey.keyboards.BACKSPACE_KEY_ITEM
import com.dessalines.thumbkey.keyboards.EMOJI_BACK_KEY_ITEM
import com.dessalines.thumbkey.keyboards.NUMERIC_KEY_ITEM
import com.dessalines.thumbkey.keyboards.RETURN_KEY_ITEM
import com.dessalines.thumbkey.keyboards.THUMBKEY_EN_V4_MAIN
import com.dessalines.thumbkey.utils.KeyAction
import com.dessalines.thumbkey.utils.KeyboardLayout
import com.dessalines.thumbkey.utils.KeyboardMode
import com.dessalines.thumbkey.utils.KeyboardPosition
import com.dessalines.thumbkey.utils.getKeyboardMode
import com.dessalines.thumbkey.utils.keyboardLayoutToModes
import com.dessalines.thumbkey.utils.keyboardPositionToAlignment
import com.dessalines.thumbkey.utils.toBool

@Composable
fun KeyboardScreen(
    settings: AppSettings?,
    onSwitchLanguage: () -> Unit,
    onSwitchPosition: () -> Unit,
) {
    val ctx = LocalContext.current as IMEService

    var mode by remember {
        val startMode = getKeyboardMode(
            ime = ctx,
            autoCapitalize = settings?.autoCapitalize?.toBool() ?: false,
        )

        mutableStateOf(startMode)
    }

    var capsLock by remember {
        mutableStateOf(false)
    }

    // TODO get rid of this crap
    val lastAction = remember { mutableStateOf<KeyAction?>(null) }

    val keyboardGroup = keyboardLayoutToModes(
        KeyboardLayout.values().sortedBy { it.index }[
            settings?.keyboardLayout
                ?: DEFAULT_KEYBOARD_LAYOUT,
        ],
    )

    val keyboard = keyboardGroup[mode] ?: THUMBKEY_EN_V4_MAIN

    val alignment = keyboardPositionToAlignment(
        KeyboardPosition.values()[
            settings?.position
                ?: DEFAULT_POSITION,
        ],
    )
    val pushupSizeDp = (settings?.pushupSize ?: DEFAULT_PUSHUP_SIZE).dp

    val autoCapitalize = (settings?.autoCapitalize ?: DEFAULT_AUTO_CAPITALIZE).toBool()
    val spacebarMultiTaps = (settings?.spacebarMultiTaps ?: DEFAULT_SPACEBAR_MULTITAPS).toBool()
    val slideEnabled = (settings?.slideEnabled ?: DEFAULT_SLIDE_ENABLED).toBool()
    val keyBorders = (settings?.keyBorders ?: DEFAULT_KEY_BORDERS).toBool()
    val vibrateOnTap = (settings?.vibrateOnTap ?: DEFAULT_VIBRATE_ON_TAP).toBool()
    val soundOnTap = (settings?.soundOnTap ?: DEFAULT_SOUND_ON_TAP).toBool()
    val hideLetters = (settings?.hideLetters ?: DEFAULT_HIDE_LETTERS).toBool()
    val hideSymbols = (settings?.hideSymbols ?: DEFAULT_HIDE_SYMBOLS).toBool()

    if (mode == KeyboardMode.EMOJI) {
        val controllerKeys = listOf(EMOJI_BACK_KEY_ITEM, NUMERIC_KEY_ITEM, BACKSPACE_KEY_ITEM, RETURN_KEY_ITEM)

        val keySize = settings?.keySize ?: DEFAULT_KEY_SIZE
        val keyboardHeight = Dp((keySize * controllerKeys.size).toFloat()) + pushupSizeDp

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier.weight(1f), // Take up available space equally
            ) {
                AndroidView(
                    // Write the emoji to our text box when we tap one.
                    factory = { context ->
                        val emojiPicker = EmojiPickerView(context)
                        emojiPicker.setOnEmojiPickedListener {
                            ctx.currentInputConnection.commitText(
                                it.emoji,
                                1,
                            )
                        }

                        emojiPicker
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(keyboardHeight)
                        .background(MaterialTheme.colorScheme.background),
                )
            }

            Column() {
                controllerKeys.forEach { key ->
                    Column {
                        KeyboardKey(
                            key = key,
                            lastAction = lastAction,
                            keySize = settings?.keySize ?: DEFAULT_KEY_SIZE,
                            autoCapitalize = autoCapitalize,
                            spacebarMultiTaps = spacebarMultiTaps,
                            keyBorders = keyBorders,
                            vibrateOnTap = vibrateOnTap,
                            soundOnTap = soundOnTap,
                            hideLetters = hideLetters,
                            hideSymbols = hideSymbols,
                            capsLock = capsLock,
                            animationSpeed = settings?.animationSpeed
                                ?: DEFAULT_ANIMATION_SPEED,
                            animationHelperSpeed = settings?.animationHelperSpeed
                                ?: DEFAULT_ANIMATION_HELPER_SPEED,
                            minSwipeLength = settings?.minSwipeLength ?: DEFAULT_MIN_SWIPE_LENGTH,
                            slideSensitivity = settings?.slideSensitivity ?: DEFAULT_SLIDE_SENSITIVITY,
                            slideEnabled = slideEnabled,
                            onToggleShiftMode = { enable ->
                                mode = if (enable) {
                                    KeyboardMode.SHIFTED
                                } else {
                                    capsLock = false
                                    KeyboardMode.MAIN
                                }
                            },
                            onToggleNumericMode = { enable ->
                                mode = if (enable) {
                                    KeyboardMode.NUMERIC
                                } else {
                                    capsLock = false
                                    KeyboardMode.MAIN
                                }
                            },
                            onToggleEmojiMode = { enable ->
                                mode = if (enable) {
                                    KeyboardMode.EMOJI
                                } else {
                                    KeyboardMode.MAIN
                                }
                            },
                            onToggleCapsLock = {
                                capsLock = !capsLock
                            },
                            onAutoCapitalize = { enable ->
                                if (mode !== KeyboardMode.NUMERIC) {
                                    if (enable) {
                                        mode = KeyboardMode.SHIFTED
                                    } else if (!capsLock) {
                                        mode = KeyboardMode.MAIN
                                    }
                                }
                            },
                            onSwitchLanguage = onSwitchLanguage,
                            onSwitchPosition = onSwitchPosition,
                        )
                    }
                }
            }
        }
    } else {
        Box(
            contentAlignment = alignment,
            modifier = Modifier
                .padding(bottom = pushupSizeDp),
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.onBackground),
            ) {
                keyboard.arr.forEach { row ->
                    Row {
                        row.forEach { key ->
                            Column {
                                KeyboardKey(
                                    key = key,
                                    lastAction = lastAction,
                                    keySize = settings?.keySize ?: DEFAULT_KEY_SIZE,
                                    autoCapitalize = autoCapitalize,
                                    spacebarMultiTaps = spacebarMultiTaps,
                                    keyBorders = keyBorders,
                                    vibrateOnTap = vibrateOnTap,
                                    soundOnTap = soundOnTap,
                                    hideLetters = hideLetters,
                                    hideSymbols = hideSymbols,
                                    capsLock = capsLock,
                                    animationSpeed = settings?.animationSpeed
                                        ?: DEFAULT_ANIMATION_SPEED,
                                    animationHelperSpeed = settings?.animationHelperSpeed
                                        ?: DEFAULT_ANIMATION_HELPER_SPEED,
                                    minSwipeLength = settings?.minSwipeLength ?: DEFAULT_MIN_SWIPE_LENGTH,
                                    slideSensitivity = settings?.slideSensitivity ?: DEFAULT_SLIDE_SENSITIVITY,
                                    slideEnabled = slideEnabled,
                                    onToggleShiftMode = { enable ->
                                        mode = if (enable) {
                                            KeyboardMode.SHIFTED
                                        } else {
                                            capsLock = false
                                            KeyboardMode.MAIN
                                        }
                                    },
                                    onToggleNumericMode = { enable ->
                                        mode = if (enable) {
                                            KeyboardMode.NUMERIC
                                        } else {
                                            capsLock = false
                                            KeyboardMode.MAIN
                                        }
                                    },
                                    onToggleEmojiMode = { enable ->
                                        mode = if (enable) {
                                            KeyboardMode.EMOJI
                                        } else {
                                            KeyboardMode.MAIN
                                        }
                                    },
                                    onToggleCapsLock = {
                                        capsLock = !capsLock
                                    },
                                    onAutoCapitalize = { enable ->
                                        if (mode !== KeyboardMode.NUMERIC) {
                                            if (enable) {
                                                mode = KeyboardMode.SHIFTED
                                            } else if (!capsLock) {
                                                mode = KeyboardMode.MAIN
                                            }
                                        }
                                    },
                                    onSwitchLanguage = onSwitchLanguage,
                                    onSwitchPosition = onSwitchPosition,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
