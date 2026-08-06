//
//  CounterWidget.swift
//  CounterWidget
//

import Shared
import SwiftUI
import WidgetKit

/// Timeline entry carries WARP JSON so reloads show fresh Kotlin state (not a cached view).
struct CounterWidgetEntry: TimelineEntry {
    let date: Date
    /// From [counterWidgetJson] — Kotlin `WarpNode.toJson()` after `registerWarpClicks`.
    let json: String
}

/// Builds entries by calling into Shared (Kotlin) on each snapshot / timeline refresh.
struct CounterWidgetProvider: TimelineProvider {
    func placeholder(in context: Context) -> CounterWidgetEntry {
        CounterWidgetEntry(date: Date(), json: counterWidgetJson())
    }

    func getSnapshot(in context: Context, completion: @escaping (CounterWidgetEntry) -> Void) {
        completion(CounterWidgetEntry(date: Date(), json: counterWidgetJson()))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<CounterWidgetEntry>) -> Void) {
        let entry = CounterWidgetEntry(date: Date(), json: counterWidgetJson())
        completion(Timeline(entries: [entry], policy: .atEnd))
    }
}

/// Renders [CounterWidgetEntry.json] via warp-ui SwiftUI (`WarpSwiftUIRootView`).
struct CounterWidgetEntryView: View {
    var entry: CounterWidgetProvider.Entry

    var body: some View {
        counterWidgetRootView(json: entry.json)
    }
}

struct CounterWidget: Widget {
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
        .configurationDisplayName("Counter")
        .description("WARP counter widget")
        .supportedFamilies([.systemSmall])
    }
}

#Preview(as: .systemSmall) {
    CounterWidget()
} timeline: {
    CounterWidgetEntry(date: .now, json: counterWidgetJson())
}
