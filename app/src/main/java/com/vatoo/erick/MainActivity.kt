package com.vatoo.erick

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import com.vatoo.erick.ui.theme.ERICKTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ERICKTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    val spriteSheet = remember {
                        BitmapFactory.decodeResource(resources, R.drawable.potions_full_corked)
                    }

                    val tileWidth = 16
                    val tileHeight = 24

                    val firstPotion = remember {
                        android.graphics.Bitmap.createBitmap(spriteSheet, 0, 0, tileWidth, tileHeight)
                    }

                    Image(
                        bitmap = firstPotion.asImageBitmap(),
                        contentDescription = "Potion",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}