import AppIntents
import Foundation

/// Stable per-widget identity — new UUID when user adds another Todo widget.
struct TodoInstanceEntity: AppEntity {
    var id: String

    static var typeDisplayRepresentation = TypeDisplayRepresentation(name: "Todo")

    static var defaultQuery = TodoInstanceQuery()

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(title: "Todo")
    }
}

struct TodoInstanceQuery: EntityQuery {
    func entities(for identifiers: [TodoInstanceEntity.ID]) async throws -> [TodoInstanceEntity] {
        identifiers.map { TodoInstanceEntity(id: $0) }
    }

    func suggestedEntities() async throws -> [TodoInstanceEntity] {
        [TodoInstanceEntity(id: UUID().uuidString)]
    }

    func defaultResult() async -> TodoInstanceEntity? {
        TodoInstanceEntity(id: UUID().uuidString)
    }
}
