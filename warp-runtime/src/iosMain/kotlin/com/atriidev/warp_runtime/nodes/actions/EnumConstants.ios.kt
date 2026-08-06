package com.atriidev.warp_runtime.nodes.actions

import kotlin.reflect.KClass

internal actual fun <A : Enum<A>> platformEnumConstants(idClass: KClass<A>): Array<A> {
    error(
        "platformEnumConstants is unavailable on iOS. Use WarpClickHandler with explicit action entries.",
    )
}
