import AppIntents
import Shared
import SwiftUI
import warpWidgetKit

// MARK: - Extension AppIntent → WarpWidgetHost

/// WidgetKit interactive tap. **Must live in this extension target**, not Shared.
///
/// ### Why
/// App Intents inside a static Shared / spm4Kmp library are not discovered by WidgetKit.
///
/// ### Flow
/// ```
/// Button(intent: CounterWidgetClickIntent)
///   → perform()
///   → WarpWidgetHost.dispatchClick(widget: CounterWarpWidget…)
///   → WarpClicksRegistry → CounterWarpClickHandler
///   → updateWarpWidgetState (UserDefaults) + WidgetCenter.reload
/// ```
@available(iOS 17.0, *)
struct CounterWidgetClickIntent: AppIntent {
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
        WarpWidgetKitMappingKt.installWarpWidgetKitBridge()
        let env: WidgetEnvironment = WarpWidgetKitEnv.placeholder().makeEnvironment()
        let session = WarpWidgetHost.shared.iosSession(
            widget: CounterWarpWidget.shared,
            environment: env
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

// MARK: - Wire renderer → this intent

/// Installs [WarpClickIntentRegistry.buttonBuilder] so Shared’s SwiftUI renderer
/// creates buttons backed by [CounterWidgetClickIntent] (extension-local).
///
/// Call from `CounterWidgetBundle.init` before any timeline render.
@available(iOS 17.0, *)
enum CounterWidgetClickSetup {
    static func install() {
        WarpClickIntentRegistry.buttonBuilder = { actionId, parametersJson, label in
            AnyView(
                Button(intent: CounterWidgetClickIntent(
                    actionId: actionId,
                    parametersJson: parametersJson
                )) {
                    Text(label)
                        .font(.title3.weight(.semibold))
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                }
                .buttonStyle(.bordered)
                .buttonBorderShape(.circle)
                .controlSize(.small)
            )
        }
    }
}
