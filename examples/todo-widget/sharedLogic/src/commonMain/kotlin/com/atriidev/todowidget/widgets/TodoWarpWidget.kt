package com.atriidev.todowidget.widgets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.atriidev.warp_runtime.compose.WarpBox
import com.atriidev.warp_runtime.compose.WarpColumn
import com.atriidev.warp_runtime.compose.WarpDivider
import com.atriidev.warp_runtime.compose.WarpImage
import com.atriidev.warp_runtime.compose.WarpProgressIndicator
import com.atriidev.warp_runtime.compose.WarpRow
import com.atriidev.warp_runtime.compose.WarpSpacer
import com.atriidev.warp_runtime.compose.WarpText
import com.atriidev.warp_runtime.log.WarpLogger
import com.atriidev.warp_runtime.log.WarpLoggerLevel
import com.atriidev.warp_runtime.nodes.assets.WarpAsset
import com.atriidev.warp_runtime.nodes.assets.WarpAssetId
import com.atriidev.warp_runtime.nodes.modifiers.WarpModifier
import com.atriidev.warp_runtime.nodes.style.WarpContentAlignment
import com.atriidev.warp_runtime.nodes.style.WarpFontWeight
import com.atriidev.warp_runtime.nodes.style.WarpHorizontalAlignment
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
import com.atriidev.warp_widget.ui.WarpTheme
import com.atriidev.warp_widget.ui.adaptiveValue
import com.atriidev.warp_widget.ui.isMediumAdaptive
import com.atriidev.warp_widget.updateWarpWidgetState
import kotlinx.serialization.Serializable


/** Type-safe asset keys — share with GlanceWidgets Assets. */
object TodoAssets {
    val Plus = WarpAssetId("plus")
    val Trash = WarpAssetId("trash")

    val Circle = WarpAssetId("circle")
    val CheckCircle = WarpAssetId("checkmark.circle.fill")
}

@Serializable
sealed interface TodoActions {
    @Serializable
    data class Toggle(val todoId: Int) : TodoActions

    @Serializable
    data object Clear : TodoActions

    @Serializable
    data object AddSample : TodoActions
}

val sampleTodoWidgetState = TodoWidgetState(
    todos = listOf(
        TodoItem(
            id = 1,
            title = "Review pull request",
            done = true,
        ),
        TodoItem(
            id = 2,
            title = "Write widget documentation",
            done = false,
        ),
        TodoItem(
            id = 3,
            title = "Buy groceries",
            done = false,
        ),
        TodoItem(
            id = 4,
            title = "Go for a 30 min walk",
            done = true,
        ),
        TodoItem(
            id = 5,
            title = "Plan weekend trip",
            done = false,
        ),
    ),
)

@Serializable
@Stable
data class TodoWidgetState(
    val todos: List<TodoItem>,
)

@Serializable
data class TodoItem(
    val id: Int,
    val title: String,
    val done: Boolean,
)

const val APPLE_GROUP_ID = "group.warpexample.todowidget"

object TodoWarpWidget :
    WarpWidget<TodoWidgetState>(stateSerializer = TodoWidgetState.serializer()) {
    override val id: String
        get() = "TodoWidget"

    override val iosGroupId: String
        get() = APPLE_GROUP_ID

    override val defaultState: TodoWidgetState
        get() = sampleTodoWidgetState

    override val stateScope: WarpWidgetStateScope
        get() = WarpWidgetStateScope.Shared

    @Composable
    override fun Content(
        env: WidgetEnvironment,
        state: TodoWidgetState,
    ) {
        WarpLogger.level = WarpLoggerLevel.Debug
        WarpTheme(
            environment = env,
            darkTheme = false.takeIf { env.platform.isAndroid }
        ) {
            WarpAdaptiveContent(
                environment = env,
                small = { TodoWidgetContent(state, env, compact = true) },
                medium = { TodoWidgetContent(state, env) },
                large = { TodoWidgetContent(state, env, spacious = true) },
            )
        }
    }


    override fun clickHandlers(session: WarpWidgetSession): List<WarpClickHandler<*>> = listOf(
        TodoClickHandler(session)
    )
}


private class TodoClickHandler(
    private val session: WarpWidgetSession,
) : WarpClickHandler<TodoActions>(serializer = TodoActions.serializer()) {
    override suspend fun onAction(action: TodoActions) {
        when (action) {
            is TodoActions.Toggle -> updateWarpWidgetState(session, TodoWarpWidget) { state ->
                state.copy(
                    todos = state.todos.map { todo ->
                        if (todo.id == action.todoId)
                            todo.copy(done = !todo.done)
                        else todo
                    }
                )
            }

            is TodoActions.AddSample -> updateWarpWidgetState(session, TodoWarpWidget) { state ->
                sampleTodoWidgetState
            }

            is TodoActions.Clear -> updateWarpWidgetState(session, TodoWarpWidget) { state ->
                state.copy(
                    todos = emptyList()
                )
            }
        }
    }
}


// TO-DO UI

@Composable
private fun TodoWidgetContent(
    state: TodoWidgetState,
    env: WidgetEnvironment,
    compact: Boolean = false,
    spacious: Boolean = false,
) {
    val colors = WarpTheme.colors
    val isMedium = env.isMediumAdaptive()
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
            WarpRow(
                modifier = WarpModifier
                    .fillMaxWidth()
            ) {
                WarpText(
                    text = "Your Todos",
                    modifier = WarpModifier.weight(1f),
                    style = WarpTextStyle(
                        fontSize = when {
                            spacious -> 18f
                            isMedium -> 14f
                            else -> 10f
                        }
                    )
                )

                // reset & sample
                IconButton(
                    asset = TodoAssets.Trash.asSystem(),
                    action = TodoActions.Clear,
                    compact = compact
                )
                WarpSpacer(modifier = WarpModifier.width(if (compact) 4 else 6))

                IconButton(
                    asset = TodoAssets.Plus.asSystem(),
                    action = TodoActions.AddSample,
                    compact = compact
                )


            }
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
                        isMedium -> 4
                        compact -> 4
                        else -> 8
                    },
                ),
            )

            TodoBody(
                state = state,
                env = env,
                compact = compact
            )

        }
    }
}


@Composable
private fun IconButton(
    asset: WarpAsset,
    action: TodoActions,
    compact: Boolean,
) {
    val colors = WarpTheme.colors
    val bg = colors.surfaceVariant
    val fg = colors.primary
    val chipPaddingH = if (compact) 8 else 10
    val chipPaddingV = if (compact) 4 else 6
    val iconSize = if (compact) 12 else 14
    WarpRow(
        modifier = WarpModifier
            .background(bg)
            .cornerRadius(20)
            .padding(horizontal = chipPaddingH, vertical = chipPaddingV)
            .clickable(action),
        verticalAlignment = WarpVerticalAlignment.Center,
    ) {
        WarpImage(
            asset = asset,
            contentDescription = "Icon Button",
            modifier = WarpModifier.size(iconSize),
            tint = fg,
        )
    }
}

@Composable
private fun TodoBody(
    state: TodoWidgetState,
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
        if (visibleTodos.isNotEmpty())
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

        if (visibleTodos.isEmpty()) {
            EmptyTodoBody()
        } else {

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
                asset = TodoAssets.Circle.asSystem(),
                contentDescription = "No todos",
                modifier = WarpModifier.size(36),
                tint = colors.primary,
            )

            WarpSpacer(modifier = WarpModifier.height(12))

            WarpText(
                text = "No todos",
                style = WarpTextStyle(
                    fontSize = 16f,
                    fontWeight = WarpFontWeight.Semibold,
                    color = colors.onSurface,
                ),
            )

            WarpSpacer(modifier = WarpModifier.height(4))

            WarpText(
                text = "Tap + to add a sample task",
                style = WarpTextStyle(
                    fontSize = 12f,
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
    val icon = if (todo.done) TodoAssets.CheckCircle else TodoAssets.Circle
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
            .clickable(TodoActions.Toggle(todo.id)),
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










