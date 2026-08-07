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
import com.atriidev.warp_runtime.nodes.actions.asClickAction
import com.atriidev.warp_runtime.nodes.assets.WarpAssetId
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
import com.atriidev.warp_widget.ui.WarpTheme
import com.atriidev.warp_widget.updateWarpWidgetState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.abs

/** Type-safe click actions for [CounterWarpWidget]. */
@Serializable
sealed class CounterActions {
    @Serializable
    data object Increment : CounterActions()

    @Serializable
    data object Decrement : CounterActions()

    @Serializable
    data object Reset : CounterActions()

    /** Switch to [mode] (Count / To-do chip). */
    @Serializable
    data class SwitchMode(val mode: WidgetMode) : CounterActions()

    /** Toggle done state for [todoId]. */
    @Serializable
    data class ToggleTodo(val todoId: String) : CounterActions()
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

        WarpTheme(environment = env) {
            val colors = WarpTheme.colors
            WarpBox(
                modifier = WarpModifier
                    .fillMaxSize()
                    .background(colors.widgetBackground)
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
                        color = colors.outline,
                    )

                    WarpSpacer(modifier = WarpModifier.height(8))

                    when (state.mode) {
                        WidgetMode.Counter -> CounterBody(state)
                        WidgetMode.Todo -> TodoBody(state)
                    }
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
    val colors = WarpTheme.colors
    val bg = if (selected) colors.primary else colors.surfaceVariant
    val fg = if (selected) colors.onPrimary else colors.onSurfaceVariant
    WarpRow(
        modifier = WarpModifier
            .background(bg)
            .cornerRadius(20)
            .padding(horizontal = 10, vertical = 6)
            .clickable(CounterActions.SwitchMode(targetMode)),
        verticalAlignment = WarpVerticalAlignment.Center,
    ) {
        WarpImage(
            asset = asset.asSystem(),
            contentDescription = label,
            modifier = WarpModifier.size(14),
            tint = fg,
        )
        WarpSpacer(modifier = WarpModifier.width(4))
        WarpText(
            text = label,
            style = WarpTextStyle(
                color = fg,
                fontSize = 12f,
                fontWeight = WarpFontWeight.Medium,
            ),
            maxLines = 1,
        )
    }
}

@Composable
private fun CounterBody(state: CounterState) {
    val colors = WarpTheme.colors
    // Nested column — Glance allows max 10 children per Column/Row.
    WarpColumn(modifier = WarpModifier.fillMaxWidth()) {
        val count = state.count
        val progress = (abs(count) % 100) / 100f

        WarpRow(
            modifier = WarpModifier
                .fillMaxWidth()
                .background(colors.surfaceVariant)
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
                    .padding(horizontal = 8, vertical = 0)
                    .clickable(CounterActions.Reset),
                style = WarpTextStyle(
                    color = colors.onSurface,
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
            color = colors.primary,
            backgroundColor = colors.surfaceVariant,
        )
    }
}

@Composable
private fun TodoBody(state: CounterState) {
    val colors = WarpTheme.colors
    val doneCount = state.todos.count { it.done }
    val total = state.todos.size
    val progress = if (total == 0) 0f else doneCount.toFloat() / total

    // Nested column — keeps root under Glance's 10-child Column limit
    // (flat forEach rows + spacers was dropping the 3rd todo).
    WarpColumn(modifier = WarpModifier.fillMaxWidth()) {
        WarpText(
            text = "$doneCount / $total done",
            style = WarpTextStyle(
                color = colors.onSurfaceVariant,
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
            color = colors.primary,
            backgroundColor = colors.surfaceVariant,
        )
    }
}

@Composable
private fun TodoRow(todo: TodoItem) {
    val colors = WarpTheme.colors
    val icon = if (todo.done) CounterAssets.CheckCircle else CounterAssets.Circle
    val titleColor = if (todo.done) colors.onSurfaceVariant else colors.onSurface
    WarpRow(
        modifier = WarpModifier
            .fillMaxWidth()
            .background(colors.surfaceVariant)
            .cornerRadius(10)
            .padding(horizontal = 10, vertical = 8)
            .clickable(CounterActions.ToggleTodo(todo.id)),
        verticalAlignment = WarpVerticalAlignment.Center,
    ) {
        WarpImage(
            asset = icon.asSystem(),
            contentDescription = if (todo.done) "Done" else "Todo",
            modifier = WarpModifier.size(20),
            tint = if (todo.done) colors.primary else colors.onSurfaceVariant,
        )
        WarpSpacer(modifier = WarpModifier.width(8))
        WarpText(
            text = todo.title,
            modifier = WarpModifier.weight(),
            style = WarpTextStyle(
                color = titleColor,
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
) : WarpClickHandler<CounterActions>(CounterActions.serializer()) {

    override suspend fun onClick(action: CounterActions) {
        println("WARP_CLICK: action=$action")
        updateWarpWidgetState(session.context, CounterWarpWidget) { state ->
            when (action) {
                CounterActions.Increment -> state.copy(count = state.count + 1)
                CounterActions.Decrement -> state.copy(count = state.count - 1)
                CounterActions.Reset -> state.copy(count = 0)
                is CounterActions.SwitchMode -> state.copy(mode = action.mode)
                is CounterActions.ToggleTodo -> state.copy(
                    todos = state.todos.map { todo ->
                        if (todo.id == action.todoId) todo.copy(done = !todo.done) else todo
                    },
                )
            }
        }
    }
}
