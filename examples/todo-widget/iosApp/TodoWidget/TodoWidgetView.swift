import SharedLogic
import SwiftUI
import WidgetKit
import warpWidgetKit

/// Timeline / AppIntent fallback — no SwiftUI environment (trait-based theme only).
func composeWidgetJson(context: TimelineProviderContext) -> String {
    composeWidgetJson(kitEnv: WarpWidgetKitEnv.from(context: context))
}

/// Live render path — uses SwiftUI environment (updates when appearance / tint changes).
func composeWidgetJson(
    colorScheme: ColorScheme,
    widgetFamily: WidgetFamily,
    widgetRenderingMode: WidgetRenderingMode? = nil,
    displaySize: CGSize? = nil,
    isPreview: Bool = false
) -> String {
    composeWidgetJson(
        kitEnv: WarpWidgetKitEnv.from(
            colorScheme: colorScheme,
            family: WarpWidgetKitEnv.Family(widgetFamily: widgetFamily),
            width: displaySize?.width,
            height: displaySize?.height,
            isPreview: isPreview,
            widgetRenderingMode: widgetRenderingMode
        )
    )
}

func composeWidgetPlaceholderJson() -> String {
    composeWidgetJson(kitEnv: WarpWidgetKitEnv.placeholder())
}

private func composeWidgetJson(kitEnv: WarpWidgetKitEnv) -> String {
    let session = WarpWidgetHost.shared.iosSession(
        widget: TodoWarpWidget.shared,
        kitFields: kitEnv.asKitFields(
            appGroupId: TodoWarpWidget.shared.iosGroupId
        )
    )
    WarpWidgetHost.shared.prepare(widget: TodoWarpWidget.shared, session: session)
    return WarpWidgetHost.shared.composeJson(widget: TodoWarpWidget.shared, session: session)
}
