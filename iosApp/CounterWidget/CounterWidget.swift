//
//  CounterWidget.swift
//  CounterWidget
//

import Shared
import SwiftUI
import WidgetKit
import warpWidgetKit

/// Timeline entry — state lives in App Group prefs; [CounterWidgetEntryView] composes live JSON.
struct CounterWidgetEntry: TimelineEntry {
    let date: Date
    let displayWidth: CGFloat
    let displayHeight: CGFloat
}

/// Refreshes timeline schedule only. UI JSON is built in [CounterWidgetEntryView] from
/// `@Environment(\.colorScheme)` so theme matches what WidgetKit is drawing (including
/// light/dark pre-render passes).
struct CounterWidgetProvider: TimelineProvider {
    func placeholder(in context: Context) -> CounterWidgetEntry {
        entry(from: context)
    }

    func getSnapshot(in context: Context, completion: @escaping (CounterWidgetEntry) -> Void) {
        completion(entry(from: context))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<CounterWidgetEntry>) -> Void) {
        completion(Timeline(entries: [entry(from: context)], policy: .atEnd))
    }

    private func entry(from context: Context) -> CounterWidgetEntry {
        let size = context.displaySize
        return CounterWidgetEntry(
            date: Date(),
            displayWidth: size.width,
            displayHeight: size.height
        )
    }
}

/// Composes WARP JSON at render time — [EnvironmentValues.colorScheme] is the source of truth.
struct CounterWidgetEntryView: View {
    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.widgetFamily) private var widgetFamily
    @Environment(\.widgetRenderingMode) private var widgetRenderingMode

    var entry: CounterWidgetProvider.Entry

    var body: some View {
        WarpSwiftUIRootView(
            json: composeWidgetJson(
                colorScheme: colorScheme,
                widgetFamily: widgetFamily,
                widgetRenderingMode: widgetRenderingMode,
                displaySize: CGSize(width: entry.displayWidth, height: entry.displayHeight)
            ),
            useIntents: true,
            widgetId: CounterWarpWidget.shared.id
        )
    }
}

/// WidgetKit host for shared [CounterWarpWidget].
///
/// Named `CounterHomeWidget` so it does not clash with Kotlin
/// `CounterWidget` exported from Shared (`warp-runtime` example).
/// [kind] stays `"CounterWidget"` (= [CounterWarpWidget.id]) for reloads.
struct CounterHomeWidget: Widget {
    let kind: String = "CounterWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: CounterWidgetProvider()) { entry in
            if #available(iOS 17.0, *) {
                CounterWidgetEntryView(entry: entry)
                    .containerBackground(.fill.tertiary, for: .widget)
            } else {
                CounterWidgetEntryView(entry: entry)
                    .padding()
            }
        }
        .contentMarginsDisabled()
        .configurationDisplayName("Counter")
        .description("WARP counter widget")
        .supportedFamilies([.systemSmall,.systemMedium,.systemLarge])
    }
}

#Preview(as: .systemSmall) {
    CounterHomeWidget()
} timeline: {
    CounterWidgetEntry(date: .now, displayWidth: 155, displayHeight: 155)
}
