import SwiftUI
import AppIntents

// MARK: - AppIntent host hook

/// Extension-local `AppIntent` for WARP widget buttons.
///
/// WidgetKit only discovers intents compiled into the **widget extension** — not Shared.
/// Conform + call [WarpClickIntentRegistry.install(_:for:)]; `warpWidgetKit` owns button styling.
///
/// ```swift
/// struct MyClickIntent: WarpClickAppIntent { /* init(actionId:parametersJson:) + perform */ }
///
/// // WidgetBundle.init — one install per widget kind (`WarpWidget.id`):
/// WarpClickIntentRegistry.install(CounterClickIntent.self, for: CounterWarpWidget.shared.id)
/// WarpClickIntentRegistry.install(WeatherClickIntent.self, for: WeatherWarpWidget.shared.id)
/// ```
@available(iOS 17.0, *)
public protocol WarpClickAppIntent: AppIntent {
    init(actionId: String, parametersJson: String)
}

/// Per-widget-kind registry of [WarpClickAppIntent] factories.
///
/// Key = `WarpWidget.id` / WidgetKit `kind`. Multiple widgets in one extension each
/// call [install(_:for:)]; [WarpSwiftUIRootView] passes the same `widgetId` when rendering.
///
/// Renderer builds styled `Button(intent:)` — extensions supply **intent type only**.
@available(iOS 17.0, *)
public enum WarpClickIntentRegistry {
    private static var factories: [String: (String, String) -> any AppIntent] = [:]

    /// Register `I` for [widgetId] (`WarpWidget.id`). Safe to call for many widgets.
    public static func install<I: WarpClickAppIntent>(_ type: I.Type, for widgetId: String) {
        factories[widgetId] = { actionId, parametersJson in
            I(actionId: actionId, parametersJson: parametersJson)
        }
    }

    /// Remove factory for one widget kind.
    public static func uninstall(for widgetId: String) {
        factories.removeValue(forKey: widgetId)
    }

    /// Clears all factories (tests / host teardown).
    public static func uninstallAll() {
        factories.removeAll()
    }

    fileprivate static func intent(
        widgetId: String,
        actionId: String,
        parametersJson: String
    ) -> (any AppIntent)? {
        factories[widgetId]?(actionId, parametersJson)
    }
}

// MARK: - Shared button chrome (intent + bridge)

/// Single place for WARP button look — keep WidgetKit + in-app preview aligned.
@available(iOS 17.0, *)
private struct WarpButtonLabel: View {
    let title: String

    var body: some View {
        Text(title)
            .font(.title3.weight(.semibold))
            .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

@available(iOS 17.0, *)
private extension View {
    func warpButtonChrome() -> some View {
        self
            .buttonStyle(.bordered)
            .buttonBorderShape(.circle)
            .controlSize(.small)
    }
}

/// Opens `any AppIntent` into a generic `Button(intent:)` + [WarpButtonLabel] chrome.
@available(iOS 17.0, *)
private struct WarpIntentStyledButton: View {
    let intent: any AppIntent
    let label: String

    var body: some View {
        open(intent)
    }

    private func open<I: AppIntent>(_ intent: I) -> AnyView {
        AnyView(
            Button(intent: intent) {
                WarpButtonLabel(title: label)
            }
            .warpButtonChrome()
        )
    }
}

// MARK: - SwiftUI root

/// Pure SwiftUI tree for WidgetKit / previews from WARP JSON.
///
/// ### From Kotlin
/// 1. `renderXWidget(): WarpNode` + `registerWarpClicks`
/// 2. `warpWidgetJson(node)` → this view’s `json`
///
/// - `useIntents: true` — home-screen; needs [WarpClickIntentRegistry.install(_:for:)]
///   with the same [widgetId] (`WarpWidget.id`)
/// - `useIntents: false` — in-app preview via [WarpClickBridge]
public struct WarpSwiftUIRootView: View {
    let json: String
    let useIntents: Bool
    /// `WarpWidget.id` / WidgetKit `kind` — selects the installed click intent factory.
    let widgetId: String

    public init(json: String, useIntents: Bool, widgetId: String = "") {
        self.json = json
        self.useIntents = useIntents
        self.widgetId = widgetId
    }

    public var body: some View {
        if let root = WarpNodeParser.parse(json: json) {
            WarpNodeView(node: root, useIntents: useIntents, widgetId: widgetId)
                .padding()
        } else {
            Text("Invalid WARP node JSON")
                .font(.caption)
        }
    }
}

/// Recursive SwiftUI mapping of a parsed WARP node (column / row / text / button).
private struct WarpNodeView: View {
    let node: WarpParsedNode
    let useIntents: Bool
    let widgetId: String

    var body: some View {
        switch node.kind {
        case .column:
            VStack(alignment: .center, spacing: 6) {
                ForEach(Array(node.children.enumerated()), id: \.offset) { _, child in
                    WarpNodeView(node: child, useIntents: useIntents, widgetId: widgetId)
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .padding(node.padding)

        case .row:
            HStack(spacing: 6) {
                ForEach(Array(node.children.enumerated()), id: \.offset) { index, child in
                    if node.children.count == 3, index == 1, child.kind == .text {
                        WarpNodeView(node: child, useIntents: useIntents, widgetId: widgetId)
                            .frame(maxWidth: .infinity)
                            .layoutPriority(1)
                    } else if child.kind == .button {
                        WarpNodeView(node: child, useIntents: useIntents, widgetId: widgetId)
                            .frame(minWidth: 32, maxWidth: 40)
                    } else {
                        WarpNodeView(node: child, useIntents: useIntents, widgetId: widgetId)
                    }
                }
            }
            .frame(maxWidth: .infinity)
            .padding(node.padding)

        case .text:
            Text(node.text ?? "")
                .font(.title2.weight(.semibold))
                .monospacedDigit()
                .foregroundStyle(.primary)
                .lineLimit(1)
                .minimumScaleFactor(0.5)
                .fixedSize(horizontal: true, vertical: false)
                .padding(node.padding)

        case .button:
            buttonView
                .padding(node.padding)
        }
    }

    @ViewBuilder
    private var buttonView: some View {
        let label = node.text ?? ""
        if useIntents, #available(iOS 17.0, *), let actionId = node.actionId,
           !widgetId.isEmpty,
           let intent = WarpClickIntentRegistry.intent(
            widgetId: widgetId,
            actionId: actionId,
            parametersJson: node.parametersJson
           ) {
            WarpIntentStyledButton(intent: intent, label: label)
        } else if let actionId = node.actionId {
            // In-app preview / fallback — home-screen widgets need install(_:for:).
            if #available(iOS 17.0, *) {
                Button {
                    WarpClickBridge.shared.perform(
                        actionId: actionId,
                        parametersJson: node.parametersJson
                    )
                    WarpWidgetBridge.shared.reloadTimelines()
                } label: {
                    WarpButtonLabel(title: label)
                }
                .warpButtonChrome()
            } else {
                Button(label) {
                    WarpClickBridge.shared.perform(
                        actionId: actionId,
                        parametersJson: node.parametersJson
                    )
                    WarpWidgetBridge.shared.reloadTimelines()
                }
            }
        } else {
            Text(label)
        }
    }
}

// MARK: - JSON → model (mirrors warp-runtime node JSON)

enum WarpNodeKind {
    case column
    case row
    case text
    case button
}

/// Intermediate model after parsing Kotlin `WarpNode.toJson()` output.
struct WarpParsedNode {
    let kind: WarpNodeKind
    let text: String?
    /// WARP `onClick.actionId` — Kotlin [WarpClicksRegistry] key.
    let actionId: String?
    let parametersJson: String
    let padding: EdgeInsets
    let children: [WarpParsedNode]
}

/// Parses WARP node JSON produced by Kotlin `WarpNode.toJson()` / `warpWidgetJson`.
enum WarpNodeParser {
    static func parse(json: String) -> WarpParsedNode? {
        guard
            let data = json.data(using: .utf8),
            let object = try? JSONSerialization.jsonObject(with: data) as? [String: Any]
        else {
            return nil
        }
        return parseNode(object)
    }

    private static func parseNode(_ object: [String: Any]) -> WarpParsedNode? {
        guard let type = object["type"] as? String else { return nil }
        let padding = parsePadding(object["modifier"] as? [String: Any])
        let children = (object["children"] as? [[String: Any]] ?? [])
            .compactMap(parseNode)

        switch type {
        case "column":
            return WarpParsedNode(
                kind: .column,
                text: nil,
                actionId: nil,
                parametersJson: "{}",
                padding: padding,
                children: children
            )
        case "row":
            return WarpParsedNode(
                kind: .row,
                text: nil,
                actionId: nil,
                parametersJson: "{}",
                padding: padding,
                children: children
            )
        case "text":
            return WarpParsedNode(
                kind: .text,
                text: object["text"] as? String ?? "",
                actionId: nil,
                parametersJson: "{}",
                padding: padding,
                children: []
            )
        case "button":
            let click = object["onClick"] as? [String: Any]
            let actionId = click?["actionId"] as? String
            let parameters = click?["parameters"] as? [String: String] ?? [:]
            let parametersJson = jsonString(parameters) ?? "{}"
            return WarpParsedNode(
                kind: .button,
                text: object["text"] as? String ?? "",
                actionId: actionId,
                parametersJson: parametersJson,
                padding: padding,
                children: []
            )
        default:
            return nil
        }
    }

    private static func parsePadding(_ modifier: [String: Any]?) -> EdgeInsets {
        guard let padding = modifier?["padding"] as? [String: Any] else {
            return EdgeInsets()
        }
        return EdgeInsets(
            top: CGFloat(padding["top"] as? Int ?? 0),
            leading: CGFloat(padding["start"] as? Int ?? 0),
            bottom: CGFloat(padding["bottom"] as? Int ?? 0),
            trailing: CGFloat(padding["end"] as? Int ?? 0)
        )
    }

    private static func jsonString(_ dictionary: [String: String]) -> String? {
        guard JSONSerialization.isValidJSONObject(dictionary),
              let data = try? JSONSerialization.data(withJSONObject: dictionary)
        else {
            return nil
        }
        return String(data: data, encoding: .utf8)
    }
}
