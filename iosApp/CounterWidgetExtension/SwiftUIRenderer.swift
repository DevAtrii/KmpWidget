import Shared
import SwiftUI

struct WarpNodeView: View {
    let node: WarpNode
    let state: [String: String]

    var body: some View {
        switch node {
        case .row(let row):
            WarpRowView(row: row, state: state)
        case .column(let column):
            WarpColumnView(column: column, state: state)
        case .text(let textNode):
            WarpTextView(textNode: textNode, state: state)
        case .button(let buttonNode):
            WarpButtonView(buttonNode: buttonNode)
        }
    }
}

struct WarpRowView: View {
    let row: WarpRowNode
    let state: [String: String]

    var body: some View {
        applyModifiers(
            to: HStack(alignment: verticalAlignment(row.verticalAlignment), spacing: 8) {
                ForEach(Array(row.children.enumerated()), id: \.offset) { _, child in
                    WarpNodeView(node: child, state: state)
                }
            }
            .frame(
                maxWidth: .infinity,
                maxHeight: .infinity,
                alignment: frameAlignment(
                    vertical: row.verticalAlignment,
                    horizontal: row.horizontalAlignment
                )
            ),
            modifier: row.modifier,
            includeWeight: false
        )
    }
}

struct WarpColumnView: View {
    let column: WarpColumnNode
    let state: [String: String]

    var body: some View {
        applyModifiers(
            to: VStack(alignment: horizontalAlignment(column.horizontalAlignment), spacing: 8) {
                ForEach(Array(column.children.enumerated()), id: \.offset) { _, child in
                    WarpNodeView(node: child, state: state)
                }
            }
            .frame(
                maxWidth: .infinity,
                maxHeight: .infinity,
                alignment: frameAlignment(
                    vertical: column.verticalAlignment,
                    horizontal: column.horizontalAlignment
                )
            ),
            modifier: column.modifier,
            includeWeight: false
        )
    }
}

struct WarpTextView: View {
    let textNode: WarpTextNode
    let state: [String: String]

    var body: some View {
        applyModifiers(
            to: Text(resolveText())
                .frame(maxWidth: .infinity),
            modifier: textNode.modifier
        )
    }

    private func resolveText() -> String {
        if let text = textNode.text {
            return text
        }
        if let stateKey = textNode.stateKey {
            return state[stateKey] ?? "0"
        }
        return ""
    }
}

struct WarpButtonView: View {
    let buttonNode: WarpButtonNode

    var body: some View {
        switch buttonNode.action {
        case .callback(let action):
            switch action.id {
            case "increment":
                Button(intent: IncrementCounterIntent()) {
                    Text(buttonNode.label)
                }
            case "decrement":
                Button(intent: DecrementCounterIntent()) {
                    Text(buttonNode.label)
                }
            default:
                Text(buttonNode.label)
            }
        case .openUrl(let action):
            if let url = URL(string: action.url) {
                Link(buttonNode.label, destination: url)
            } else {
                Text(buttonNode.label)
            }
        }
    }
}

@ViewBuilder
private func applyModifiers<Content: View>(
    to content: Content,
    modifier: WarpModifier,
    includeWeight: Bool = true
) -> some View {
    if let padding = modifier.padding, let background = modifier.background {
        if includeWeight, modifier.weight != nil {
            content
                .padding(.all, CGFloat(padding.all))
                .background(Color(warpColor: background))
                .frame(maxWidth: .infinity, alignment: .center)
                .layoutPriority(Double(modifier.weight ?? 1))
        } else {
            content
                .padding(.all, CGFloat(padding.all))
                .background(Color(warpColor: background))
        }
    } else if let padding = modifier.padding {
        if includeWeight, modifier.weight != nil {
            content
                .padding(.all, CGFloat(padding.all))
                .frame(maxWidth: .infinity, alignment: .center)
                .layoutPriority(Double(modifier.weight ?? 1))
        } else {
            content
                .padding(.all, CGFloat(padding.all))
        }
    } else if let background = modifier.background {
        if includeWeight, modifier.weight != nil {
            content
                .background(Color(warpColor: background))
                .frame(maxWidth: .infinity, alignment: .center)
                .layoutPriority(Double(modifier.weight ?? 1))
        } else {
            content
                .background(Color(warpColor: background))
        }
    } else if includeWeight, modifier.weight != nil {
        content
            .frame(maxWidth: .infinity, alignment: .center)
            .layoutPriority(Double(modifier.weight ?? 1))
    } else {
        content
    }
}

private func verticalAlignment(_ value: String) -> VerticalAlignment {
    switch value {
    case "Top": return .top
    case "Bottom": return .bottom
    default: return .center
    }
}

private func horizontalAlignment(_ value: String) -> HorizontalAlignment {
    switch value {
    case "Start": return .leading
    case "End": return .trailing
    default: return .center
    }
}

private func frameAlignment(vertical: String, horizontal: String) -> Alignment {
    switch (vertical, horizontal) {
    case ("Top", "Start"): return .topLeading
    case ("Top", "End"): return .topTrailing
    case ("Top", _): return .top
    case ("Bottom", "Start"): return .bottomLeading
    case ("Bottom", "End"): return .bottomTrailing
    case ("Bottom", _): return .bottom
    case (_, "Start"): return .leading
    case (_, "End"): return .trailing
    default: return .center
    }
}

private extension Color {
    init(warpColor: WarpColor) {
        let argb = warpColor.argb
        let alpha = Double((argb >> 24) & 0xFF) / 255.0
        let red = Double((argb >> 16) & 0xFF) / 255.0
        let green = Double((argb >> 8) & 0xFF) / 255.0
        let blue = Double(argb & 0xFF) / 255.0
        self.init(.sRGB, red: red, green: green, blue: blue, opacity: alpha)
    }
}
