package com.atriidev.warp.ir

import kotlinx.serialization.Serializable

@Serializable
enum class WarpVerticalAlignment {
    Top,
    CenterVertically,
    Bottom,
}

@Serializable
enum class WarpHorizontalAlignment {
    Start,
    CenterHorizontally,
    End,
}
