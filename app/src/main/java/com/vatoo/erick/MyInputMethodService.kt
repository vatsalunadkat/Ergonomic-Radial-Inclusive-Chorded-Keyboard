package com.vatoo.erick

import android.inputmethodservice.InputMethodService
import android.view.View
import android.widget.Button

class MyInputMethodService : InputMethodService() {

    override fun onCreateInputView(): View {
        val view = layoutInflater.inflate(R.layout.keyboard_simple, null)

        val button1 = view.findViewById<Button>(R.id.button1)
        val button2 = view.findViewById<Button>(R.id.button2)

        button1?.setOnClickListener {
            currentInputConnection?.commitText("1", 1)
        }

        button2?.setOnClickListener {
            currentInputConnection?.commitText("2", 1)
        }

        return view
    }

    override fun onEvaluateInputViewShown(): Boolean {
        // 确保键盘在需要时始终能够显示
        return true
    }
}
