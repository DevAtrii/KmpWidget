@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package com.atriidev.warp_runtime.nodes.actions

import com.atriidev.warp_runtime.log.WarpLogger
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SealedClassSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.serializer

/**
 * Marker for widget click-action sealed hierarchies.
 *
 * Define a `@Serializable sealed class` with action variants and parameters only —
 * wire ids and codec come from [warpActionFamily] / [asClickAction]:
 *
 * ```
 * @Serializable
 * sealed class CounterActions {
 *     @Serializable data object Increment : CounterActions()
 *     @Serializable data class ToggleTodo(val todoId: String) : CounterActions()
 * }
 *
 * Handler: `WarpClickHandler<CounterActions>(CounterActions.serializer())`
 * ```
 *
 * Wire shape stays `{ "actionId": "toggle_todo", "parameters": { "todoId": "1" } }`.
 * [actionId] is snake_case of the variant name unless overridden with `@SerialName`.
 */
interface WarpTypedAction

/**
 * Codec for a widget's `@Serializable` sealed click-action hierarchy.
 *
 * Prefer passing [CounterActions.serializer()] to [com.atriidev.warp_ui.WarpClickHandler] —
 * no manual [actionIds] or [decode].
 */
interface WarpActionFamily<A : Any> {
    /** All wire ids this family handles. */
    val actionIds: Set<String>

    /** Decode platform callback; return null for unknown / malformed payloads. */
    fun decode(actionId: String, parameters: WarpActionParameters): A?
}

/** Auto codec from kotlinx.serialization — use via companion delegation. */
inline fun <reified A : Any> warpActionFamily(): WarpActionFamily<A> =
    warpActionFamily(serializer())

/** Auto codec from an explicit [baseSerializer]. */
fun <A : Any> warpActionFamily(baseSerializer: KSerializer<A>): WarpActionFamily<A> =
    SerializableWarpActionFamily(baseSerializer)

/** Encode typed action → serializable [ClickAction] for the WARP tree. */
inline fun <reified A : Any> A.asClickAction(): ClickAction =
    if (this is ClickAction) this else encodeWarpAction(this, serializer())

/** Encode [action] using its concrete kotlinx.serialization serializer. */
fun <A : Any> encodeWarpAction(action: A, concreteSerializer: KSerializer<A>): ClickAction {
    val jsonElement = warpActionJson.encodeToJsonElement(concreteSerializer, action)
    val polymorphicType = (jsonElement as? JsonObject)?.get("type")?.jsonPrimitive?.content
    val serialName = polymorphicType ?: concreteSerializer.descriptor.serialName
    val wireId = wireActionId(serialName)
    val parameters = when (jsonElement) {
        is JsonObject -> jsonElement
            .filterKeys { it != "type" }
            .mapValues { (_, element) -> element.toParameterString() }
        else -> emptyMap()
    }
    return ClickAction(actionId = wireId, parameters = parameters)
}

class SerializableWarpActionFamily<A : Any>(
    private val baseSerializer: KSerializer<A>,
) : WarpActionFamily<A> {
    private val sealedSerializer = baseSerializer as? SealedClassSerializer<A>
        ?: error(
            "${baseSerializer.descriptor.serialName} must be a @Serializable sealed class",
        )

    private val wireIdToSerialName: Map<String, String> = buildMap {
        val variantsDescriptor = sealedSerializer.descriptor.getElementDescriptor(1)
        for (index in 0 until variantsDescriptor.elementsCount) {
            val serialName = variantsDescriptor.getElementName(index)
            put(wireActionId(serialName), serialName)
        }
    }

    override val actionIds: Set<String> = wireIdToSerialName.keys

    override fun decode(actionId: String, parameters: WarpActionParameters): A? {
        val serialName = wireIdToSerialName[actionId] ?: run {
            WarpLogger.w(
                "WarpActionFamily",
                "Unknown actionId=$actionId (registered: ${wireIdToSerialName.keys})"
            )
            return null
        }
        val json = buildJsonObject {
            put("type", JsonPrimitive(serialName))
            parameters.forEach { (key, value) ->
                val trimmed = value.trim()
                val element = if ((trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                    (trimmed.startsWith("[") && trimmed.endsWith("]"))
                ) {
                    try {
                        warpActionJson.parseToJsonElement(trimmed)
                    } catch (_: Exception) {
                        JsonPrimitive(value)
                    }
                } else {
                    JsonPrimitive(value)
                }
                put(key, element)
            }
        }
        return try {
            val decoded = warpActionJson.decodeFromJsonElement(sealedSerializer, json)
            WarpLogger.d(
                "WarpActionFamily",
                "Successfully decoded action $decoded (actionId=$actionId)"
            )
            decoded
        } catch (e: Exception) {
            WarpLogger.e("WarpActionFamily", "Failed to decode action $actionId with json=$json", e)
            null
        }
    }
}

/** PascalCase, FQCN, or `@SerialName` → stable wire id (snake_case). */
internal fun wireActionId(serialName: String): String {
    val simpleName = serialName.substringAfterLast('.')
    if (simpleName.all { it.isLowerCase() || it == '_' || it.isDigit() }) {
        return simpleName
    }
    return simpleName
        .replace(Regex("([a-z0-9])([A-Z])")) { match ->
            "${match.groupValues[1]}_${match.groupValues[2]}"
        }
        .lowercase()
}

private fun WarpActionParameters.toJsonObject(): JsonObject = buildJsonObject {
    // Wire values are always strings — keep as string JSON primitives so `todoId: String`
    // does not fail decode when the id looks numeric (e.g. "1", "2").
    forEach { (key, value) -> put(key, JsonPrimitive(value)) }
}

private fun JsonElement.toParameterString(): String = when (this) {
    is JsonPrimitive -> content
    else -> warpActionJson.encodeToString(JsonElement.serializer(), this)
}

private val warpActionJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}
