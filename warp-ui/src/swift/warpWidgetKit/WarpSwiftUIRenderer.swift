import SwiftUI
import AppIntents

// MARK: - AppIntent host hook

/// Widget **extension** registers a button builder so `AppIntent` types live in the
/// extension binary (WidgetKit does not discover intents that exist only inside Shared).
///
/// ### Kotlin / Shared
/// Renderer calls this when `useIntents == true`. Kotlin still owns click *logic*
/// via `dispatchCounterWidgetClick` → `WarpClicksRegistry`.
///
/// ### Extension setup (`WidgetBundle.init`)
/// ```swift
/// WarpClickIntentRegistry.buttonBuilder = { actionId, parametersJson, label in
///     AnyView(Button(intent: MyClickIntent(...)) { Text(label) } /* styles */)
/// }
/// ```
@available(iOS 17.0, *)
public enum WarpClickIntentRegistry {
    /// `(actionId, parametersJson, label) → styled button view`
    public static var buttonBuilder: ((String, String, String) -> AnyView)?
}

// MARK: - SwiftUI root

/// Pure SwiftUI tree for WidgetKit / previews from WARP JSON.
///
/// ### From Kotlin
/// 1. `renderXWidget(): WarpNode` + `registerWarpClicks`
/// 2. `warpWidgetJson(node)` → this view’s `json`
///
/// - `useIntents: true` — home-screen widget (needs [WarpClickIntentRegistry])
/// - `useIntents: false` — in-app preview via [WarpClickBridge]
public struct WarpSwiftUIRootView: View {
    let json: String
    let useIntents: Bool

    public init(json: String, useIntents: Bool) {
        self.json = json
        self.useIntents = useIntents
    }

    public var body: some View {
        if let root = WarpNodeParser.parse(json: json) {
            WarpNodeView(node: root, useIntents: useIntents)
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

    var body: some View {
        switch node.kind {
        case .column:
            VStack(alignment: .center, spacing: 6) {
                ForEach(Array(node.children.enumerated()), id: \.offset) { _, child in
                    WarpNodeView(node: child, useIntents: useIntents)
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .padding(node.padding)

        case .row:
            HStack(spacing: 6) {
                ForEach(Array(node.children.enumerated()), id: \.offset) { index, child in
                    if node.children.count == 3, index == 1, child.kind == .text {
                        WarpNodeView(node: child, useIntents: useIntents)
                            .frame(maxWidth: .infinity)
                            .layoutPriority(1)
                    } else if child.kind == .button {
                        WarpNodeView(node: child, useIntents: useIntents)
                            .frame(minWidth: 32, maxWidth: 40)
                    } else {
                        WarpNodeView(node: child, useIntents: useIntents)
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
            if useIntents, #available(iOS 17.0, *), let actionId = node.actionId,
               let builder = WarpClickIntentRegistry.buttonBuilder {
                builder(actionId, node.parametersJson, node.text ?? "")
                    .padding(node.padding)
            } else if let actionId = node.actionId {
                // In-app preview / fallback — home-screen widgets need registry + AppIntent.
                Button(node.text ?? "") {
                    WarpClickBridge.shared.perform(
                        actionId: actionId,
                        parametersJson: node.parametersJson
                    )
                    WarpWidgetBridge.shared.reloadTimelines()
                }
                .font(.title3.weight(.semibold))
                .buttonStyle(.bordered)
                .buttonBorderShape(.circle)
                .controlSize(.small)
                .padding(node.padding)
            } else {
                Text(node.text ?? "")
                    .padding(node.padding)
            }
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
