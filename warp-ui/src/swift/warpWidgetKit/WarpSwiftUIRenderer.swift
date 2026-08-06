import SwiftUI
import AppIntents

struct WarpSwiftUIRootView: View {
    let json: String
    let useIntents: Bool

    var body: some View {
        if let root = WarpNodeParser.parse(json: json) {
            WarpNodeView(node: root, useIntents: useIntents)
                .padding()
        } else {
            Text("Invalid WARP node JSON")
                .font(.caption)
        }
    }
}

private struct WarpNodeView: View {
    let node: WarpParsedNode
    let useIntents: Bool

    var body: some View {
        switch node.kind {
        case .column:
            VStack(alignment: .leading, spacing: 8) {
                ForEach(Array(node.children.enumerated()), id: \.offset) { _, child in
                    WarpNodeView(node: child, useIntents: useIntents)
                }
            }
            .padding(node.padding)

        case .row:
            HStack(spacing: 8) {
                ForEach(Array(node.children.enumerated()), id: \.offset) { index, child in
                    if node.children.count == 3, index == 1, child.kind == .text {
                        WarpNodeView(node: child, useIntents: useIntents)
                            .frame(maxWidth: .infinity)
                    } else {
                        WarpNodeView(node: child, useIntents: useIntents)
                    }
                }
            }
            .padding(node.padding)

        case .text:
            Text(node.text ?? "")
                .padding(node.padding)

        case .button:
            if useIntents, #available(iOS 17.0, *), let actionId = node.actionId {
                Button(intent: WarpClickIntent(actionId: actionId, parametersJson: node.parametersJson)) {
                    Text(node.text ?? "")
                }
                .buttonStyle(.bordered)
                .padding(node.padding)
            } else if let actionId = node.actionId {
                Button(node.text ?? "") {
                    WarpClickBridge.shared.perform(
                        actionId: actionId,
                        parametersJson: node.parametersJson
                    )
                    WarpWidgetBridge.shared.reloadTimelines()
                }
                .buttonStyle(.bordered)
                .padding(node.padding)
            } else {
                Text(node.text ?? "")
                    .padding(node.padding)
            }
        }
    }
}

enum WarpNodeKind {
    case column
    case row
    case text
    case button
}

struct WarpParsedNode {
    let kind: WarpNodeKind
    let text: String?
    let actionId: String?
    let parametersJson: String
    let padding: EdgeInsets
    let children: [WarpParsedNode]
}

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

@available(iOS 17.0, *)
struct WarpClickIntent: AppIntent {
    static var title: LocalizedStringResource = "WARP Click"

    @Parameter(title: "Action ID")
    var actionId: String

    @Parameter(title: "Parameters JSON")
    var parametersJson: String

    init() {
        actionId = ""
        parametersJson = "{}"
    }

    init(actionId: String, parametersJson: String) {
        self.actionId = actionId
        self.parametersJson = parametersJson
    }

    func perform() async throws -> some IntentResult {
        WarpClickBridge.shared.perform(actionId: actionId, parametersJson: parametersJson)
        WarpWidgetBridge.shared.reloadTimelines()
        return .result()
    }
}
