import AppIntents

struct IncrementCounterIntent: AppIntent {
    static var title: LocalizedStringResource = "Increment Counter"
    static var openAppWhenRun: Bool = false

    init() {}

    func perform() async throws -> some IntentResult {
        CounterStore.mutate(by: 1)
        return .result()
    }
}

struct DecrementCounterIntent: AppIntent {
    static var title: LocalizedStringResource = "Decrement Counter"
    static var openAppWhenRun: Bool = false

    init() {}

    func perform() async throws -> some IntentResult {
        CounterStore.mutate(by: -1)
        return .result()
    }
}
