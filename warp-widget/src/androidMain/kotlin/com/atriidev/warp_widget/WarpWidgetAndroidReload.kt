package com.atriidev.warp_widget

import android.content.BroadcastReceiver
import android.content.ComponentCallbacks
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import com.atriidev.warp_widget.api.PlatformContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "WarpWidgetAndroidReload"

private const val ACTION_UI_MODE_CHANGED = "android.intent.action.UI_MODE_CHANGED"

/**
 * Reloads registered Glance widgets when system uiMode (light/dark) changes.
 *
 * Glance snapshots theme at last [androidx.glance.appwidget.GlanceAppWidget.update];
 * without an explicit reload, home-screen widgets keep stale [WidgetEnvironment.theme].
 */
internal object WarpWidgetAndroidReload {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Volatile
    private var installed = false

    private var lastNightMode: Int? = null

    private var uiModeReceiver: BroadcastReceiver? = null

    fun install(context: Context) {
        if (installed) return
        synchronized(this) {
            if (installed) return
            val app = context.applicationContext
            lastNightMode = app.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

            app.registerComponentCallbacks(
                object : ComponentCallbacks {
                    override fun onConfigurationChanged(newConfig: Configuration) {
                        onUiModeChanged(app, newConfig)
                    }

                    override fun onLowMemory() = Unit
                },
            )

            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context, intent: Intent) {
                    when (intent.action) {
                        ACTION_UI_MODE_CHANGED,
                        Intent.ACTION_CONFIGURATION_CHANGED,
                        -> scheduleReloadAll(ctx, "broadcast:${intent.action}")
                    }
                }
            }
            val filter = IntentFilter().apply {
                addAction(ACTION_UI_MODE_CHANGED)
                addAction(Intent.ACTION_CONFIGURATION_CHANGED)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                app.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                app.registerReceiver(receiver, filter)
            }
            uiModeReceiver = receiver

            WarpWidgetAndroidRegistry.ensureAllWidgetReceiversRegistered(app)

            installed = true
            Log.d(TAG, "Installed uiMode reload listeners")
        }
    }

    fun onUiModeChanged(context: Context, config: Configuration) {
        val nightMode = config.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val previous = lastNightMode
        lastNightMode = nightMode
        if (previous != null && previous != nightMode) {
            scheduleReloadAll(context, "uiMode:$previous→$nightMode")
        }
    }

    fun scheduleReloadAll(context: Context, reason: String = "manual") {
        val appContext = context.applicationContext
        Log.d(TAG, "scheduleReloadAll ($reason)")
        scope.launch {
            WarpWidgetAndroidRegistry.ensureAllWidgetReceiversRegistered(appContext)
            WarpWidgetAndroidRegistry.reloadAll(PlatformContext(appContext))
        }
    }
}
