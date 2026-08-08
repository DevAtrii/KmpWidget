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
import com.atriidev.warp_runtime.nodes.assets.WarpAssetId
import com.atriidev.warp_runtime.nodes.modifiers.WarpModifier
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
import com.atriidev.warp_widget.ui.WarpTheme
import com.atriidev.warp_widget.ui.adaptiveValue
import com.atriidev.warp_widget.ui.isMediumAdaptive
import com.atriidev.warp_widget.updateWarpWidgetState
import kotlinx.serialization.Serializable


/** Type-safe asset keys — share with GlanceWidgets Assets. */
object TodoAssets {
    val Circle = WarpAssetId("circle")
    val CheckCircle = WarpAssetId("checkmark.circle.fill")
}

@Serializable
sealed interface TodoActions {

    @Serializable
    data class Toggle(val todoItem: TodoItem) : TodoActions

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
    override suspend fun onClick(action: TodoActions) {
        println("click_handling... $action")
        when (action) {
            is TodoActions.Toggle -> updateWarpWidgetState(session, TodoWarpWidget) {
                it.copy(
                    todos = it.todos.map { todo ->
                        if (todo == action.todoItem)
                            todo.copy(done = true)
                        else todo
                    }
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
            WarpText(
                text = "Your Todos",
                style = WarpTextStyle(
                    fontSize = 16f
                )
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
            .clickable(TodoActions.Toggle(todo)),
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










