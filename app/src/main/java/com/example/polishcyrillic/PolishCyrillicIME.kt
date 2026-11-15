package com.example.polishcyrillic

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.view.KeyEvent
import android.view.View

class PolishCyrillicIME : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardView: KeyboardView
    private lateinit var latinKeyboard: Keyboard
    private lateinit var cyrillicKeyboard: Keyboard
    private var isCyrillic = false
    private var isCaps = false

    override fun onCreateInputView(): View {
        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as KeyboardView
        latinKeyboard = Keyboard(this, R.xml.keyboard_latin)
        cyrillicKeyboard = Keyboard(this, R.xml.keyboard_cyrillic)

        keyboardView.keyboard = latinKeyboard
        keyboardView.setOnKeyboardActionListener(this)
        return keyboardView
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val ic = currentInputConnection ?: return

        when (primaryCode) {
            -5 -> { // Backspace
                val selected = ic.getSelectedText(0)
                if (selected.isNullOrEmpty()) {
                    ic.deleteSurroundingText(1, 0)
                } else {
                    ic.commitText("", 1)
                }
            }
            -1 -> { // Shift
                isCaps = !isCaps
                val kb = if (isCyrillic) cyrillicKeyboard else latinKeyboard
                kb.isShifted = isCaps
                keyboardView.invalidateAllKeys()
            }
            -101 -> { // Switch layout
                isCyrillic = !isCyrillic
                val kb = if (isCyrillic) cyrillicKeyboard else latinKeyboard
                kb.isShifted = isCaps
                keyboardView.keyboard = kb
                keyboardView.invalidateAllKeys()
            }
            10 -> {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
            else -> {
                var ch = primaryCode.toChar()
                if (isCaps && ch.isLowerCase()) ch = ch.uppercaseChar()
                ic.commitText(ch.toString(), 1)
            }
        }
    }

    // Multi-letter keys (ja, иа, сь, зь, кс) arrive here via keyOutputText.
    // With Shift on, only the first letter is capitalized (Ja, Иа, Szcz).
    override fun onText(text: CharSequence?) {
        if (text == null) return
        val s = text.toString()
        val out = if (isCaps) s.replaceFirstChar { it.uppercaseChar() } else s
        currentInputConnection?.commitText(out, 1)
    }

    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
}
