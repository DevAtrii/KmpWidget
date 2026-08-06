import Foundation
import WidgetKit

/// App Group id shared with Kotlin `APP_GROUP_ID` / `KmpDataStore` (UserDefaults suite).
public let warpAppGroupId = "group.com.atriidev.kmpwidget"

/// Thin ObjC wrapper around `WidgetCenter` for Kotlin `WidgetUpdater`.
///
/// Kotlin: `WarpWidgetBridge.shared().reloadTimelines()` after counter writes
/// so the home-screen timeline re-reads App Group state.
@objcMembers
public class WarpWidgetBridge: NSObject {
    public static let shared = WarpWidgetBridge()

    private override init() {
        super.init()
    }

    /// Reloads all WidgetKit timelines (safe to call from app or extension).
    public func reloadTimelines() {
        if #available(iOS 14.0, *) {
            WidgetCenter.shared.reloadAllTimelines()
        }
    }
}
