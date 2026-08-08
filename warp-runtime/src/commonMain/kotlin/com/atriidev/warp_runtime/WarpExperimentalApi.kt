package com.atriidev.warp_runtime

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This WARP API is experimental and may change or be removed without notice."
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPEALIAS,
    AnnotationTarget.CONSTRUCTOR
)
annotation class WarpExperimentalApi