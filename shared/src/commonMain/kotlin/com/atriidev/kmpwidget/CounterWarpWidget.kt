package com.atriidev.kmpwidget

import androidx.compose.runtime.Composable
import com.atriidev.warp_runtime.compose.WarpBox
import com.atriidev.warp_runtime.compose.WarpButton
import com.atriidev.warp_runtime.compose.WarpColumn
import com.atriidev.warp_runtime.compose.WarpDivider
import com.atriidev.warp_runtime.compose.WarpImage
import com.atriidev.warp_runtime.compose.WarpProgressIndicator
import com.atriidev.warp_runtime.compose.WarpRow
import com.atriidev.warp_runtime.compose.WarpSpacer
import com.atriidev.warp_runtime.compose.WarpText
import com.atriidev.warp_runtime.nodes.actions.WarpActionId
import com.atriidev.warp_runtime.nodes.actions.asClickAction
import com.atriidev.warp_runtime.nodes.assets.WarpAssetId
import com.atriidev.warp_runtime.nodes.modifiers.WarpColor
import com.atriidev.warp_runtime.nodes.modifiers.WarpModifier
import com.atriidev.warp_runtime.nodes.style.WarpButtonColors
import com.atriidev.warp_runtime.nodes.style.WarpContentAlignment
import com.atriidev.warp_runtime.nodes.style.WarpFontWeight
import com.atriidev.warp_runtime.nodes.style.WarpProgressIndicatorStyle
import com.atriidev.warp_runtime.nodes.style.WarpTextStyle
import com.atriidev.warp_runtime.nodes.style.WarpVerticalAlignment
import com.atriidev.warp_ui.WarpClickHandler
import com.atriidev.warp_widget.WarpWidget
import com.atriidev.warp_widget.WarpWidgetSession
import com.atriidev.warp_widget.api.WidgetEnvironment
import com.atriidev.warp_widget.updateWarpWidgetState
import kotlin.math.abs
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Click action ids for [CounterWarpWidget]. */
enum class CounterActions(
    override val actionId: String,
) : WarpActionId {
    Increment("increment"),
    Decrement("decrement"),
    Reset("reset"),
    /** Flip [CounterState.mode] between counter and todo. */
    SwitchMode("switch_mode"),
    /** Toggle a todo — pass `"todoId"`. */
    ToggleTodo("toggle_todo"),
}

@Serializable
enum class WidgetMode {
    @SerialName("counter")
    Counter,

    @SerialName("todo")
    Todo,
}

@Serializable
data class TodoItem(
    val id: String,
    val title: String,
    val done: Boolean = false,
)

/** Sample list used as [CounterState] default. */
val SampleTodos: List<TodoItem> = listOf(
    TodoItem(id = "1", title = "Ship WarpImage"),
    TodoItem(id = "2", title = "Add todo mode", done = true),
    TodoItem(id = "3", title = "Polish SF Symbols"),
)

/** Serializable state for [CounterWarpWidget] — persisted as JSON under prefs key = widget id. */
@Serializable
data class CounterState(
    val mode: WidgetMode = WidgetMode.Counter,
    val count: Int = 0,
    val todos: List<TodoItem> = SampleTodos,
)

/** Type-safe asset keys — share with [CounterGlanceAppWidget.assets]. */
object CounterAssets {
    val NumberCircle = WarpAssetId("number.circle.fill")
    val Checklist = WarpAssetId("checklist")
    val Circle = WarpAssetId("circle")
    val CheckCircle = WarpAssetId("checkmark.circle.fill")
}

/**
 * Shared counter + todo [WarpWidget] — one definition for Glance + WidgetKit.
 *
 * Tap mode chips to switch. In todo mode, tap a row to mark done / undone.
 */
object CounterWarpWidget : WarpWidget<CounterState>(CounterState.serializer()) {
    override val id: String = "CounterWidget"

    override val iosGroupId: String = APP_GROUP_ID

    override val defaultState: CounterState = CounterState()

    @Composable
    override fun Content(env: WidgetEnvironment, state: CounterState) {
        println("WIDGET_ENV $env")

        WarpBox(
            modifier = WarpModifier
                .fillMaxSize()
                .background("#1B2838")
                .cornerRadius(16)
                .padding(12),
            contentAlignment = WarpContentAlignment.TopStart,
        ) {
            WarpColumn(
                modifier = WarpModifier.fillMaxWidth(),
            ) {
                ModeSwitcher(mode = state.mode)

                WarpSpacer(modifier = WarpModifier.height(6))

                WarpDivider(
                    modifier = WarpModifier.fillMaxWidth(),
                    thickness = 1,
                    color = WarpColor("#33FFFFFF"),
                )

                WarpSpacer(modifier = WarpModifier.height(8))

                when (state.mode) {
                    WidgetMode.Counter -> CounterBody(state)
                    WidgetMode.Todo -> TodoBody(state)
                }
            }
        }
    }

    override fun clickHandlers(session: WarpWidgetSession): List<WarpClickHandler<*>> =
        listOf(CounterWarpClickHandler(session))
}

@Composable
private fun ModeSwitcher(mode: WidgetMode) {
    WarpRow(
        modifier = WarpModifier.fillMaxWidth(),
        verticalAlignment = WarpVerticalAlignment.Center,
    ) {
        ModeChip(
            label = "Count",
            asset = CounterAssets.NumberCircle,
            selected = mode == WidgetMode.Counter,
            targetMode = WidgetMode.Counter,
        )
        WarpSpacer(modifier = WarpModifier.width(8))
        ModeChip(
            label = "Todo",
            asset = CounterAssets.Checklist,
            selected = mode == WidgetMode.Todo,
            targetMode = WidgetMode.Todo,
        )
    }
}

@Composable
private fun ModeChip(
    label: String,
    asset: WarpAssetId,
    selected: Boolean,
    targetMode: WidgetMode,
) {
    val bg = if (selected) "#4FC3F7" else "#243447"
    val fg = if (selected) "#1B2838" else "#B0BEC5"
    WarpRow(
        modifier = WarpModifier
            .background(bg)
            .cornerRadius(20)
            .padding(horizontal = 10, vertical = 6)
            .clickable(
                CounterActions.SwitchMode.asClickAction(
                    "mode" to targetMode.name.lowercase(),
                ),
            ),
        verticalAlignment = WarpVerticalAlignment.Center,
    ) {
        WarpImage(
            asset = asset.asSystem(),
            contentDescription = label,
            modifier = WarpModifier.size(14),
            tint = WarpColor(fg),
        )
        WarpSpacer(modifier = WarpModifier.width(4))
        WarpText(
            text = label,
            style = WarpTextStyle(
                color = WarpColor(fg),
                fontSize = 12f,
                fontWeight = WarpFontWeight.Medium,
            ),
            maxLines = 1,
        )
    }
}

@Composable
private fun CounterBody(state: CounterState) {
    // Nested column — Glance allows max 10 children per Column/Row.
    WarpColumn(modifier = WarpModifier.fillMaxWidth()) {
        val count = state.count
        val progress = (abs(count) % 100) / 100f

        WarpRow(
            modifier = WarpModifier
                .fillMaxWidth()
                .background("#243447")
                .cornerRadius(12)
                .padding(8),
            verticalAlignment = WarpVerticalAlignment.Center,
        ) {
            WarpButton(
                text = "−",
                onClick = CounterActions.Decrement.asClickAction(),
                modifier = WarpModifier
                    .size(40)
                    .cornerRadius(20),
                style = WarpTextStyle(
                    fontSize = 18f,
                    fontWeight = WarpFontWeight.Bold,
                ),
                colors = WarpButtonColors.of(
                    backgroundColor = "#E74C3C",
                    contentColor = "#FFFFFF",
                ),
            )
            WarpText(
                text = count.toString(),
                modifier = WarpModifier
                    .weight()
                    .padding(horizontal = 8, vertical = 0),
                style = WarpTextStyle(
                    color = WarpColor("#FFFFFF"),
                    fontSize = 22f,
                    fontWeight = WarpFontWeight.Bold,
                ),
                maxLines = 1,
            )
            WarpButton(
                text = "+",
                onClick = CounterActions.Increment.asClickAction(),
                modifier = WarpModifier
                    .size(40)
                    .cornerRadius(20),
                style = WarpTextStyle(
                    fontSize = 18f,
                    fontWeight = WarpFontWeight.Bold,
                ),
                colors = WarpButtonColors.of(
                    backgroundColor = "#27AE60",
                    contentColor = "#FFFFFF",
                ),
            )
        }

        WarpSpacer(modifier = WarpModifier.height(10))

        WarpProgressIndicator(
            modifier = WarpModifier.fillMaxWidth(),
            style = WarpProgressIndicatorStyle.Linear,
            progress = progress,
            color = WarpColor("#4FC3F7"),
            backgroundColor = WarpColor("#243447"),
        )
    }
}

@Composable
private fun TodoBody(state: CounterState) {
    val doneCount = state.todos.count { it.done }
    val total = state.todos.size
    val progress = if (total == 0) 0f else doneCount.toFloat() / total

    // Nested column — keeps root under Glance's 10-child Column limit
    // (flat forEach rows + spacers was dropping the 3rd todo).
    WarpColumn(modifier = WarpModifier.fillMaxWidth()) {
        WarpText(
            text = "$doneCount / $total done",
            style = WarpTextStyle(
                color = WarpColor("#B0BEC5"),
                fontSize = 12f,
                fontWeight = WarpFontWeight.Medium,
            ),
            maxLines = 1,
        )

        WarpSpacer(modifier = WarpModifier.height(6))

        state.todos.forEachIndexed { index, todo ->
            if (index > 0) {
                WarpSpacer(modifier = WarpModifier.height(4))
            }
            TodoRow(todo)
        }

        WarpSpacer(modifier = WarpModifier.height(8))

        WarpProgressIndicator(
            modifier = WarpModifier.fillMaxWidth(),
            style = WarpProgressIndicatorStyle.Linear,
            progress = progress,
            color = WarpColor("#4FC3F7"),
            backgroundColor = WarpColor("#243447"),
        )
    }
}

@Composable
private fun TodoRow(todo: TodoItem) {
    val icon = if (todo.done) CounterAssets.CheckCircle else CounterAssets.Circle
    val titleColor = if (todo.done) "#6B7C8A" else "#FFFFFF"
    WarpRow(
        modifier = WarpModifier
            .fillMaxWidth()
            .background("#243447")
            .cornerRadius(10)
            .padding(horizontal = 10, vertical = 8)
            .clickable(
                CounterActions.ToggleTodo.asClickAction("todoId" to todo.id),
            ),
        verticalAlignment = WarpVerticalAlignment.Center,
    ) {
        WarpImage(
            asset = icon.asSystem(),
            contentDescription = if (todo.done) "Done" else "Todo",
            modifier = WarpModifier.size(20),
            tint = WarpColor(if (todo.done) "#4FC3F7" else "#B0BEC5"),
        )
        WarpSpacer(modifier = WarpModifier.width(8))
        WarpText(
            text = todo.title,
            modifier = WarpModifier.weight(),
            style = WarpTextStyle(
                color = WarpColor(titleColor),
                fontSize = 14f,
                fontWeight = if (todo.done) WarpFontWeight.Normal else WarpFontWeight.Medium,
            ),
            maxLines = 1,
        )
    }
}

/**
 * Persists [CounterState] via [updateWarpWidgetState] (Glance prefs / UserDefaults + reload).
 */
class CounterWarpClickHandler(
    private val session: WarpWidgetSession,
) : WarpClickHandler<CounterActions>(CounterActions::class, CounterActions.entries) {

    override suspend fun onClick(actionId: CounterActions, parameters: Map<String, String>) {
        println("WARP_CLICK: id=$actionId, params=$parameters")
        updateWarpWidgetState(session.context, CounterWarpWidget) { state ->
            when (actionId) {
                CounterActions.Increment -> state.copy(count = state.count + 1)
                CounterActions.Decrement -> state.copy(count = state.count - 1)
                CounterActions.Reset -> state.copy(count = 0)
                CounterActions.SwitchMode -> {
                    val next = when (parameters["mode"]) {
                        "todo" -> WidgetMode.Todo
                        "counter" -> WidgetMode.Counter
                        else -> when (state.mode) {
                            WidgetMode.Counter -> WidgetMode.Todo
                            WidgetMode.Todo -> WidgetMode.Counter
                        }
                    }
                    state.copy(mode = next)
                }
                CounterActions.ToggleTodo -> {
                    val todoId = parameters["todoId"] ?: return@updateWarpWidgetState state
                    state.copy(
                        todos = state.todos.map { todo ->
                            if (todo.id == todoId) todo.copy(done = !todo.done) else todo
                        },
                    )
                }
            }
        }
    }
}
