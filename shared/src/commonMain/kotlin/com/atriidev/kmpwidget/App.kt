package com.atriidev.kmpwidget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.atriidev.warp_widget.api.PlatformContext
import com.atriidev.warp_widget.readWarpWidgetState
import com.atriidev.warp_widget.updateWarpWidgetState
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun App(
    platformContext: PlatformContext,
) {
    MaterialTheme {
        var count by remember { mutableIntStateOf(0) }
        val scope = rememberCoroutineScope()

        fun refreshCount() {
            count = runBlocking {
                readWarpWidgetState(platformContext, CounterWarpWidget).count
            }
        }

        LifecycleResumeEffect(Unit) {
            refreshCount()
            onPauseOrDispose { }
        }

        suspend fun persist(next: Int) {
            updateWarpWidgetState(platformContext, CounterWarpWidget) {
                it.copy(count = next)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                ) {
                    FilledTonalButton(
                        onClick = {
                            val next = count - 1
                            count = next
                            scope.launch { persist(next) }
                        },
                    ) {
                        Text(
                            text = "−",
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }

                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.displayLarge,
                    )

                    FilledTonalButton(
                        onClick = {
                            val next = count + 1
                            count = next
                            scope.launch { persist(next) }
                        },
                    ) {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }
                }

                Button(
                    onClick = {
                        scope.launch { persist(count) }
                    },
                ) {
                    Text("Update Widget")
                }
            }
        }
    }
}
