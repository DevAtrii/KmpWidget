import Foundation
import WidgetKit

enum AppGroupConfig {
    static let id = "group.com.atriidev.kmpwidget"
    static let counterKey = "counter"
    static let widgetKind = "CounterWidget"
}

enum CounterStore {
    static func readCounter() -> String {
        guard let defaults = UserDefaults(suiteName: AppGroupConfig.id) else {
            return "0"
        }
        return defaults.string(forKey: AppGroupConfig.counterKey) ?? "0"
    }

    static func mutate(by delta: Int) {
        guard let defaults = UserDefaults(suiteName: AppGroupConfig.id) else {
            return
        }

        let current = Int(defaults.string(forKey: AppGroupConfig.counterKey) ?? "0") ?? 0
        defaults.set("\(current + delta)", forKey: AppGroupConfig.counterKey)
        defaults.synchronize()

        WidgetCenter.shared.reloadTimelines(ofKind: AppGroupConfig.widgetKind)
    }
}
