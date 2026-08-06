import WidgetKit
import SwiftUI

public struct WarpWidgetEntry: TimelineEntry {
    public let date: Date
    public let nodeJson: String

    public init(date: Date, nodeJson: String) {
        self.date = date
        self.nodeJson = nodeJson
    }
}

public struct WarpWidgetProvider: TimelineProvider {
    public init() {}

    public func placeholder(in context: Context) -> WarpWidgetEntry {
        WarpWidgetEntry(date: Date(), nodeJson: "{}")
    }

    public func getSnapshot(in context: Context, completion: @escaping (WarpWidgetEntry) -> Void) {
        completion(
            WarpWidgetEntry(
                date: Date(),
                nodeJson: WarpWidgetBridge.shared.storedNodeJson()
            )
        )
    }

    public func getTimeline(in context: Context, completion: @escaping (Timeline<WarpWidgetEntry>) -> Void) {
        let entry = WarpWidgetEntry(
            date: Date(),
            nodeJson: WarpWidgetBridge.shared.storedNodeJson()
        )
        completion(Timeline(entries: [entry], policy: .atEnd))
    }
}

/// WidgetKit entry point — add to your widget extension `@main` bundle.
public struct WarpWidgetKitWidget: Widget {
    public let kind: String = "WarpWidget"

    public init() {}

    public var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: WarpWidgetProvider()) { entry in
            if #available(iOS 17.0, *) {
                WarpSwiftUIRootView(json: entry.nodeJson, useIntents: true)
                    .containerBackground(.fill.tertiary, for: .widget)
            } else {
                WarpSwiftUIRootView(json: entry.nodeJson, useIntents: false)
                    .padding()
            }
        }
        .configurationDisplayName("WARP Widget")
        .description("Renders a WARP WarpNode tree from shared JSON.")
        .supportedFamilies([.systemSmall, .systemMedium, .systemLarge])
    }
}
