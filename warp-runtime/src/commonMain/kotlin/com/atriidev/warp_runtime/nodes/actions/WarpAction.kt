package com.atriidev.warp_runtime.nodes.actions

import androidx.compose.runtime.Stable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Base type for every action stored on a [com.atriidev.warp_runtime.nodes.WarpNode] tree.
 *
 * Actions are **data only** — no lambdas, no platform APIs. Each implementation is
 * `@Serializable` so it can travel in JSON from common code to Android/iOS renderers.
 *
 * Platform code exhaustively handles each action subtype with `when (action)`:
 * - Android Glance: `ActionCallback`, `actionStartActivity`, etc.
 * - iOS WidgetKit: `AppIntent`, deep links, etc.
 *
 * ### Adding a new action type
 * 1. Add a new `@Serializable` `@SerialName("…")` data class implementing [WarpAction].
 * 2. Add a factory (for example `actionStartActivity(...)`).
 * 3. Handle the new subtype in each platform renderer. Because this interface is sealed,
 *    Kotlin reports every `when (action)` that needs a new branch.
 */
@Serializable
@Stable
sealed interface WarpAction

/**
 * String parameters attached to an action, copied into the serialized tree.
 *
 * Keep values primitive (strings) so JSON stays portable. Platform handlers parse as needed.
 */
typealias WarpActionParameters = Map<String, String>
