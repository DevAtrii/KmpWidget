package com.atriidev.warp_runtime.nodes.actions

import kotlin.reflect.KClass

internal expect fun <A : Enum<A>> platformEnumConstants(idClass: KClass<A>): Array<A>
