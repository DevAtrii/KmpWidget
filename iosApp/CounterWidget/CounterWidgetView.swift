import Shared
import SwiftUI
import warpWidgetKit

/// Kotlin compose + click registration → WARP JSON for the timeline entry.
///
/// ```
/// CounterWidgetIosKt.renderCounterWidget()  // WarpNode + registerWarpClicks
///   → WarpWidgetView_iosKt.warpWidgetJson   // String for SwiftUI
/// ```
func counterWidgetJson() -> String {
    let node = CounterWidgetIosKt.renderCounterWidget()
    return WarpWidgetView_iosKt.warpWidgetJson(node: node)
}

/// Hosts WARP JSON as pure SwiftUI (`useIntents: true` → extension AppIntent buttons).
func counterWidgetRootView(json: String) -> WarpSwiftUIRootView {
    WarpSwiftUIRootView(json: json, useIntents: true)
}
