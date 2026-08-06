//
//  CounterWidgetBundle.swift
//  CounterWidget
//

import Shared
import SwiftUI
import WidgetKit

/// Extension `@main`. Wires Swift AppIntents ↔ Kotlin before any timeline/intent runs.
///
/// 1. [CounterWidgetClickSetup.install] — renderer buttons use extension `AppIntent`
/// 2. [CounterWidgetIosKt.prepareCounterWidgetHandlers] — cold-start Kotlin registry
@main
struct CounterWidgetBundle: WidgetBundle {
    init() {
        if #available(iOS 17.0, *) {
            CounterWidgetClickSetup.install()
        }
        CounterWidgetIosKt.prepareCounterWidgetHandlers()
    }

    var body: some Widget {
        CounterWidget()
    }
}
