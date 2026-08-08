//
//  TodoWidgetBundle.swift
//  TodoWidget
//

import SharedLogic
import SwiftUI
import WidgetKit
import warpWidgetKit

/// Extension `@main`. Wires Swift AppIntents ↔ shared [TodoWarpWidget].
///
/// 1. [WarpClickIntentRegistry.install(_:for:)] — per `WarpWidget.id` (styling in warpWidgetKit)
/// 2. [WarpWidgetHost.prepare] — kit fields → session (bridge installs inside Kotlin)
@main
struct TodoWidgetBundle: WidgetBundle {
    init() {
        if #available(iOS 17.0, *) {
            WarpClickIntentRegistry.install(
                TodoWidgetClickIntent.self,
                for: TodoWarpWidget.shared.id
            )
            // Future widgets in this extension:
            // WarpClickIntentRegistry.install(OtherClickIntent.self, for: OtherWarpWidget.shared.id)
        }
        let session = WarpWidgetHost.shared.iosSession(
            widget: TodoWarpWidget.shared,
            kitFields: WarpWidgetKitEnv.placeholder().asKitFields(
                appGroupId: TodoWarpWidget.shared.iosGroupId
            )
        )
        WarpWidgetHost.shared.prepare(widget: TodoWarpWidget.shared, session: session)
    }

    var body: some Widget {
        TodoHomeWidget()
    }
}
