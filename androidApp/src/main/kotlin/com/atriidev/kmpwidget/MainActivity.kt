package com.atriidev.kmpwidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.atriidev.warp_widget.api.PlatformContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)


        setContent {
            val context = LocalContext.current
            val platformContext = remember(context) {
                PlatformContext(context)
            }
            App(platformContext = platformContext)
        }
    }
}
