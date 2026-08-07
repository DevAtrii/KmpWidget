import AppIntents
import Foundation

/// Stable per-widget identity — new UUID when user adds another Counter widget.
struct CounterInstanceEntity: AppEntity {
    var id: String

    static var typeDisplayRepresentation = TypeDisplayRepresentation(name: "Counter")

    static var defaultQuery = CounterInstanceQuery()

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(title: "Counter")
    }
}

struct CounterInstanceQuery: EntityQuery {
    func entities(for identifiers: [CounterInstanceEntity.ID]) async throws -> [CounterInstanceEntity] {
        identifiers.map { CounterInstanceEntity(id: $0) }
    }

    func suggestedEntities() async throws -> [CounterInstanceEntity] {
        [CounterInstanceEntity(id: UUID().uuidString)]
    }

    func defaultResult() async -> CounterInstanceEntity? {
        CounterInstanceEntity(id: UUID().uuidString)
    }
}
