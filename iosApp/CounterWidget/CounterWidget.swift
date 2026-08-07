//
//  CounterWidget.swift
//  CounterWidget
//

import Shared
import SwiftUI
import WidgetKit
import warpWidgetKit

/// Timeline entry carries WARP JSON so reloads show fresh Kotlin state (not a cached view).
struct CounterWidgetEntry: TimelineEntry {
    let date: Date
    /// From [WarpWidgetHost.composeJson] — [CounterWarpWidget] + [WarpWidgetKitEnv].
    let json: String
}

/// Builds entries by calling into Shared (Kotlin) on each snapshot / timeline refresh.
struct CounterWidgetProvider: TimelineProvider {
    func placeholder(in context: Context) -> CounterWidgetEntry {
        CounterWidgetEntry(date: Date(), json: counterWidgetJson(context: context))
    }

    func getSnapshot(in context: Context, completion: @escaping (CounterWidgetEntry) -> Void) {
        completion(CounterWidgetEntry(date: Date(), json: counterWidgetJson(context: context)))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<CounterWidgetEntry>) -> Void) {
        let entry = CounterWidgetEntry(date: Date(), json: counterWidgetJson(context: context))
        completion(Timeline(entries: [entry], policy: .atEnd))
    }
}

/// Renders [CounterWidgetEntry.json] via warp-ui SwiftUI (`WarpSwiftUIRootView`).
struct CounterWidgetEntryView: View {
    var entry: CounterWidgetProvider.Entry

    var body: some View {
        WarpSwiftUIRootView(
            json: entry.json,
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
        .supportedFamilies([.systemSmall,.systemMedium])
    }
}

#Preview(as: .systemSmall) {
    CounterHomeWidget()
} timeline: {
    CounterWidgetEntry(date: .now, json: counterWidgetPlaceholderJson())
}
