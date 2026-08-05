import Shared
import SwiftUI
import WidgetKit

struct CounterEntry: TimelineEntry {
    let date: Date
    let document: WarpDocument
    let state: [String: String]
}

struct CounterTimelineProvider: TimelineProvider {
    func placeholder(in context: Context) -> CounterEntry {
        makeEntry(counter: "0")
    }

    func getSnapshot(in context: Context, completion: @escaping (CounterEntry) -> Void) {
        completion(makeEntry(counter: currentCounter()))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<CounterEntry>) -> Void) {
        let entry = makeEntry(counter: currentCounter())
        let timeline = Timeline(entries: [entry], policy: .never)
        completion(timeline)
    }

    private func currentCounter() -> String {
        CounterStore.readCounter()
    }

    private func makeEntry(counter: String) -> CounterEntry {
        let state = [AppGroupConfig.counterKey: counter]
        let json = WarpBridge.shared.buildCounterWidgetJson(stateValues: state)
        let document = (try? WarpIrDecoder.decode(json)) ?? fallbackDocument()
        return CounterEntry(date: .now, document: document, state: state)
    }

    private func fallbackDocument() -> WarpDocument {
        WarpDocument(
            schemaVersion: 1,
            widgetKind: AppGroupConfig.widgetKind,
            root: .text(
                WarpTextNode(
                    type: "text",
                    modifier: WarpModifier(padding: nil, background: nil, weight: nil),
                    text: "0",
                    stateKey: nil
                )
            )
        )
    }
}

struct CounterWidgetEntryView: View {
    var entry: CounterEntry

    var body: some View {
        WarpNodeView(node: entry.document.root, state: entry.state)
            .containerBackground(.fill.tertiary, for: .widget)
    }
}

struct CounterWidget: Widget {
    let kind: String = AppGroupConfig.widgetKind

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: CounterTimelineProvider()) { entry in
            CounterWidgetEntryView(entry: entry)
        }
        .configurationDisplayName("Counter")
        .description("WARP-powered counter widget")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}

@main
struct CounterWidgetBundle: WidgetBundle {
    var body: some Widget {
        CounterWidget()
    }
}
