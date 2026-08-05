/**
 * Serializable widget node types — the **output** of WARP composition.
 *
 * These are plain data classes (not [@Composable][androidx.compose.runtime.Composable]) intended for:
 * - JSON serialization via [com.atriidev.warp_runtime.compose.WarpNode.toJson]
 * - Future platform renderers (Glance on Android, SwiftUI on iOS)
 *
 * Built by [com.atriidev.warp_runtime.compose.composeWarp] from the public composable DSL.
 */
package com.atriidev.warp_runtime.nodes

import kotlinx.serialization.Serializable

/**
 * Root type for every node in a composed widget tree.
 *
 * Implementations are `@Serializable` data classes. JSON includes a `"type"` discriminator
 * (see `@SerialName` on each implementation) for polymorphic decoding.
 */
@Serializable
sealed interface WarpNode
