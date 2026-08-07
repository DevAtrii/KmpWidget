import Shared
import SwiftUI
import WidgetKit
import warpWidgetKit

/// [CounterWarpWidget] via [WarpWidgetHost] + [WarpWidgetKitEnv] (kit fields → Kotlin; bridge auto).
func counterWidgetJson(context: TimelineProviderContext) -> String {
    let session = WarpWidgetHost.shared.iosSession(
        widget: CounterWarpWidget.shared,
        kitFields: WarpWidgetKitEnv.from(context: context).asKitFields(
            appGroupId: CounterWarpWidget.shared.iosGroupId
        )
    )
    WarpWidgetHost.shared.prepare(widget: CounterWarpWidget.shared, session: session)
    return WarpWidgetHost.shared.composeJson(widget: CounterWarpWidget.shared, session: session)
}

func counterWidgetPlaceholderJson() -> String {
    let session = WarpWidgetHost.shared.iosSession(
        widget: CounterWarpWidget.shared,
        kitFields: WarpWidgetKitEnv.placeholder().asKitFields(
            appGroupId: CounterWarpWidget.shared.iosGroupId
        )
    )
    WarpWidgetHost.shared.prepare(widget: CounterWarpWidget.shared, session: session)
    return WarpWidgetHost.shared.composeJson(widget: CounterWarpWidget.shared, session: session)
}
