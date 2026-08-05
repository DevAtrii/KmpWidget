import Foundation

struct WarpDocument: Codable {
    let schemaVersion: Int
    let widgetKind: String
    let root: WarpNode
}

enum WarpNode: Codable {
    case row(WarpRowNode)
    case column(WarpColumnNode)
    case text(WarpTextNode)
    case button(WarpButtonNode)

    enum CodingKeys: String, CodingKey {
        case type
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        let type = try container.decode(String.self, forKey: .type)

        switch type {
        case "row":
            self = .row(try WarpRowNode(from: decoder))
        case "column":
            self = .column(try WarpColumnNode(from: decoder))
        case "text":
            self = .text(try WarpTextNode(from: decoder))
        case "button":
            self = .button(try WarpButtonNode(from: decoder))
        default:
            throw DecodingError.dataCorruptedError(
                forKey: .type,
                in: container,
                debugDescription: "Unsupported WARP node type: \(type)"
            )
        }
    }

    func encode(to encoder: Encoder) throws {
        switch self {
        case .row(let node):
            try node.encode(to: encoder)
        case .column(let node):
            try node.encode(to: encoder)
        case .text(let node):
            try node.encode(to: encoder)
        case .button(let node):
            try node.encode(to: encoder)
        }
    }
}

struct WarpModifier: Codable {
    let padding: WarpPadding?
    let background: WarpColor?
    let weight: Float?
}

struct WarpPadding: Codable {
    let all: Int
}

struct WarpColor: Codable {
    let argb: Int64
}

struct WarpRowNode: Codable {
    let type: String
    let modifier: WarpModifier
    let verticalAlignment: String
    let horizontalAlignment: String
    let children: [WarpNode]
}

struct WarpColumnNode: Codable {
    let type: String
    let modifier: WarpModifier
    let verticalAlignment: String
    let horizontalAlignment: String
    let children: [WarpNode]
}

struct WarpTextNode: Codable {
    let type: String
    let modifier: WarpModifier
    let text: String?
    let stateKey: String?
}

struct WarpButtonNode: Codable {
    let type: String
    let modifier: WarpModifier
    let label: String
    let action: WarpAction
}

enum WarpAction: Codable {
    case callback(WarpCallbackAction)
    case openUrl(WarpOpenUrlAction)

    enum CodingKeys: String, CodingKey {
        case type
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        let type = try container.decode(String.self, forKey: .type)

        switch type {
        case "callback":
            self = .callback(try WarpCallbackAction(from: decoder))
        case "open_url":
            self = .openUrl(try WarpOpenUrlAction(from: decoder))
        default:
            throw DecodingError.dataCorruptedError(
                forKey: .type,
                in: container,
                debugDescription: "Unsupported WARP action type: \(type)"
            )
        }
    }

    func encode(to encoder: Encoder) throws {
        switch self {
        case .callback(let action):
            try action.encode(to: encoder)
        case .openUrl(let action):
            try action.encode(to: encoder)
        }
    }
}

struct WarpCallbackAction: Codable {
    let type: String
    let id: String
    let payload: [String: String]
}

struct WarpOpenUrlAction: Codable {
    let type: String
    let url: String
    let id: String
}

enum WarpIrDecoder {
    static func decode(_ json: String) throws -> WarpDocument {
        let data = Data(json.utf8)
        return try JSONDecoder().decode(WarpDocument.self, from: data)
    }
}
