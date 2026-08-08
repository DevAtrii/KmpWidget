package com.atriidev.kmpwidget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.atriidev.warp_runtime.compose.WarpBox
import com.atriidev.warp_runtime.compose.WarpButton
import com.atriidev.warp_runtime.compose.WarpColumn
import com.atriidev.warp_runtime.compose.WarpDivider
import com.atriidev.warp_runtime.compose.WarpImage
import com.atriidev.warp_runtime.compose.WarpProgressIndicator
import com.atriidev.warp_runtime.compose.WarpRow
import com.atriidev.warp_runtime.compose.WarpSpacer
import com.atriidev.warp_runtime.compose.WarpText
import com.atriidev.warp_runtime.log.WarpLogger
import com.atriidev.warp_runtime.log.WarpLoggerLevel
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
import com.atriidev.warp_widget.WarpWidgetStateScope
import com.atriidev.warp_widget.api.WidgetEnvironment
import com.atriidev.warp_widget.api.isAndroid
import com.atriidev.warp_widget.ui.WarpAdaptiveContent
import com.atriidev.warp_widget.ui.WarpAdaptiveSize
import com.atriidev.warp_widget.ui.WarpTheme
import com.atriidev.warp_widget.ui.adaptiveSize
import com.atriidev.warp_widget.ui.adaptiveValue
import com.atriidev.warp_widget.ui.isMediumAdaptive
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

    /** Toggle done state for [todo]. */
    @Serializable
    data class ToggleTodo(val todo: TodoItem) : CounterActions()
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
    TodoItem(id = "4", title = "LazyColumn on large widget"),
    TodoItem(id = "5", title = "Wire WidgetKit reload"),
//    TodoItem(id = "6", title = "Add adaptive sizing", done = true),
//    TodoItem(id = "7", title = "Test Android resize"),
//    TodoItem(id = "8", title = "Document WarpButton API"),
//    TodoItem(id = "9", title = "Ship iOS medium layout"),
//    TodoItem(id = "10", title = "Release v1.0"),
)

/** Serializable state for [CounterWarpWidget] — persisted as JSON under prefs key = widget id. */
@Serializable
@Stable
data class CounterState(
    val mode: WidgetMode = WidgetMode.Counter,
    val count: Int = 0,
    val todos: List<TodoItem> = SampleTodos,
)

/** Merges newly added [SampleTodos] into persisted state (keeps [TodoItem.done] by id). */
internal fun CounterState.withMergedSampleTodos(): CounterState {
    if (todos.size >= SampleTodos.size) return this
    val byId = todos.associateBy { it.id }
    return copy(
        todos = SampleTodos.map { sample ->
            byId[sample.id]?.let { existing -> sample.copy(done = existing.done) } ?: sample
        },
    )
}

/** Type-safe asset keys — share with [CounterGlanceAppWidget.assets]. */
object CounterAssets {
    val NumberCircle = WarpAssetId("number.circle.fill")
    val Checklist = WarpAssetId("checklist")
    val Circle = WarpAssetId("circle")
    val CheckCircle = WarpAssetId("checkmark.circle.fill")
}

/**
 * Counter + todo [WarpWidget] — one definition for Glance + WidgetKit.
 *
 * State is **per home-screen instance** ([WarpWidgetStateScope.Instance]).
 * Tap mode chips to switch. In todo mode, tap a row to mark done / undone.
 */
object CounterWarpWidget : WarpWidget<CounterState>(CounterState.serializer()) {
    override val id: String = "CounterWidget"

    override val iosGroupId: String = APP_GROUP_ID

    override val stateScope: WarpWidgetStateScope = WarpWidgetStateScope.Instance

    override val defaultState: CounterState = CounterState()

    @Composable
    override fun Content(env: WidgetEnvironment, state: CounterState) {
        val state = state.withMergedSampleTodos()
        WarpLogger.level = WarpLoggerLevel.Debug
        WarpTheme(
            environment = env,
            darkTheme = false.takeIf { env.platform.isAndroid }
        ) {
            WarpAdaptiveContent(
                environment = env,
                small = { CounterWidgetContent(state, env, compact = true) },
                medium = { CounterWidgetContent(state, env) },
                large = { CounterWidgetContent(state, env, spacious = true) },
            )
        }
    }

    override fun clickHandlers(session: WarpWidgetSession): List<WarpClickHandler<*>> =
        listOf(CounterWarpClickHandler(session))
}

@Composable
private fun CounterWidgetContent(
    state: CounterState,
    env: WidgetEnvironment,
    compact: Boolean = false,
    spacious: Boolean = false,
) {
    val colors = WarpTheme.colors
    val isMedium = env.isMediumAdaptive()
    val todoCompact = compact || (state.mode == WidgetMode.Todo && isMedium)
    val outerPadding = when {
        spacious -> 16
        compact -> 8
        else -> 12
    }
    val cornerRadius = env.adaptiveValue(small = 12, medium = 16, large = 20)
    WarpBox(
        modifier = WarpModifier
            .fillMaxSize()
            .background(colors.widgetBackground)
            .cornerRadius(cornerRadius)
            .padding(outerPadding),
        contentAlignment = WarpContentAlignment.TopStart,
    ) {
        WarpColumn(
            modifier = WarpModifier.fillMaxWidth(),
        ) {
            ModeSwitcher(
                mode = state.mode,
                env = env,
                compact = compact,
            )

            WarpSpacer(modifier = WarpModifier.height(if (compact) 4 else 6))

            WarpDivider(
                modifier = WarpModifier.fillMaxWidth(),
                thickness = 1,
                color = colors.outline,
            )

            WarpSpacer(
                modifier = WarpModifier.height(
                    when {
                        spacious -> 12
                        state.mode == WidgetMode.Todo && isMedium -> 4
                        compact -> 4
                        else -> 8
                    },
                ),
            )

            when (state.mode) {
                WidgetMode.Counter -> CounterBody(state, env, spacious = spacious)
                WidgetMode.Todo -> TodoBody(state, env, compact = todoCompact)
            }
        }
    }
}

@Composable
private fun ModeSwitcher(
    mode: WidgetMode,
    env: WidgetEnvironment,
    compact: Boolean = false,
) {
    WarpRow(
        modifier = WarpModifier.fillMaxWidth(),
        verticalAlignment = WarpVerticalAlignment.Center,
    ) {
        ModeChip(
            label = "Count",
            asset = CounterAssets.NumberCircle,
            selected = mode == WidgetMode.Counter,
            action = CounterActions.SwitchMode(WidgetMode.Counter),
            compact = compact,
        )
        WarpSpacer(modifier = WarpModifier.width(if (compact) 6 else 8))
        ModeChip(
            label = "Todo",
            asset = CounterAssets.Checklist,
            selected = mode == WidgetMode.Todo,
            action = CounterActions.SwitchMode(WidgetMode.Todo),
            compact = compact,
        )
        WarpSpacer(modifier = WarpModifier.weight())
        AdaptiveSizeLabel(env = env, compact = compact)
    }
}

@Composable
private fun AdaptiveSizeLabel(
    env: WidgetEnvironment,
    compact: Boolean = false,
) {
    val colors = WarpTheme.colors
    val label = when (env.adaptiveSize()) {
        WarpAdaptiveSize.Small -> "small"
        WarpAdaptiveSize.Medium -> "medium"
        WarpAdaptiveSize.Large -> "large"
    }
    WarpText(
        text = label,
        style = WarpTextStyle(
            color = colors.onSurfaceVariant,
            fontSize = if (compact) 10f else 11f,
            fontWeight = WarpFontWeight.Medium,
        ),
        maxLines = 1,
    )
}

@Composable
private fun ModeChip(
    label: String,
    asset: WarpAssetId,
    selected: Boolean,
    action: CounterActions,
    compact: Boolean = false,
) {
    val colors = WarpTheme.colors
    val bg = if (selected) colors.primary else colors.surfaceVariant
    val fg = if (selected) colors.onPrimary else colors.onSurfaceVariant
    val chipPaddingH = if (compact) 8 else 10
    val chipPaddingV = if (compact) 4 else 6
    val iconSize = if (compact) 12 else 14
    val fontSize = if (compact) 11f else 12f
    WarpRow(
        modifier = WarpModifier
            .background(bg)
            .cornerRadius(20)
            .padding(horizontal = chipPaddingH, vertical = chipPaddingV)
            .clickable(action.asClickAction()),
        verticalAlignment = WarpVerticalAlignment.Center,
    ) {
        WarpImage(
            asset = asset.asSystem(),
            contentDescription = label,
            modifier = WarpModifier.size(iconSize),
            tint = fg,
        )
        WarpSpacer(modifier = WarpModifier.width(4))
        WarpText(
            text = label,
            style = WarpTextStyle(
                color = fg,
                fontSize = fontSize,
                fontWeight = WarpFontWeight.Medium,
            ),
            maxLines = 1,
        )
    }
}

@Composable
private fun CounterBody(
    state: CounterState,
    env: WidgetEnvironment,
    spacious: Boolean = false,
) {
    val colors = WarpTheme.colors
    val buttonSize = env.adaptiveValue(small = 36, medium = 40, large = 48)
    val countFontSize = env.adaptiveValue(small = 22f, medium = 26f, large = 32f)
    val cardPadding = if (spacious) 12 else 8
    // Nested column — Glance allows max 10 children per Column/Row.
    WarpColumn(modifier = WarpModifier.fillMaxWidth()) {
        val count = state.count
        val progress = (abs(count) % 100) / 100f

        WarpRow(
            modifier = WarpModifier
                .fillMaxWidth()
                .background(colors.surfaceVariant)
                .cornerRadius(12)
                .padding(cardPadding),
            verticalAlignment = WarpVerticalAlignment.Center,
        ) {
            WarpButton(
                text = "−",
                onClick = CounterActions.Decrement.asClickAction(),
                modifier = WarpModifier
                    .size(buttonSize)
                    .cornerRadius(buttonSize / 2),
                style = WarpTextStyle(
                    fontSize = if (spacious) 20f else 18f,
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
                    .clickable(CounterActions.Reset.asClickAction()),
                style = WarpTextStyle(
                    color = colors.onSurface,
                    fontSize = countFontSize,
                    fontWeight = WarpFontWeight.Bold,
                ),
                maxLines = 1,
            )
            WarpButton(
                text = "+",
                onClick = CounterActions.Increment.asClickAction(),
                modifier = WarpModifier
                    .size(buttonSize)
                    .cornerRadius(buttonSize / 2),
                style = WarpTextStyle(
                    fontSize = if (spacious) 20f else 18f,
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
private fun TodoBody(
    state: CounterState,
    env: WidgetEnvironment,
    compact: Boolean = false,
) {
    val colors = WarpTheme.colors
    val doneCount = state.todos.count { it.done }
    val total = state.todos.size
    val progress = if (total == 0) 0f else doneCount.toFloat() / total
    // Large shows all sample todos (pre-lazy A/B); small/medium stay capped.
    val maxVisible = env.adaptiveValue(small = 2, medium = 2, large = 10)
    val visibleTodos = state.todos.take(maxVisible)

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

        WarpSpacer(modifier = WarpModifier.height(if (compact) 4 else 6))

        visibleTodos.forEachIndexed { index, todo ->
            if (index > 0) {
                WarpSpacer(modifier = WarpModifier.height(if (compact) 3 else 4))
            }
            TodoRow(todo, compact = compact)
        }

        if (visibleTodos.size < total) {
            WarpSpacer(modifier = WarpModifier.height(4))
            WarpText(
                text = "+${total - visibleTodos.size} more",
                style = WarpTextStyle(
                    color = colors.onSurfaceVariant,
                    fontSize = 11f,
                    fontWeight = WarpFontWeight.Medium,
                ),
                maxLines = 1,
            )
        }

        WarpSpacer(modifier = WarpModifier.height(if (compact) 4 else 8))

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
private fun TodoRow(
    todo: TodoItem,
    compact: Boolean = false,
) {
    val colors = WarpTheme.colors
    val icon = if (todo.done) CounterAssets.CheckCircle else CounterAssets.Circle
    val titleColor = if (todo.done) colors.onSurfaceVariant else colors.onSurface
    val iconSize = if (compact) 18 else 20
    val titleSize = if (compact) 13f else 14f
    val rowPaddingV = if (compact) 6 else 8
    WarpRow(
        modifier = WarpModifier
            .fillMaxWidth()
            .background(colors.surfaceVariant)
            .cornerRadius(10)
            .padding(horizontal = 10, vertical = rowPaddingV)
            .clickable(CounterActions.ToggleTodo(todo).asClickAction()),
        verticalAlignment = WarpVerticalAlignment.Center,
    ) {
        WarpImage(
            asset = icon.asSystem(),
            contentDescription = if (todo.done) "Done" else "Todo",
            modifier = WarpModifier.size(iconSize),
            tint = if (todo.done) colors.primary else colors.onSurfaceVariant,
        )
        WarpSpacer(modifier = WarpModifier.width(8))
        WarpText(
            text = todo.title,
            modifier = WarpModifier.weight(),
            style = WarpTextStyle(
                color = titleColor,
                fontSize = titleSize,
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

    override suspend fun onAction(action: CounterActions) {
        updateWarpWidgetState(session, CounterWarpWidget) { raw ->
            val state = raw.withMergedSampleTodos()
            when (action) {
                CounterActions.Increment -> state.copy(count = state.count + 1)
                CounterActions.Decrement -> state.copy(count = state.count - 1)
                CounterActions.Reset -> state.copy(count = 0)
                is CounterActions.SwitchMode -> state.copy(mode = action.mode)
                is CounterActions.ToggleTodo -> state.copy(
                    todos = state.todos.map { todo ->
                        if (todo.id == action.todo.id) todo.copy(done = !todo.done) else todo
                    },
                )
            }
        }
    }
}
