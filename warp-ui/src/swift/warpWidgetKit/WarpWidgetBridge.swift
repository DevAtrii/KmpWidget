import Foundation
import UIKit
import SwiftUI
import WidgetKit

private let nodeJsonKey = "warp_node_json"
public let warpAppGroupId = "group.com.atriidev.kmpwidget"

/// Kotlin ↔ Swift bridge for WARP WidgetKit storage, preview, and timeline reload.
@objcMembers
public class WarpWidgetBridge: NSObject {
    public static let shared = WarpWidgetBridge()

    private var defaults: UserDefaults {
        UserDefaults(suiteName: warpAppGroupId) ?? .standard
    }

    private override init() {
        super.init()
    }

    public func publishNodeJson(_ json: String) {
        defaults.set(json, forKey: nodeJsonKey)
    }

    public func storedNodeJson() -> String {
        defaults.string(forKey: nodeJsonKey) ?? "{}"
    }

    public func reloadTimelines() {
        if #available(iOS 14.0, *) {
            WidgetCenter.shared.reloadAllTimelines()
        }
    }

    /// In-app SwiftUI preview hosted in UIKit — not Compose.
    public func makePreviewViewController() -> UIViewController {
        UIHostingController(
            rootView: WarpSwiftUIRootView(
                json: storedNodeJson(),
                useIntents: false
            )
        )
    }
}
