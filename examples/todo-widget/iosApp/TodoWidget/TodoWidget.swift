//
//  TodoWidget.swift
//  TodoWidget
//

import SharedLogic
import SwiftUI
import WidgetKit
import warpWidgetKit

/// Timeline entry — state lives in App Group prefs; [TodoWidgetEntryView] composes live JSON.
struct TodoWidgetEntry: TimelineEntry {
    let date: Date
    let displayWidth: CGFloat
    let displayHeight: CGFloat
}

/// Refreshes timeline schedule only. UI JSON is built in [TodoWidgetEntryView] from
/// `@Environment(\.colorScheme)` so theme matches what WidgetKit is drawing (including
/// light/dark pre-render passes).
struct TodoWidgetProvider: TimelineProvider {
    func placeholder(in context: Context) -> TodoWidgetEntry {
        entry(from: context)
    }

    func getSnapshot(in context: Context, completion: @escaping (TodoWidgetEntry) -> Void) {
        completion(entry(from: context))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<TodoWidgetEntry>) -> Void) {
        completion(Timeline(entries: [entry(from: context)], policy: .atEnd))
    }

    private func entry(from context: Context) -> TodoWidgetEntry {
        let size = context.displaySize
        return TodoWidgetEntry(
            date: Date(),
            displayWidth: size.width,
            displayHeight: size.height
        )
    }
}

/// Composes WARP JSON at render time — [EnvironmentValues.colorScheme] is the source of truth.
struct TodoWidgetEntryView: View {
    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.widgetFamily) private var widgetFamily
    @Environment(\.widgetRenderingMode) private var widgetRenderingMode

    var entry: TodoWidgetProvider.Entry

    var body: some View {
        WarpSwiftUIRootView(
            json: composeWidgetJson(
                colorScheme: colorScheme,
                widgetFamily: widgetFamily,
                widgetRenderingMode: widgetRenderingMode,
                displaySize: CGSize(width: entry.displayWidth, height: entry.displayHeight)
            ),
            useIntents: true,
            widgetId: TodoWarpWidget.shared.id
        )
    }
}

/// WidgetKit host for shared [TodoWarpWidget].
///
/// Named `TodoHomeWidget` so it does not clash with Kotlin
/// `TodoWidget` exported from Shared (`warp-runtime` example).
/// [kind] stays `"TodoWidget"` (= [TodoWarpWidget.id]) for reloads.
struct TodoHomeWidget: Widget {
    let kind: String = "TodoWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: TodoWidgetProvider()) { entry in
            if #available(iOS 17.0, *) {
                TodoWidgetEntryView(entry: entry)
                    .containerBackground(.fill.tertiary, for: .widget)
            } else {
                TodoWidgetEntryView(entry: entry)
                    .padding()
            }
        }
        .contentMarginsDisabled()
        .configurationDisplayName("Todo")
        .description("WARP Todo widget")
        .supportedFamilies([.systemSmall,.systemMedium,.systemLarge])
    }
}

#Preview(as: .systemSmall) {
    TodoHomeWidget()
} timeline: {
    TodoWidgetEntry(date: .now, displayWidth: 155, displayHeight: 155)
}
