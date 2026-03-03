package com.vatoo.erick

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.view.View
import android.widget.Button

class MyInputMethodService : InputMethodService() {

    override fun onCreateInputView(): View {
        val view = layoutInflater.inflate(R.layout.keyboard_simple, null)

        // Find buttons from XML
        val button1 = view.findViewById<Button>(R.id.button1)
        val button2 = view.findViewById<Button>(R.id.button2)

        // NEW: find settings button
        val settingsBtn = view.findViewById<Button>(R.id.btn_settings)

        // Button clicks (typing)
        button1.setOnClickListener {
            currentInputConnection.commitText("1", 1)
        }

        button2.setOnClickListener {
            currentInputConnection.commitText("2", 1)
        }

        // NEW: open Settings screen
        settingsBtn.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)

            // Optional: hide keyboard while settings is open (not required)
            // requestHideSelf(0)
        }

        return view
    }

    override fun onEvaluateInputViewShown(): Boolean {
        return true
    }
}