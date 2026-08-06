package com.atriidev.warp_runtime.nodes.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Base type for every action stored on a [com.atriidev.warp_runtime.nodes.WarpNode] tree.
 *
 * Actions are **data only** — no lambdas, no platform APIs. Each implementation is
 * `@Serializable` so it can travel in JSON from common code to Android/iOS renderers.
 *
 * Platform code maps [ClickAction.id] (and future action payloads) to native handlers:
 * - Android Glance: `ActionCallback`, `actionStartActivity`, etc.
 * - iOS WidgetKit: `AppIntent`, deep links, etc.
 *
 * ### Adding a new action type
 * 1. Add a new `@Serializable` `@SerialName("…")` data class implementing [WarpAction].
 * 2. Add a composable factory (for example `actionStartActivity(...)`).
 * 3. Teach each platform renderer to handle the new `"type"` discriminator in JSON.
 */
@Serializable
sealed interface WarpAction

/**
 * String parameters attached to an action, copied into the serialized tree.
 *
 * Keep values primitive (strings) so JSON stays portable. Platform handlers parse as needed.
 */
typealias WarpActionParameters = Map<String, String>
