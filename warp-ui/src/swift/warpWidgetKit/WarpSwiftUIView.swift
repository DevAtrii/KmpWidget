import SwiftUI
import UIKit
import ObjectiveC

/// Kotlin-callable holder for a WARP SwiftUI tree (`@objc` for cinterop / Shared).
///
/// ### Construct from Kotlin
/// `warpRender` / `warpWidgetView` → this class (JSON + `useIntents` stored).
///
/// ### Construct from Swift (preferred for WidgetKit)
/// ```swift
/// WarpSwiftUIView(json: WarpWidgetView_iosKt.warpWidgetJson(node: node), useIntents: true)
///     .widgetRootView()
/// ```
/// Building in Swift keeps `widgetRootView()` visible (not limited to ObjC header).
///
/// ### useIntents
/// - `true` — WidgetKit; buttons use [WarpClickIntentRegistry]
/// - `false` — in-app preview; buttons use [WarpClickBridge]
@objcMembers
public class WarpSwiftUIView: NSObject {
    private let json: String
    private let useIntents: Bool

    public init(json: String, useIntents: Bool) {
        self.json = json
        self.useIntents = useIntents
    }

    /// Type-erased SwiftUI tree (generic hosting).
    public func makeView() -> AnyView {
        AnyView(WarpSwiftUIRootView(json: json, useIntents: useIntents))
    }

    /// Pure SwiftUI root for WidgetKit — **do not** wrap in `UIViewControllerRepresentable`.
    public func widgetRootView() -> WarpSwiftUIRootView {
        WarpSwiftUIRootView(json: json, useIntents: useIntents)
    }

    /// UIKit host keeping the holder’s `useIntents` flag. Not for home-screen widgets.
    public func makeWidgetViewController() -> UIViewController {
        UIHostingController(
            rootView: WarpSwiftUIRootView(json: json, useIntents: useIntents)
        )
    }

    /// In-app preview host — forces `useIntents: false` (bridge taps in-process).
    public func makePreviewViewController() -> UIViewController {
        UIHostingController(
            rootView: WarpSwiftUIRootView(json: json, useIntents: false)
        )
    }

    /// `UIView` for Compose `UIKitView` — hosting controller retained on the view.
    public func makePreviewView() -> UIView {
        let controller = makePreviewViewController()
        let view = controller.view!
        objc_setAssociatedObject(
            view,
            &PreviewHostAssociationKey,
            controller,
            .OBJC_ASSOCIATION_RETAIN_NONATOMIC
        )
        return view
    }
}

private var PreviewHostAssociationKey: UInt8 = 0
