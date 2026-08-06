//
//  CounterWarpRenderView.swift
//  CounterWidget
//

import AppIntents
import Shared
import SwiftUI
import WidgetKit

struct CounterWarpRenderView: View {
    let json: String

    var body: some View {
        if let root = CounterWarpNodeParser.parse(json: json) {
            CounterWarpNodeView(node: root)
        } else {
            Text("Invalid WARP node JSON")
                .font(.caption)
        }
    }
}

private struct CounterWarpNodeView: View {
    let node: CounterWarpParsedNode

    var body: some View {
        switch node.kind {
        case .column:
            VStack(alignment: .center, spacing: 6) {
                ForEach(Array(node.children.enumerated()), id: \.offset) { _, child in
                    CounterWarpNodeView(node: child)
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .padding(node.padding)

        case .row:
            HStack(spacing: 6) {
                ForEach(Array(node.children.enumerated()), id: \.offset) { index, child in
                    if node.children.count == 3, index == 1, child.kind == .text {
                        CounterWarpNodeView(node: child)
                            .frame(maxWidth: .infinity)
                            .layoutPriority(1)
                    } else if child.kind == .button {
                        CounterWarpNodeView(node: child)
                            .frame(minWidth: 32, maxWidth: 40)
                    } else {
                        CounterWarpNodeView(node: child)
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
            if #available(iOS 17.0, *), let actionId = node.actionId {
                Button(intent: CounterWidgetClickIntent(actionId: actionId, parametersJson: node.parametersJson)) {
                    Text(node.text ?? "")
                        .font(.title3.weight(.semibold))
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                }
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

private enum CounterWarpNodeKind {
    case column
    case row
    case text
    case button
}

private struct CounterWarpParsedNode {
    let kind: CounterWarpNodeKind
    let text: String?
    let actionId: String?
    let parametersJson: String
    let padding: EdgeInsets
    let children: [CounterWarpParsedNode]
}

private enum CounterWarpNodeParser {
    static func parse(json: String) -> CounterWarpParsedNode? {
        guard
            let data = json.data(using: .utf8),
            let object = try? JSONSerialization.jsonObject(with: data) as? [String: Any]
        else {
            return nil
        }
        return parseNode(object)
    }

    private static func parseNode(_ object: [String: Any]) -> CounterWarpParsedNode? {
        guard let type = object["type"] as? String else { return nil }
        let padding = parsePadding(object["modifier"] as? [String: Any])
        let children = (object["children"] as? [[String: Any]] ?? [])
            .compactMap(parseNode)

        switch type {
        case "column":
            return CounterWarpParsedNode(
                kind: .column,
                text: nil,
                actionId: nil,
                parametersJson: "{}",
                padding: padding,
                children: children
            )
        case "row":
            return CounterWarpParsedNode(
                kind: .row,
                text: nil,
                actionId: nil,
                parametersJson: "{}",
                padding: padding,
                children: children
            )
        case "text":
            return CounterWarpParsedNode(
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
            return CounterWarpParsedNode(
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
struct CounterWidgetClickIntent: AppIntent {
    static var title: LocalizedStringResource = "Counter Widget Click"

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
        CounterWidgetIosKt.dispatchCounterWidgetClick(
            actionId: actionId,
            parametersJson: parametersJson
        )
        WidgetCenter.shared.reloadAllTimelines()
        return .result()
    }
}
