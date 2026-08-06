package com.atriidev.warp_ui.glance.internal

import androidx.glance.action.ActionParameters
import com.atriidev.warp_runtime.nodes.actions.WarpActionParameters
import kotlinx.serialization.json.Json

/** Typed keys used to pass [com.atriidev.warp_runtime.nodes.actions.ClickAction] through Glance. */
@PublishedApi
internal object WarpGlanceActionKeys {
    val ActionId = ActionParameters.Key<String>("warp_action_id")
    val ParametersJson = ActionParameters.Key<String>("warp_action_parameters")

    private val json = Json { encodeDefaults = true }

    fun encodeParameters(parameters: WarpActionParameters): String =
        json.encodeToString(parameters)

    fun decodeParameters(raw: String?): WarpActionParameters {
        if (raw.isNullOrBlank()) return emptyMap()
        return json.decodeFromString(raw)
    }

    fun actionParametersOf(
        actionId: String,
        parameters: WarpActionParameters,
    ): ActionParameters = androidx.glance.action.actionParametersOf(
        ActionId to actionId,
        ParametersJson to encodeParameters(parameters),
    )
}
