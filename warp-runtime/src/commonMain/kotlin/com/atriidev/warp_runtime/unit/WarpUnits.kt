package com.atriidev.warp_runtime.unit

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Dimension value class representing Density-independent Pixels (dp).
 */
@Serializable
@JvmInline
value class Dp(val value: Float) : Comparable<Dp> {
    override fun compareTo(other: Dp): Int = value.compareTo(other.value)

    override fun toString(): String = if (isUnspecified) "Dp.Unspecified" else "${value}.dp"

    companion object {
        val Unspecified = Dp(Float.NaN)
        val Zero = Dp(0f)
    }
}

val Dp.isSpecified: Boolean get() = !value.isNaN()
val Dp.isUnspecified: Boolean get() = value.isNaN()

inline val Int.dp: Dp get() = Dp(this.toFloat())
inline val Float.dp: Dp get() = Dp(this)
inline val Double.dp: Dp get() = Dp(this.toFloat())

/**
 * Dimension value class representing Scale-independent Pixels (sp) for typography.
 */
@Serializable
@JvmInline
value class Sp(val value: Float) : Comparable<Sp> {
    override fun compareTo(other: Sp): Int = value.compareTo(other.value)

    override fun toString(): String = if (isUnspecified) "Sp.Unspecified" else "${value}.sp"

    companion object {
        val Unspecified = Sp(Float.NaN)
        val Zero = Sp(0f)
    }
}

val Sp.isSpecified: Boolean get() = !value.isNaN()
val Sp.isUnspecified: Boolean get() = value.isNaN()

inline val Int.sp: Sp get() = Sp(this.toFloat())
inline val Float.sp: Sp get() = Sp(this)
inline val Double.sp: Sp get() = Sp(this.toFloat())

typealias WarpDp = Dp
typealias WarpSp = Sp
