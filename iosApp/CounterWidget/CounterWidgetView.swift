import Shared
import SwiftUI
import WidgetKit
import warpWidgetKit

/// [CounterWarpWidget] via [WarpWidgetHost] + [WarpWidgetKitEnv] (kit → Shared via Kotlin).
func counterWidgetJson(context: TimelineProvider.Context) -> String {
    WarpWidgetKitMappingKt.installWarpWidgetKitBridge()
    let session: WarpWidgetSession = WarpWidgetKitEnv.from(context: context).makeSession()
    WarpWidgetHost.shared.prepare(widget: CounterWarpWidget.shared, session: session)
    return WarpWidgetHost.shared.composeJson(widget: CounterWarpWidget.shared, session: session)
}

/// Cold-start / preview without a timeline context.
func counterWidgetJsonPlaceholder() -> String {
    WarpWidgetKitMappingKt.installWarpWidgetKitBridge()
    let session: WarpWidgetSession = WarpWidgetKitEnv.placeholder().makeSession()
    WarpWidgetHost.shared.prepare(widget: CounterWarpWidget.shared, session: session)
    return WarpWidgetHost.shared.composeJson(widget: CounterWarpWidget.shared, session: session)
}

/// Hosts WARP JSON as pure SwiftUI (`useIntents: true` → extension AppIntent buttons).
func counterWidgetRootView(json: String) -> WarpSwiftUIRootView {
    WarpSwiftUIRootView(json: json, useIntents: true)
}
