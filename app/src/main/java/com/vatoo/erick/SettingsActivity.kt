package com.vatoo.erick

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Reverted to original ERIC-48 shell
            SettingsScreen()
        }
    }
}
