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
/// 1. [CounterWidgetClickSetup.install] — renderer buttons use extension `AppIntent`
/// 2. [WarpWidgetHost.prepare] with [WarpWidgetKitEnv.placeholder] session
@main
struct CounterWidgetBundle: WidgetBundle {
    init() {
        if #available(iOS 17.0, *) {
            CounterWidgetClickSetup.install()
        }
        WarpWidgetKitMappingKt.installWarpWidgetKitBridge()
        let env: WidgetEnvironment = WarpWidgetKitEnv.placeholder().makeEnvironment()
        let session = WarpWidgetHost.shared.iosSession(
            widget: CounterWarpWidget.shared,
            environment: env
        )
        WarpWidgetHost.shared.prepare(widget: CounterWarpWidget.shared, session: session)
    }

    var body: some Widget {
        CounterHomeWidget()
    }
}
