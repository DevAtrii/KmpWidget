import AppIntents
import Shared
import warpWidgetKit

/// WidgetKit interactive tap. **Must live in this extension target**, not Shared.
///
/// Conform to [WarpClickAppIntent] and register with
/// `WarpClickIntentRegistry.install(Self.self, for: CounterWarpWidget.shared.id)`.
/// Button chrome lives in `warpWidgetKit`.
///
/// ### Flow
/// ```
/// WarpSwiftUIRootView(widgetId:) + WarpClickIntentRegistry
///   → Button(intent: CounterWidgetClickIntent)
///   → perform()
///   → WarpWidgetHost.dispatchClick(widget: CounterWarpWidget…)
///   → WarpClicksRegistry → CounterWarpClickHandler
///   → updateWarpWidgetState (UserDefaults) + WidgetCenter.reload
/// ```
@available(iOS 17.0, *)
struct CounterWidgetClickIntent: WarpClickAppIntent {
    static var title: LocalizedStringResource = "Counter Widget Click"
    static var openAppWhenRun: Bool = false

    /// WARP wire id (`"increment"` / `"decrement"`) from Kotlin JSON.
    @Parameter(title: "Action ID")
    var actionId: String

    /// JSON object of string params from WARP `onClick.parameters`, or `"{}"`.
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
        let session = WarpWidgetHost.shared.iosSession(
            widget: CounterWarpWidget.shared,
            kitFields: WarpWidgetKitEnv.placeholder().asKitFields(
                appGroupId: CounterWarpWidget.shared.iosGroupId
            )
        )
        WarpWidgetHost.shared.dispatchClick(
            widget: CounterWarpWidget.shared,
            session: session,
            actionId: actionId,
            parametersJson: parametersJson
        )
        return .result()
    }
}
