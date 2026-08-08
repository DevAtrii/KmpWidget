package com.atriidev.warp_widget.api

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.glance.LocalContext
import com.atriidev.warp_widget.WarpWidget

/**
 * Android [PlatformContext]: application / widget [Context] for Glance state and updates.
 */
@Stable
actual class PlatformContext(val context: Context)


@Composable
actual fun <T : Any> rememberPlatformContext(widget: WarpWidget<T>): PlatformContext {
    val context = LocalContext.current
    return remember(context, widget) {
        PlatformContext(context)
    }
}