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
import kotlinx.coroutines.launch


@Composable
fun App(
    dataStore: KmpDataStore,
    widgetUpdater: WidgetUpdater,
) {

    MaterialTheme {
        var count by remember { mutableIntStateOf(0) }

        LifecycleResumeEffect(Unit) {
            count = dataStore.get(COUNTER_KEY, "0").toIntOrNull() ?: 0

            onPauseOrDispose { }
        }
        val scope = rememberCoroutineScope()



        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {

                    FilledTonalButton(
                        onClick = {
                            count--
                            dataStore.set(COUNTER_KEY, count.toString())
                        }
                    ) {
                        Text(
                            text = "−",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }

                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.displayLarge
                    )

                    FilledTonalButton(
                        onClick = {
                            count++
                            dataStore.set(COUNTER_KEY, count.toString())
                        }
                    ) {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            widgetUpdater.update(count)
                        }
                    }
                ){
                    Text("Update Widget")
                }
            }
        }
    }
}