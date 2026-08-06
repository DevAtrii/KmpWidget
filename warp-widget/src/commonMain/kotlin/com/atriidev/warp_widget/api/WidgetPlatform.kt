package com.atriidev.warp_widget.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IosVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
)

@Serializable
data class AppInfo(
    val version: Int,
    val versionName: String,
    val appName: String,
)

/**
 * Process-level platform identity for a widget host.
 *
 * Nested inside [WidgetEnvironment]; kept serializable for timeline / debug payloads.
 */
@Serializable
sealed class WidgetPlatform {
    abstract val osVersion: Int
    abstract val debug: Boolean
    abstract val appInfo: AppInfo
    abstract val isDynamicColorSupported: Boolean

    @Serializable
    @SerialName("ios")
    data class Ios(
        override val osVersion: Int,
        override val debug: Boolean,
        override val appInfo: AppInfo,
        val exactVersion: IosVersion,
    ) : WidgetPlatform() {
        override val isDynamicColorSupported: Boolean get() = false
    }

    @Serializable
    @SerialName("android")
    data class Android(
        override val osVersion: Int,
        override val debug: Boolean,
        override val appInfo: AppInfo,
    ) : WidgetPlatform() {
        override val isDynamicColorSupported: Boolean
            get() = osVersion >= 31 // Android 12
    }
}

internal expect val widgetPlatform: WidgetPlatform

val WidgetPlatform.isIos: Boolean
    get() = this is WidgetPlatform.Ios

val WidgetPlatform.isAndroid: Boolean
    get() = this is WidgetPlatform.Android

inline fun WidgetPlatform.ifAndroid(crossinline action: (WidgetPlatform.Android) -> Unit) {
    if (this.isAndroid) action(this as WidgetPlatform.Android)
}

inline fun WidgetPlatform.ifIos(crossinline action: (WidgetPlatform.Ios) -> Unit) {
    if (this.isIos) action(this as WidgetPlatform.Ios)
}
