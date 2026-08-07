//
//  CounterWidgetBundle.swift
//  CounterWidget
//

import Shared
import SwiftUI
import WidgetKit
import warpWidgetKit

/// Extension `@main`. Wires Swift AppIntents ↔ shared [CounterWarpWidget].
///
/// 1. [WarpClickIntentRegistry.install(_:for:)] — per `WarpWidget.id` (styling in warpWidgetKit)
/// 2. [WarpWidgetHost.prepare] — kit fields → session (bridge installs inside Kotlin)
@main
struct CounterWidgetBundle: WidgetBundle {
    init() {
        if #available(iOS 17.0, *) {
            WarpClickIntentRegistry.install(
                CounterWidgetClickIntent.self,
                for: CounterWarpWidget.shared.id
            )
            // Future widgets in this extension:
            // WarpClickIntentRegistry.install(OtherClickIntent.self, for: OtherWarpWidget.shared.id)
        }
        let session = WarpWidgetHost.shared.iosSession(
            widget: CounterWarpWidget.shared,
            kitFields: WarpWidgetKitEnv.placeholder().asKitFields(
                appGroupId: CounterWarpWidget.shared.iosGroupId
            )
        )
        WarpWidgetHost.shared.prepare(widget: CounterWarpWidget.shared, session: session)
    }

    var body: some Widget {
        CounterHomeWidget()
    }
}
