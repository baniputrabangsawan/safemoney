package com.jagauang.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.jagauang.app.ui.theme.JagaUangTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JagaUangTheme {
                JagaUangApp(rememberAppContainer())
            }
        }
    }
}
