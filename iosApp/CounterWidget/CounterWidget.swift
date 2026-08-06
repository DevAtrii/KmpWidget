//
//  CounterWidget.swift
//  CounterWidget
//

import Shared
import SwiftUI
import WidgetKit

struct CounterWidgetEntry: TimelineEntry {
    let date: Date
    let nodeJson: String
}

struct CounterWidgetProvider: TimelineProvider {
    func placeholder(in context: Context) -> CounterWidgetEntry {
        CounterWidgetEntry(date: Date(), nodeJson: "{}")
    }

    func getSnapshot(in context: Context, completion: @escaping (CounterWidgetEntry) -> Void) {
        completion(
            CounterWidgetEntry(
                date: Date(),
                nodeJson: CounterWidgetIosKt.renderCounterWidget()
            )
        )
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<CounterWidgetEntry>) -> Void) {
        let entry = CounterWidgetEntry(
            date: Date(),
            nodeJson: CounterWidgetIosKt.renderCounterWidget()
        )
        completion(Timeline(entries: [entry], policy: .atEnd))
    }
}

struct CounterWidgetEntryView: View {
    var entry: CounterWidgetProvider.Entry

    var body: some View {
        CounterWarpRenderView(json: entry.nodeJson)
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
    CounterWidgetEntry(date: .now, nodeJson: "{}")
}
