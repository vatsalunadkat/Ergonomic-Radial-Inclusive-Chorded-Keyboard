package com.vatoo.erick

import android.inputmethodservice.InputMethodService
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import com.vatoo.erick.shared.ColorPalettes
import com.vatoo.erick.shared.Direction
import com.vatoo.erick.shared.InputAction
import com.vatoo.erick.shared.KeyboardActionDelegate
import com.vatoo.erick.shared.KeyboardMode
import com.vatoo.erick.shared.KeyboardStateMachine
import com.vatoo.erick.shared.LayoutType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MyInputMethodService : InputMethodService(), KeyboardActionDelegate {

    private lateinit var leftJoystick: JoystickView
    private lateinit var rightJoystick: JoystickView
    private lateinit var previewContainer: FrameLayout
    private lateinit var previewText: TextView

    private lateinit var stateMachine: KeyboardStateMachine

    private lateinit var preferencesManager: PreferencesManager

    // Scope needed only for observing the DataStore preference flow
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferencesManager(this)
        stateMachine = KeyboardStateMachine(this)

        // Observe layout preference and push changes into the state machine
        serviceScope.launch {
            preferencesManager.layoutType.collect { layoutString ->
                val layoutType = when (layoutString) {
                    PreferencesManager.LAYOUT_EFFICIENCY -> LayoutType.EFFICIENCY
                    else                                 -> LayoutType.LOGICAL
                }
                stateMachine.setLayoutType(layoutType)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    override fun onCreateInputView(): View {
        val view = layoutInflater.inflate(R.layout.keyboard_simple, null)

        leftJoystick = view.findViewById(R.id.left_joystick)
        rightJoystick = view.findViewById(R.id.right_joystick)
        rightJoystick.isRightSide = true

        previewContainer = view.findViewById(R.id.live_preview_container)
        previewText = view.findViewById(R.id.live_preview_text)

        leftJoystick.setOnTouchListener { v, event ->
            v.performClick()
            dispatchTouchToStateMachine(event, isLeft = true, joystick = leftJoystick)
            true
        }

        rightJoystick.setOnTouchListener { v, event ->
            v.performClick()
            dispatchTouchToStateMachine(event, isLeft = false, joystick = rightJoystick)
            true
        }

        val settingsBtn = view.findViewById<ImageButton>(R.id.btn_settings)
        settingsBtn?.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }

        return view
    }

    private fun dispatchTouchToStateMachine(event: MotionEvent, isLeft: Boolean, joystick: JoystickView) {
        val dx = event.x - (joystick.width / 2f)
        val dy = event.y - (joystick.height / 2f)

        val actionMasked = event.actionMasked
        val isDownOrMove = actionMasked == MotionEvent.ACTION_DOWN || actionMasked == MotionEvent.ACTION_MOVE
        val isUpOrCancel = actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_CANCEL

        if (isDownOrMove) {
            joystick.updateThumb(dx, dy)
        } else if (isUpOrCancel) {
            joystick.resetThumb()
        }

        stateMachine.handleTouch(dx, dy, isLeft, isDownOrMove, isUpOrCancel)

        // 3. Update right joystick mode if needed
        rightJoystick.keyboardMode = stateMachine.currentMode

        // 3. 从状态机获取原先的预览逻辑（移除旧的摇杆文字预览）
        // rightJoystick.setPreviewText(stateMachine.getPreviewText())

        updateLivePreview()
    }

    private fun updateLivePreview() {
        val leftDir = leftJoystick.activeDirection
        val rightDir = rightJoystick.activeDirection

        if (leftDir == Direction.NONE) {
            previewContainer.visibility = View.INVISIBLE
            return
        }

        previewContainer.visibility = View.VISIBLE

        val chars = stateMachine.getCharactersForDirection(leftDir)
        if (chars.isEmpty()) {
            previewText.text = ""
            return
        }

        val builder = SpannableStringBuilder()

        // 8 possible right directions in clockwise order
        val allRightDirs = listOf(
            Direction.N, Direction.NE, Direction.E, Direction.SE,
            Direction.S, Direction.SW, Direction.W, Direction.NW
        )

        for (i in chars.indices) {
            val charStr = chars[i]
            if (charStr.isBlank()) continue

            val dirForChar = allRightDirs.getOrNull(i) ?: Direction.NONE

            val start = builder.length
            builder.append(charStr)
            builder.append("  ") // spacing

            val colorHex = ColorPalettes.getColorForDirectionHex(dirForChar)
            val color = Color.parseColor(colorHex)

            builder.setSpan(
                ForegroundColorSpan(color),
                start,
                start + charStr.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // Enlarge if this is the active right direction
            if (dirForChar == rightDir && rightDir != Direction.NONE) {
                builder.setSpan(
                    RelativeSizeSpan(1.5f),
                    start,
                    start + charStr.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                builder.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start,
                    start + charStr.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        previewText.text = builder
    }

    override fun commitText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    override fun sendInputAction(action: InputAction) {
        val keyCode = when (action) {
            InputAction.SPACE          -> KeyEvent.KEYCODE_SPACE
            InputAction.ENTER          -> KeyEvent.KEYCODE_ENTER
            InputAction.BACKSPACE      -> KeyEvent.KEYCODE_DEL
            InputAction.MOVE_HOME      -> KeyEvent.KEYCODE_MOVE_HOME
            InputAction.MOVE_END       -> KeyEvent.KEYCODE_MOVE_END
            InputAction.TOGGLE_SHIFT,
            InputAction.TOGGLE_CAPS    -> -1
            InputAction.DELETE_FORWARD -> KeyEvent.KEYCODE_FORWARD_DEL
            InputAction.DPAD_UP        -> KeyEvent.KEYCODE_DPAD_UP
            InputAction.DPAD_DOWN      -> KeyEvent.KEYCODE_DPAD_DOWN
            InputAction.DPAD_LEFT      -> KeyEvent.KEYCODE_DPAD_LEFT
            InputAction.DPAD_RIGHT     -> KeyEvent.KEYCODE_DPAD_RIGHT
            InputAction.PAGE_UP        -> KeyEvent.KEYCODE_PAGE_UP
            InputAction.PAGE_DOWN      -> KeyEvent.KEYCODE_PAGE_DOWN
            InputAction.TAB            -> KeyEvent.KEYCODE_TAB
        }

        if (keyCode != -1) {
            currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
        }
    }

    override fun onModeChanged(mode: KeyboardMode) {
        leftJoystick.keyboardMode = mode
        rightJoystick.keyboardMode = mode
    }
    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        attribute?.let { it.imeOptions = it.imeOptions or EditorInfo.IME_FLAG_NO_EXTRACT_UI }
        super.onStartInput(attribute, restarting)
    }
    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        info?.let { it.imeOptions = it.imeOptions or EditorInfo.IME_FLAG_NO_EXTRACT_UI }
        super.onStartInputView(info, restarting)
    }
    override fun onEvaluateFullscreenMode(): Boolean = false
    override fun onUpdateExtractingVisibility(ei: EditorInfo?) { setExtractViewShown(false) }
    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        return true
    }
}