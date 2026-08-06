package com.atriidev.warp_runtime.nodes.actions

import kotlin.reflect.KClass

internal actual fun <A : Enum<A>> platformEnumConstants(idClass: KClass<A>): Array<A> {
    @Suppress("UNCHECKED_CAST")
    return idClass.java.enumConstants as Array<A>
}
