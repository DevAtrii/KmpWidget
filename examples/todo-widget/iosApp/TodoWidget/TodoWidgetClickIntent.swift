import AppIntents
import SharedLogic
import warpWidgetKit

/// WidgetKit interactive tap. **Must live in this extension target**, not Shared.
@available(iOS 17.0, *)
struct TodoWidgetClickIntent: WarpClickAppIntent {
    static var title: LocalizedStringResource = "Todo Widget Click"
    static var openAppWhenRun: Bool = false

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
        print("TodoWidgetClickIntent.perform actionId=\(actionId) params=\(parametersJson)")
        let session = WarpWidgetHost.shared.iosSession(
            widget: TodoWarpWidget.shared,
            kitFields: WarpWidgetKitEnv.placeholder().asKitFields(
                appGroupId: TodoWarpWidget.shared.iosGroupId
            )
        )
        WarpWidgetHost.shared.dispatchClick(
            widget: TodoWarpWidget.shared,
            session: session,
            actionId: actionId,
            parametersJson: parametersJson
        )
        // Ensure timelines refresh even if Kotlin reload is skipped.
        WarpWidgetBridge.shared.reloadTimelinesOfKind(TodoWarpWidget.shared.id)
        return .result()
    }
}
