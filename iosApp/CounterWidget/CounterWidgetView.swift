import Shared
import SwiftUI
import WidgetKit
import warpWidgetKit

/// [CounterWarpWidget] via [WarpWidgetHost] + [WarpWidgetKitEnv] (kit → Shared via Kotlin).
func counterWidgetJson(context: TimelineProviderContext) -> String {
    WarpWidgetKitMappingKt.installWarpWidgetKitBridge()
    let env: WidgetEnvironment = WarpWidgetKitEnv.from(context: context).makeEnvironment()
    let session = WarpWidgetHost.shared.iosSession(
        widget: CounterWarpWidget.shared,
        environment: env
    )
    WarpWidgetHost.shared.prepare(widget: CounterWarpWidget.shared, session: session)
    return WarpWidgetHost.shared.composeJson(widget: CounterWarpWidget.shared, session: session)
}

func counterWidgetPlaceholderJson() -> String {
    WarpWidgetKitMappingKt.installWarpWidgetKitBridge()
    let env: WidgetEnvironment = WarpWidgetKitEnv.placeholder().makeEnvironment()
    let session = WarpWidgetHost.shared.iosSession(
        widget: CounterWarpWidget.shared,
        environment: env
    )
    WarpWidgetHost.shared.prepare(widget: CounterWarpWidget.shared, session: session)
    return WarpWidgetHost.shared.composeJson(widget: CounterWarpWidget.shared, session: session)
}
