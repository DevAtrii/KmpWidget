import SwiftUI
import AppIntents
import UIKit

// MARK: - AppIntent host hook

/// Extension-local `AppIntent` for WARP widget buttons.
///
/// WidgetKit only discovers intents compiled into the **widget extension** — not Shared.
/// Conform + call [WarpClickIntentRegistry.install(_:for:)].
///
/// ```swift
/// struct MyClickIntent: WarpClickAppIntent { /* init(actionId:parametersJson:) + perform */ }
///
/// // WidgetBundle.init — one install per widget kind (`WarpWidget.id`):
/// WarpClickIntentRegistry.install(CounterClickIntent.self, for: CounterWarpWidget.shared.id)
/// ```
@available(iOS 17.0, *)
public protocol WarpClickAppIntent: AppIntent {
    init(actionId: String, parametersJson: String)
}

/// Per-widget-kind registry of [WarpClickAppIntent] factories.
@available(iOS 17.0, *)
public enum WarpClickIntentRegistry {
    private static var factories: [String: (String, String) -> any AppIntent] = [:]

    public static func install<I: WarpClickAppIntent>(_ type: I.Type, for widgetId: String) {
        factories[widgetId] = { actionId, parametersJson in
            I(actionId: actionId, parametersJson: parametersJson)
        }
    }

    public static func uninstall(for widgetId: String) {
        factories.removeValue(forKey: widgetId)
    }

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

// MARK: - SwiftUI root

/// Pure SwiftUI tree for WidgetKit / previews from WARP JSON.
///
/// WARP modifiers are the single source of truth — this renderer applies only
/// styles present in `modifier.elements[]` (no invented font/padding/chrome).
public struct WarpSwiftUIRootView: View {
    let json: String
    let useIntents: Bool
    let widgetId: String

    public init(json: String, useIntents: Bool, widgetId: String = "") {
        self.json = json
        self.useIntents = useIntents
        self.widgetId = widgetId
    }

    public var body: some View {
        if let root = WarpNodeParser.parse(json: json) {
            WarpNodeView(node: root, useIntents: useIntents, widgetId: widgetId)
        } else {
            Text("Invalid WARP node JSON")
        }
    }
}

/// Recursive SwiftUI mapping of a parsed WARP node.
private struct WarpNodeView: View {
    let node: WarpParsedNode
    let useIntents: Bool
    let widgetId: String

    var body: some View {
        content
            .modifier(WarpStyleModifier(
                style: node.style,
                // Glance: weight expands slot; textAlign (if any) aligns content inside — else Start.
                weightContentAlignment: node.textArgs.textAlign?.frameAlignment ?? .leading
            ))
            .opacity(node.enabled ? 1 : 0.4)
            .modifier(WarpClickModifier(
                actionId: node.enabled ? node.effectiveActionId : nil,
                parametersJson: node.effectiveParametersJson,
                useIntents: useIntents,
                widgetId: widgetId
            ))
    }

    @ViewBuilder
    private var content: some View {
        switch node.kind {
        case .column:
            // Cross-axis only (Glance Column.horizontalAlignment).
            VStack(alignment: node.horizontalAlignment.stackAlignment, spacing: 0) {
                ForEach(Array(node.children.enumerated()), id: \.offset) { _, child in
                    WarpNodeView(node: child, useIntents: useIntents, widgetId: widgetId)
                }
            }

        case .row:
            HStack(alignment: node.verticalAlignment.stackAlignment, spacing: 0) {
                ForEach(Array(node.children.enumerated()), id: \.offset) { _, child in
                    WarpNodeView(node: child, useIntents: useIntents, widgetId: widgetId)
                }
            }

        case .box:
            ZStack(alignment: node.contentAlignment) {
                ForEach(Array(node.children.enumerated()), id: \.offset) { _, child in
                    WarpNodeView(node: child, useIntents: useIntents, widgetId: widgetId)
                }
            }

        case .text:
            Text(node.text ?? "")
                .modifier(WarpTextArgsModifier(args: node.textArgs))

        case .button:
            Text(node.text ?? "")
                .modifier(WarpTextArgsModifier(args: node.textArgs))

        case .spacer:
            Color.clear
                .frame(width: 0, height: 0) // size comes from WarpStyleModifier width/height/size

        case .divider:
            Rectangle()
                .fill(node.dividerColor ?? Color.secondary.opacity(0.35))
                .frame(height: CGFloat(node.dividerThickness))
                .frame(maxWidth: .infinity)

        case .progressIndicator:
            if node.progressStyle == .linear {
                ProgressView(value: node.progress.map { Double($0) })
                    .progressViewStyle(.linear)
                    .tint(node.progressColor)
            } else {
                ProgressView()
                    .progressViewStyle(.circular)
                    .tint(node.progressColor)
            }

        case .image:
            WarpImageView(
                asset: node.imageAsset,
                contentScale: node.imageContentScale,
                tint: node.imageTint
            )
        }
    }
}

/// Resolves [WarpAsset] — SF Symbols via `Image(systemName:)`, catalog ids, local URIs.
private struct WarpImageView: View {
    let asset: WarpParsedAsset?
    let contentScale: WarpParsedContentScale
    let tint: Color?

    var body: some View {
        Group {
            if let asset {
                resolved(asset)
            } else {
                Color.clear
            }
        }
    }

    @ViewBuilder
    private func resolved(_ asset: WarpParsedAsset) -> some View {
        switch asset {
        case .system(let name):
            // SF Symbol — native WidgetKit path; tint via template rendering.
            Image(systemName: name)
                .resizable()
                .symbolRenderingMode(.monochrome)
                .modifier(WarpImageScaleModifier(contentScale: contentScale))
                .foregroundStyle(tint ?? Color.primary)
        case .id(let id):
            let image = Image(id).resizable()
            if let tint {
                image
                    .renderingMode(.template)
                    .modifier(WarpImageScaleModifier(contentScale: contentScale))
                    .foregroundStyle(tint)
            } else {
                image.modifier(WarpImageScaleModifier(contentScale: contentScale))
            }
        case .uri(let uriString):
            if let image = loadUriImage(uriString) {
                let view = image.resizable()
                if let tint {
                    view
                        .renderingMode(.template)
                        .modifier(WarpImageScaleModifier(contentScale: contentScale))
                        .foregroundStyle(tint)
                } else {
                    view.modifier(WarpImageScaleModifier(contentScale: contentScale))
                }
            } else {
                Color.clear
            }
        }
    }

    private func loadUriImage(_ uriString: String) -> Image? {
        guard let url = URL(string: uriString), url.isFileURL else { return nil }
        // Local / App Group files only — remote http(s) not supported.
        guard let data = try? Data(contentsOf: url),
              let uiImage = UIImage(data: data)
        else {
            return nil
        }
        return Image(uiImage: uiImage)
    }
}

private struct WarpImageScaleModifier: ViewModifier {
    let contentScale: WarpParsedContentScale

    func body(content: Content) -> some View {
        switch contentScale {
        case .fit:
            content.scaledToFit()
        case .crop:
            content.scaledToFill().clipped()
        case .fillBounds:
            content.frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }
}

/// Applies [WarpTextStyle] / button `colors.contentColor` / `maxLines` when present.
private struct WarpTextArgsModifier: ViewModifier {
    let args: WarpParsedTextArgs

    func body(content: Content) -> some View {
        var view = AnyView(content)
        if let size = args.fontSize {
            let weight = args.fontWeight ?? .regular
            view = AnyView(view.font(.system(size: size, weight: weight)))
        } else if let weight = args.fontWeight {
            view = AnyView(view.fontWeight(weight))
        }
        if let color = args.color {
            view = AnyView(view.foregroundStyle(color))
        }
        if let limit = args.maxLines {
            view = AnyView(view.lineLimit(limit))
        }
        if let align = args.textAlign {
            view = AnyView(view.multilineTextAlignment(align))
        }
        return view
    }
}

private extension TextAlignment {
    var frameAlignment: Alignment {
        switch self {
        case .center: return .center
        case .trailing: return .trailing
        default: return .leading
        }
    }
}

// MARK: - Apply WARP style (layout / appearance only)

private struct WarpStyleModifier: ViewModifier {
    let style: WarpParsedStyle
    /// Alignment inside a weighted slot (Glance Text textAlign / default Start).
    var weightContentAlignment: Alignment = .leading

    func body(content: Content) -> some View {
        var view = AnyView(content)

        if let w = style.width, let h = style.height {
            view = AnyView(view.frame(width: CGFloat(w), height: CGFloat(h)))
        } else {
            if let w = style.width {
                view = AnyView(view.frame(width: CGFloat(w)))
            }
            if let h = style.height {
                view = AnyView(view.frame(height: CGFloat(h)))
            }
        }

        if style.fillMaxWidth || style.fillMaxSize {
            view = AnyView(view.frame(maxWidth: .infinity, alignment: .leading))
        }
        if style.fillMaxHeight || style.fillMaxSize {
            view = AnyView(view.frame(maxHeight: .infinity, alignment: .top))
        }
        if style.wrapContentWidth {
            view = AnyView(view.fixedSize(horizontal: true, vertical: false))
        }
        if style.wrapContentHeight {
            view = AnyView(view.fixedSize(horizontal: false, vertical: true))
        }
        if style.wrapContentSize {
            view = AnyView(view.fixedSize())
        }
        if style.weight != nil {
            // Glance defaultWeight: expand; content Start unless textAlign set.
            view = AnyView(
                view.frame(maxWidth: .infinity, alignment: weightContentAlignment)
            )
        }

        view = AnyView(view.padding(style.padding))

        if let color = style.background {
            if let radius = style.cornerRadius {
                view = AnyView(
                    view.background(
                        RoundedRectangle(cornerRadius: CGFloat(radius))
                            .fill(color)
                    )
                )
            } else {
                view = AnyView(view.background(color))
            }
        } else if let radius = style.cornerRadius {
            view = AnyView(
                view.clipShape(RoundedRectangle(cornerRadius: CGFloat(radius)))
            )
        }

        if let border = style.border {
            let shape = RoundedRectangle(cornerRadius: CGFloat(style.cornerRadius ?? 0))
            view = AnyView(
                view.overlay(
                    shape.stroke(border.color, lineWidth: CGFloat(border.width))
                )
            )
        }

        if let alpha = style.alpha {
            view = AnyView(view.opacity(Double(alpha)))
        }

        switch style.visibility {
        case .invisible:
            view = AnyView(view.hidden())
        case .gone:
            view = AnyView(EmptyView())
        case .visible, .none:
            break
        }

        return view
    }
}

/// Applies effective click (modifier.clickable wins over node onClick).
private struct WarpClickModifier: ViewModifier {
    let actionId: String?
    let parametersJson: String
    let useIntents: Bool
    let widgetId: String

    func body(content: Content) -> some View {
        guard let actionId else { return AnyView(content) }

        if useIntents, #available(iOS 17.0, *), !widgetId.isEmpty,
           let intent = WarpClickIntentRegistry.intent(
            widgetId: widgetId,
            actionId: actionId,
            parametersJson: parametersJson
           ) {
            return AnyView(WarpIntentPlainButton(intent: intent, label: content))
        }

        return AnyView(
            Button {
                if #available(iOS 17.0, *) {
                    WarpClickBridge.shared.perform(
                        actionId: actionId,
                        parametersJson: parametersJson
                    )
                    WarpWidgetBridge.shared.reloadTimelines()
                }
            } label: {
                content
            }
            .buttonStyle(.plain)
        )
    }
}

@available(iOS 17.0, *)
private struct WarpIntentPlainButton<Label: View>: View {
    let intent: any AppIntent
    let label: Label

    var body: some View {
        open(intent)
    }

    private func open<I: AppIntent>(_ intent: I) -> AnyView {
        AnyView(
            Button(intent: intent) { label }
                .buttonStyle(.plain)
        )
    }
}

// MARK: - JSON → model

enum WarpNodeKind {
    case column
    case row
    case box
    case text
    case button
    case spacer
    case divider
    case progressIndicator
    case image
}

enum WarpParsedAsset {
    case id(String)
    case system(String)
    case uri(String)
}

enum WarpParsedContentScale {
    case fit
    case crop
    case fillBounds
}

enum WarpParsedProgressStyle {
    case circular
    case linear
}

enum WarpParsedVisibility {
    case visible
    case invisible
    case gone
}

struct WarpParsedBorder {
    let width: Int
    let color: Color
}

/// Folded layout/appearance from `modifier.elements[]` — only fields present in WARP.
struct WarpParsedStyle {
    var padding: EdgeInsets = EdgeInsets()
    var background: Color?
    var cornerRadius: Int?
    var alpha: Float?
    var border: WarpParsedBorder?
    var visibility: WarpParsedVisibility?
    var fillMaxWidth = false
    var fillMaxHeight = false
    var fillMaxSize = false
    var width: Int?
    var height: Int?
    var weight: Float?
    var wrapContentWidth = false
    var wrapContentHeight = false
    var wrapContentSize = false
}

/// Text args from button/text parameters (not modifiers).
struct WarpParsedTextArgs {
    var color: Color?
    var fontSize: CGFloat?
    var fontWeight: Font.Weight?
    var textAlign: TextAlignment?
    var maxLines: Int?
}

enum WarpParsedHorizontalAlignment {
    case start, center, end

    var stackAlignment: HorizontalAlignment {
        switch self {
        case .start: return .leading
        case .center: return .center
        case .end: return .trailing
        }
    }

    var frameAlignment: Alignment {
        switch self {
        case .start: return .leading
        case .center: return .center
        case .end: return .trailing
        }
    }
}

enum WarpParsedVerticalAlignment {
    case top, center, bottom

    var stackAlignment: VerticalAlignment {
        switch self {
        case .top: return .top
        case .center: return .center
        case .bottom: return .bottom
        }
    }

    var frameAlignment: Alignment {
        switch self {
        case .top: return .top
        case .center: return .center
        case .bottom: return .bottom
        }
    }
}

struct WarpParsedNode {
    let kind: WarpNodeKind
    let text: String?
    /// Node-level `onClick` (button).
    let nodeActionId: String?
    let nodeParametersJson: String
    /// Modifier `clickable` — wins over node action when set.
    let modifierActionId: String?
    let modifierParametersJson: String
    let style: WarpParsedStyle
    let enabled: Bool
    let textArgs: WarpParsedTextArgs
    let horizontalAlignment: WarpParsedHorizontalAlignment
    let verticalAlignment: WarpParsedVerticalAlignment
    /// Glance Box `contentAlignment`.
    let contentAlignment: Alignment
    let dividerThickness: Int
    let dividerColor: Color?
    let progressStyle: WarpParsedProgressStyle
    let progress: Float?
    let progressColor: Color?
    let imageAsset: WarpParsedAsset?
    let imageTint: Color?
    let imageContentScale: WarpParsedContentScale
    let children: [WarpParsedNode]

    /// Modifier clickable first, then node onClick.
    var effectiveActionId: String? {
        modifierActionId ?? nodeActionId
    }

    var effectiveParametersJson: String {
        if modifierActionId != nil {
            return modifierParametersJson
        }
        return nodeParametersJson
    }

    static func leafDefaults(
        kind: WarpNodeKind,
        text: String? = nil,
        nodeActionId: String? = nil,
        nodeParametersJson: String = "{}",
        modifierActionId: String? = nil,
        modifierParametersJson: String = "{}",
        style: WarpParsedStyle,
        enabled: Bool = true,
        textArgs: WarpParsedTextArgs = WarpParsedTextArgs(),
        contentAlignment: Alignment = .topLeading,
        dividerThickness: Int = 1,
        dividerColor: Color? = nil,
        progressStyle: WarpParsedProgressStyle = .circular,
        progress: Float? = nil,
        progressColor: Color? = nil,
        imageAsset: WarpParsedAsset? = nil,
        imageTint: Color? = nil,
        imageContentScale: WarpParsedContentScale = .fit,
        children: [WarpParsedNode] = []
    ) -> WarpParsedNode {
        WarpParsedNode(
            kind: kind,
            text: text,
            nodeActionId: nodeActionId,
            nodeParametersJson: nodeParametersJson,
            modifierActionId: modifierActionId,
            modifierParametersJson: modifierParametersJson,
            style: style,
            enabled: enabled,
            textArgs: textArgs,
            horizontalAlignment: .start,
            verticalAlignment: .top,
            contentAlignment: contentAlignment,
            dividerThickness: dividerThickness,
            dividerColor: dividerColor,
            progressStyle: progressStyle,
            progress: progress,
            progressColor: progressColor,
            imageAsset: imageAsset,
            imageTint: imageTint,
            imageContentScale: imageContentScale,
            children: children
        )
    }
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
        let modifier = object["modifier"] as? [String: Any]
        let style = parseStyle(modifier)
        let (modActionId, modParams) = parseClickable(modifier)
        let children = (object["children"] as? [[String: Any]] ?? [])
            .compactMap(parseNode)

        let horizontalAlignment = parseHorizontalAlignment(object["horizontalAlignment"] as? String)
        let verticalAlignment = parseVerticalAlignment(object["verticalAlignment"] as? String)

        switch type {
        case "column":
            return WarpParsedNode(
                kind: .column,
                text: nil,
                nodeActionId: nil,
                nodeParametersJson: "{}",
                modifierActionId: modActionId,
                modifierParametersJson: modParams,
                style: style,
                enabled: true,
                textArgs: WarpParsedTextArgs(),
                horizontalAlignment: horizontalAlignment,
                verticalAlignment: verticalAlignment,
                contentAlignment: .topLeading,
                dividerThickness: 1,
                dividerColor: nil,
                progressStyle: .circular,
                progress: nil,
                progressColor: nil,
                imageAsset: nil,
                imageTint: nil,
                imageContentScale: .fit,
                children: children
            )
        case "row":
            return WarpParsedNode(
                kind: .row,
                text: nil,
                nodeActionId: nil,
                nodeParametersJson: "{}",
                modifierActionId: modActionId,
                modifierParametersJson: modParams,
                style: style,
                enabled: true,
                textArgs: WarpParsedTextArgs(),
                horizontalAlignment: horizontalAlignment,
                verticalAlignment: verticalAlignment,
                contentAlignment: .topLeading,
                dividerThickness: 1,
                dividerColor: nil,
                progressStyle: .circular,
                progress: nil,
                progressColor: nil,
                imageAsset: nil,
                imageTint: nil,
                imageContentScale: .fit,
                children: children
            )
        case "box":
            return WarpParsedNode.leafDefaults(
                kind: .box,
                modifierActionId: modActionId,
                modifierParametersJson: modParams,
                style: style,
                contentAlignment: parseContentAlignment(object["contentAlignment"] as? String),
                children: children
            )
        case "text":
            var textArgs = parseTextStyle(object["style"] as? [String: Any])
            if let maxLines = object["maxLines"] as? Int, maxLines != Int.max {
                textArgs.maxLines = maxLines
            }
            return WarpParsedNode.leafDefaults(
                kind: .text,
                text: object["text"] as? String ?? "",
                modifierActionId: modActionId,
                modifierParametersJson: modParams,
                style: style,
                textArgs: textArgs
            )
        case "button":
            let click = object["onClick"] as? [String: Any]
            let actionId = click?["actionId"] as? String
            let parameters = click?["parameters"] as? [String: String] ?? [:]
            let parametersJson = jsonString(parameters) ?? "{}"
            var buttonStyle = style
            let colors = object["colors"] as? [String: Any]
            if buttonStyle.background == nil,
               let bg = parseColorValue(colors?["backgroundColor"]) {
                buttonStyle.background = bg
            }
            var textArgs = parseTextStyle(object["style"] as? [String: Any])
            if textArgs.color == nil, let content = parseColorValue(colors?["contentColor"]) {
                textArgs.color = content
            }
            if let maxLines = object["maxLines"] as? Int, maxLines != Int.max {
                textArgs.maxLines = maxLines
            }
            return WarpParsedNode.leafDefaults(
                kind: .button,
                text: object["text"] as? String ?? "",
                nodeActionId: actionId,
                nodeParametersJson: parametersJson,
                modifierActionId: modActionId,
                modifierParametersJson: modParams,
                style: buttonStyle,
                enabled: object["enabled"] as? Bool ?? true,
                textArgs: textArgs
            )
        case "spacer":
            return WarpParsedNode.leafDefaults(
                kind: .spacer,
                modifierActionId: modActionId,
                modifierParametersJson: modParams,
                style: style
            )
        case "divider":
            return WarpParsedNode.leafDefaults(
                kind: .divider,
                modifierActionId: modActionId,
                modifierParametersJson: modParams,
                style: style,
                dividerThickness: object["thickness"] as? Int ?? 1,
                dividerColor: parseColorValue(object["color"])
            )
        case "progress_indicator":
            let progressStyle: WarpParsedProgressStyle =
                (object["style"] as? String) == "linear" ? .linear : .circular
            let progress: Float? = {
                if let p = object["progress"] as? Double { return Float(p) }
                if let p = object["progress"] as? Int { return Float(p) }
                return nil
            }()
            return WarpParsedNode.leafDefaults(
                kind: .progressIndicator,
                modifierActionId: modActionId,
                modifierParametersJson: modParams,
                style: style,
                progressStyle: progressStyle,
                progress: progress,
                progressColor: parseColorValue(object["color"])
            )
        case "image":
            return WarpParsedNode.leafDefaults(
                kind: .image,
                modifierActionId: modActionId,
                modifierParametersJson: modParams,
                style: style,
                imageAsset: parseAsset(object["asset"] as? [String: Any]),
                imageTint: parseColorValue(object["tint"]),
                imageContentScale: parseContentScale(object["contentScale"] as? String)
            )
        default:
            return nil
        }
    }

    private static func parseAsset(_ object: [String: Any]?) -> WarpParsedAsset? {
        guard let object, let type = object["type"] as? String else { return nil }
        switch type {
        case "system":
            guard let name = object["name"] as? String else { return nil }
            return .system(name)
        case "id":
            guard let id = object["id"] as? String else { return nil }
            return .id(id)
        case "uri":
            guard let uri = object["uri"] as? String else { return nil }
            return .uri(uri)
        default:
            return nil
        }
    }

    private static func parseContentScale(_ raw: String?) -> WarpParsedContentScale {
        switch raw {
        case "crop": return .crop
        case "fillBounds": return .fillBounds
        default: return .fit
        }
    }

    private static func parseContentAlignment(_ raw: String?) -> Alignment {
        switch raw {
        case "topCenter": return .top
        case "topEnd": return .topTrailing
        case "centerStart": return .leading
        case "center": return .center
        case "centerEnd": return .trailing
        case "bottomStart": return .bottomLeading
        case "bottomCenter": return .bottom
        case "bottomEnd": return .bottomTrailing
        default: return .topLeading
        }
    }

    private static func parseHorizontalAlignment(
        _ raw: String?
    ) -> WarpParsedHorizontalAlignment {
        switch raw {
        case "center": return .center
        case "end": return .end
        default: return .start
        }
    }

    private static func parseVerticalAlignment(
        _ raw: String?
    ) -> WarpParsedVerticalAlignment {
        switch raw {
        case "center": return .center
        case "bottom": return .bottom
        default: return .top
        }
    }

    private static func parseTextStyle(_ object: [String: Any]?) -> WarpParsedTextArgs {
        var args = WarpParsedTextArgs()
        guard let object else { return args }
        args.color = parseColorValue(object["color"])
        if let size = object["fontSize"] as? Double {
            args.fontSize = CGFloat(size)
        } else if let size = object["fontSize"] as? Int {
            args.fontSize = CGFloat(size)
        }
        switch object["fontWeight"] as? String {
        case "medium": args.fontWeight = .medium
        case "semibold": args.fontWeight = .semibold
        case "bold": args.fontWeight = .bold
        case "normal": args.fontWeight = .regular
        default: break
        }
        switch object["textAlign"] as? String {
        case "center": args.textAlign = .center
        case "end": args.textAlign = .trailing
        case "start": args.textAlign = .leading
        default: break
        }
        return args
    }

    private static func parseColorValue(_ value: Any?) -> Color? {
        if let obj = value as? [String: Any], let hex = obj["hex"] as? String {
            return color(from: hex)
        }
        if let hex = value as? String {
            return color(from: hex)
        }
        return nil
    }

    private static func parseClickable(
        _ modifier: [String: Any]?
    ) -> (String?, String) {
        guard let elements = modifier?["elements"] as? [[String: Any]] else {
            return (nil, "{}")
        }
        // Last clickable wins (matches Kotlin resolvedClickable).
        for element in elements.reversed() where element["type"] as? String == "clickable" {
            let action = element["action"] as? [String: Any]
            let actionId = action?["actionId"] as? String
            let parameters = action?["parameters"] as? [String: String] ?? [:]
            return (actionId, jsonString(parameters) ?? "{}")
        }
        return (nil, "{}")
    }

    private static func parseStyle(_ modifier: [String: Any]?) -> WarpParsedStyle {
        var style = WarpParsedStyle()
        guard let modifier else { return style }

        if let elements = modifier["elements"] as? [[String: Any]] {
            for element in elements {
                guard let type = element["type"] as? String else { continue }
                switch type {
                case "padding":
                    style.padding.top += CGFloat(element["top"] as? Int ?? 0)
                    style.padding.leading += CGFloat(element["start"] as? Int ?? 0)
                    style.padding.bottom += CGFloat(element["bottom"] as? Int ?? 0)
                    style.padding.trailing += CGFloat(element["end"] as? Int ?? 0)
                case "background":
                    if let colorObj = element["color"] as? [String: Any],
                       let hex = colorObj["hex"] as? String {
                        style.background = color(from: hex)
                    } else if let hex = element["color"] as? String {
                        style.background = color(from: hex)
                    }
                case "cornerRadius":
                    style.cornerRadius = element["radius"] as? Int
                case "alpha":
                    if let a = element["alpha"] as? Double {
                        style.alpha = Float(a)
                    } else if let a = element["alpha"] as? Int {
                        style.alpha = Float(a)
                    }
                case "border":
                    let width = element["width"] as? Int ?? 1
                    let hex: String? = {
                        if let c = element["color"] as? [String: Any] {
                            return c["hex"] as? String
                        }
                        return element["color"] as? String
                    }()
                    if let hex, let c = color(from: hex) {
                        style.border = WarpParsedBorder(width: width, color: c)
                    }
                case "visibility":
                    switch element["visibility"] as? String {
                    case "invisible": style.visibility = .invisible
                    case "gone": style.visibility = .gone
                    default: style.visibility = .visible
                    }
                case "fillMaxWidth":
                    style.fillMaxWidth = true
                case "fillMaxHeight":
                    style.fillMaxHeight = true
                case "fillMaxSize":
                    style.fillMaxSize = true
                case "width":
                    style.width = element["width"] as? Int
                case "height":
                    style.height = element["height"] as? Int
                case "size":
                    style.width = element["width"] as? Int
                    style.height = element["height"] as? Int
                case "weight":
                    if let w = element["weight"] as? Double {
                        style.weight = Float(w)
                    } else if let w = element["weight"] as? Int {
                        style.weight = Float(w)
                    } else {
                        style.weight = 1
                    }
                case "wrapContentWidth":
                    style.wrapContentWidth = true
                case "wrapContentHeight":
                    style.wrapContentHeight = true
                case "wrapContentSize":
                    style.wrapContentSize = true
                default:
                    break
                }
            }
            return style
        }

        // Legacy flat shape: modifier.padding { start, end, top, bottom }
        if let padding = modifier["padding"] as? [String: Any] {
            style.padding = EdgeInsets(
                top: CGFloat(padding["top"] as? Int ?? 0),
                leading: CGFloat(padding["start"] as? Int ?? 0),
                bottom: CGFloat(padding["bottom"] as? Int ?? 0),
                trailing: CGFloat(padding["end"] as? Int ?? 0)
            )
        }
        return style
    }

    private static func color(from hex: String) -> Color? {
        var raw = hex.trimmingCharacters(in: .whitespacesAndNewlines)
        if raw.hasPrefix("#") { raw.removeFirst() }
        guard raw.count == 6 || raw.count == 8,
              let value = UInt64(raw, radix: 16) else { return nil }

        let a, r, g, b: UInt64
        if raw.count == 8 {
            a = (value & 0xFF00_0000) >> 24
            r = (value & 0x00FF_0000) >> 16
            g = (value & 0x0000_FF00) >> 8
            b = value & 0x0000_00FF
        } else {
            a = 255
            r = (value & 0xFF0000) >> 16
            g = (value & 0x00FF00) >> 8
            b = value & 0x0000FF
        }
        return Color(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
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
