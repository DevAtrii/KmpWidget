package com.atriidev.warp_runtime

/**
 * Returns the name of the current platform running this KMP module.
 *
 * Used by KMP template scaffolding. Not involved in widget composition or JSON output.
 *
 * Implementations: `"Android"`, `"iOS"`, `"JVM"`.
 */
expect fun platform(): String
