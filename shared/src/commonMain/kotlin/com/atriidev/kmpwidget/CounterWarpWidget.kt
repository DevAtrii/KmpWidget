package com.atriidev.kmpwidget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.atriidev.warp_runtime.WarpExperimentalApi
import com.atriidev.warp_runtime.compose.WarpBox
import com.atriidev.warp_runtime.compose.WarpButton
import com.atriidev.warp_runtime.compose.WarpColumn
import com.atriidev.warp_runtime.compose.WarpDivider
import com.atriidev.warp_runtime.compose.WarpImage
import com.atriidev.warp_runtime.compose.WarpLazyRow
import com.atriidev.warp_runtime.compose.WarpProgressIndicator
import com.atriidev.warp_runtime.compose.WarpRow
import com.atriidev.warp_runtime.compose.WarpSpacer
import com.atriidev.warp_runtime.compose.WarpText
import com.atriidev.warp_runtime.log.WarpLogger
import com.atriidev.warp_runtime.log.WarpLoggerLevel
import com.atriidev.warp_runtime.nodes.actions.asClickAction
import com.atriidev.warp_runtime.nodes.assets.WarpAssetId
import com.atriidev.warp_runtime.nodes.modifiers.WarpColor
import com.atriidev.warp_runtime.nodes.modifiers.WarpModifier
import com.atriidev.warp_runtime.nodes.style.WarpButtonColors
import com.atriidev.warp_runtime.nodes.style.WarpContentAlignment
import com.atriidev.warp_runtime.nodes.style.WarpFontWeight
import com.atriidev.warp_runtime.nodes.style.WarpHorizontalAlignment
import com.atriidev.warp_runtime.nodes.style.WarpProgressIndicatorStyle
import com.atriidev.warp_runtime.nodes.style.WarpTextStyle
import com.atriidev.warp_runtime.nodes.style.WarpVerticalAlignment
import com.atriidev.warp_runtime.unit.dp
import com.atriidev.warp_runtime.unit.sp
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

/** Dummy mood item for testing WarpLazyRow. */
data class MoodItem(val emoji: String, val label: String)

val SampleMoods: List<MoodItem> = listOf(
    MoodItem("🚀", "Productive"),
    MoodItem("🎯", "Focused"),
    MoodItem("😊", "Happy"),
    MoodItem("🔥", "Energetic"),
    MoodItem("☕", "Relaxed"),
    MoodItem("😴", "Tired"),
    MoodItem("💡", "Creative"),
)

/** Sample list used as [CounterState] default. */
private val SampleTodos: List<TodoItem> = listOf(
    TodoItem(id = "1", title = "Ship WarpImage"),
    TodoItem(id = "2", title = "Add todo mode", done = true),
    TodoItem(id = "3", title = "Polish SF Symbols"),
    TodoItem(id = "4", title = "LazyColumn on large widget"),
    TodoItem(id = "5", title = "Wire WidgetKit reload"),
    TodoItem(id = "6", title = "Add adaptive sizing", done = true),
    TodoItem(id = "7", title = "Test Android resize"),
    TodoItem(id = "8", title = "Document WarpButton API"),
    TodoItem(id = "9", title = "Ship iOS medium layout"),
    TodoItem(id = "10", title = "Release v1.0"),
    TodoItem(id = "11", title = "Implement WarpRow"),
    TodoItem(id = "12", title = "Implement WarpColumn", done = true),
    TodoItem(id = "13", title = "Support dark theme"),
    TodoItem(id = "14", title = "Support light theme", done = true),
    TodoItem(id = "15", title = "Cache widget state"),
    TodoItem(id = "16", title = "Optimize JSON parser"),
    TodoItem(id = "17", title = "Support click actions"),
    TodoItem(id = "18", title = "Add loading placeholder"),
    TodoItem(id = "19", title = "Improve text wrapping"),
    TodoItem(id = "20", title = "Implement WarpSpacer", done = true),
    TodoItem(id = "21", title = "Support corner radius"),
    TodoItem(id = "22", title = "Add padding modifier"),
    TodoItem(id = "23", title = "Support background color"),
    TodoItem(id = "24", title = "Implement WarpDivider"),
    TodoItem(id = "25", title = "Support remote images"),
    TodoItem(id = "26", title = "Optimize renderer"),
    TodoItem(id = "27", title = "Write unit tests"),
    TodoItem(id = "28", title = "Write snapshot tests"),
    TodoItem(id = "29", title = "Add sample gallery"),
    TodoItem(id = "30", title = "Publish documentation"),
)

/** Serializable state for [CounterWarpWidget] — persisted as JSON under prefs key = widget id. */
@Serializable
@Stable
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
 * Counter + todo [WarpWidget] — one definition for Glance + WidgetKit.
 *
 * Tap mode chips to switch. In todo mode, tap a row to mark done / undone.
 */
object CounterWarpWidget : WarpWidget<CounterState>(CounterState.serializer()) {
    override val id: String = "CounterWidget"

    override val iosGroupId: String = APP_GROUP_ID

    override val stateScope: WarpWidgetStateScope = WarpWidgetStateScope.Shared

    override suspend fun defaultState(): CounterState = CounterState()

    @Composable
    override fun Content(env: WidgetEnvironment, state: CounterState) {
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
    val isLarge = spacious || env.adaptiveSize() == WarpAdaptiveSize.Large
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

            if (isLarge) {
                WarpSpacer(modifier = WarpModifier.height(6))
                MoodsSection()
            }

            WarpSpacer(modifier = WarpModifier.height(if (compact) 4 else 6))

            WarpDivider(
                modifier = WarpModifier.fillMaxWidth(),
                thickness = 0.5.dp,
                color = colors.outline,
            )

            WarpSpacer(
                modifier = WarpModifier.height(
                    when {
                        spacious -> 10
                        state.mode == WidgetMode.Todo && isMedium -> 4
                        compact -> 4
                        else -> 8
                    },
                ),
            )

            when (state.mode) {
                WidgetMode.Counter -> CounterBody(state, env, spacious = spacious)
                WidgetMode.Todo -> TodoBody(state, env, compact = todoCompact, isLarge = isLarge)
            }
        }
    }
}

@OptIn(WarpExperimentalApi::class)
@Composable
private fun MoodsSection() {
    val colors = WarpTheme.colors
    WarpColumn(modifier = WarpModifier.fillMaxWidth()) {
        WarpText(
            text = "Mood",
            style = WarpTextStyle(
                color = colors.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = WarpFontWeight.Medium,
            ),
            maxLines = 1,
        )
        WarpSpacer(modifier = WarpModifier.height(4))
        WarpLazyRow(
            modifier = WarpModifier
                .fillMaxWidth()
        ) {
            SampleMoods.forEachIndexed { index, mood ->
                if (index > 0) {
                    WarpSpacer(modifier = WarpModifier.width(6))
                }
                MoodChip(mood)
            }
        }
    }
}

@Composable
private fun MoodChip(mood: MoodItem) {
    val colors = WarpTheme.colors
    WarpRow(
        modifier = WarpModifier
            .background(colors.surfaceVariant)
            .cornerRadius(12.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = WarpVerticalAlignment.Center,
    ) {
        WarpText(
            text = "${mood.emoji} ${mood.label}",
            style = WarpTextStyle(
                color = colors.onSurface,
                fontSize = 11.sp,
                fontWeight = WarpFontWeight.Medium,
            ),
            maxLines = 1,
        )
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
            fontSize = if (compact) 10.sp else 11.sp,
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
    val chipPaddingH = if (compact) 8.dp else 10.dp
    val chipPaddingV = if (compact) 4.dp else 6.dp
    val iconSize = if (compact) 12.dp else 14.dp
    val fontSize = if (compact) 11.sp else 12.sp
    WarpRow(
        modifier = WarpModifier
            .background(bg)
            .cornerRadius(20.dp)
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
        WarpSpacer(modifier = WarpModifier.width(4.dp))
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
    val buttonSize = env.adaptiveValue(small = 36.dp, medium = 40.dp, large = 48.dp)
    val countFontSize = env.adaptiveValue(small = 22.sp, medium = 26.sp, large = 32.sp)
    val cardPadding = if (spacious) 12.dp else 8.dp
    // Nested column — Glance allows max 10 children per Column/Row.
    WarpColumn(modifier = WarpModifier.fillMaxWidth()) {
        val count = state.count
        val progress = (abs(count) % 100) / 100f

        WarpRow(
            modifier = WarpModifier
                .fillMaxWidth()
                .background(colors.surfaceVariant)
                .cornerRadius(12.dp)
                .padding(cardPadding),
            verticalAlignment = WarpVerticalAlignment.Center,
        ) {
            WarpButton(
                text = "−",
                onClick = CounterActions.Decrement.asClickAction(),
                modifier = WarpModifier
                    .size(buttonSize)
                    .cornerRadius(buttonSize.value / 2),
                style = WarpTextStyle(
                    fontSize = if (spacious) 20.sp else 18.sp,
                    fontWeight = WarpFontWeight.Bold,
                ),
                colors = WarpButtonColors (
                    backgroundColor = WarpColor.Red600,
                    contentColor= WarpColor.White
                )
            )
            WarpText(
                text = count.toString(),
                modifier = WarpModifier
                    .weight()
                    .padding(horizontal = 8.dp, vertical = 0.dp)
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
                    .cornerRadius(buttonSize.value / 2),
                style = WarpTextStyle(
                    fontSize = if (spacious) 20.sp else 18.sp,
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
    isLarge: Boolean = false,
) {
    val colors = WarpTheme.colors
    val doneCount = state.todos.count { it.done }
    val total = state.todos.size
    val progress = if (total == 0) 0f else doneCount.toFloat() / total
    val maxVisible = env.adaptiveValue(small = 2, medium = 2, large = 10)
    val visibleTodos = if (isLarge) state.todos else state.todos.take(maxVisible)

    WarpColumn(modifier = WarpModifier.fillMaxWidth()) {
        if (visibleTodos.isNotEmpty())
            WarpText(
                text = "$doneCount / $total done",
                style = WarpTextStyle(
                    color = colors.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = WarpFontWeight.Medium,
                ),
                maxLines = 1,
            )

        if (visibleTodos.isEmpty()) {
            EmptyTodoBody()
        } else {
            WarpSpacer(modifier = WarpModifier.height(if (compact) 4 else 6))

            if (isLarge) {
                WarpColumn(modifier = WarpModifier.fillMaxWidth()) {
                    visibleTodos.forEachIndexed { index, todo ->
                        if (index > 0) {
                            WarpSpacer(modifier = WarpModifier.height(4))
                        }
                        TodoRow(todo, compact = false)
                    }
                }
            } else {
                WarpColumn(modifier = WarpModifier.fillMaxWidth()) {
                    visibleTodos.forEachIndexed { index, todo ->
                        if (index > 0) {
                            WarpSpacer(modifier = WarpModifier.height(if (compact) 3 else 4))
                        }
                        TodoRow(todo, compact = compact)
                    }
                }
            }

            if (!isLarge && visibleTodos.size < total) {
                WarpSpacer(modifier = WarpModifier.height(4.dp))
                WarpText(
                    text = "+${total - visibleTodos.size} more",
                    style = WarpTextStyle(
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp,
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
}


@Composable
private fun EmptyTodoBody() {
    val colors = WarpTheme.colors

    WarpBox(
        modifier = WarpModifier.fillMaxSize(),
        contentAlignment = WarpContentAlignment.Center,
    ) {
        WarpColumn(
            modifier = WarpModifier.fillMaxWidth(),
            horizontalAlignment = WarpHorizontalAlignment.Center,
        ) {
            WarpImage(
                asset = CounterAssets.Circle.asSystem(),
                contentDescription = "No todos",
                modifier = WarpModifier.size(36),
                tint = colors.primary,
            )

            WarpSpacer(modifier = WarpModifier.height(12))

            WarpText(
                text = "No todos",
                style = WarpTextStyle(
                    fontSize = 16.sp,
                    fontWeight = WarpFontWeight.Semibold,
                    color = colors.onSurface,
                ),
            )

            WarpSpacer(modifier = WarpModifier.height(4.dp))

            WarpText(
                text = "Tap + to add a sample task",
                style = WarpTextStyle(
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariant,
                ),
                maxLines = 2,
            )
        }
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
    val iconSize = if (compact) 18.dp else 20.dp
    val titleSize = if (compact) 13.sp else 14.sp
    val rowPaddingV = if (compact) 6.dp else 8.dp
    WarpRow(
        modifier = WarpModifier
            .fillMaxWidth()
            .background(colors.surfaceVariant)
            .cornerRadius(10.dp)
            .padding(horizontal = 10.dp, vertical = rowPaddingV)
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
        updateWarpWidgetState(session, CounterWarpWidget) { state ->
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
