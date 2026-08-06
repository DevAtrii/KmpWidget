package com.atriidev.warp_widget.api

import android.os.Build

internal actual val widgetPlatform: WidgetPlatform
    get() = WidgetPlatform.Android(
        osVersion = Build.VERSION.SDK_INT,
        debug = false,
        appInfo = AppInfo(
            version = 0,
            versionName = "",
            appName = "",
        ),
    )
