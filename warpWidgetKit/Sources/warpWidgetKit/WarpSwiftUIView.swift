import SwiftUI
import UIKit
import ObjectiveC

/// Kotlin-callable holder for a WARP SwiftUI tree (`@objc` for cinterop / Shared).
///
/// ### Construct from Kotlin
/// `warpRender` / `warpWidgetView` → this class (JSON + `useIntents` + `widgetId` stored).
///
/// ### Construct from Swift (preferred for WidgetKit)
/// ```swift
/// WarpSwiftUIRootView(
///     json: json,
///     useIntents: true,
///     widgetId: CounterWarpWidget.shared.id
/// )
/// ```
///
/// ### useIntents
/// - `true` — WidgetKit; buttons use [WarpClickIntentRegistry.install(_:for:)] + kit styling
/// - `false` — in-app preview; buttons use [WarpClickBridge]
@objcMembers
public class WarpSwiftUIView: NSObject {
    private let json: String
    private let useIntents: Bool
    private let widgetId: String

    /// Legacy / preview path (no widget-kind intent lookup).
    public convenience init(json: String, useIntents: Bool) {
        self.init(json: json, useIntents: useIntents, widgetId: "")
    }

    /// Home-screen path: [widgetId] must match [WarpClickIntentRegistry.install(_:for:)].
    public init(json: String, useIntents: Bool, widgetId: String) {
        self.json = json
        self.useIntents = useIntents
        self.widgetId = widgetId
        super.init()
    }

    /// Type-erased SwiftUI tree (generic hosting).
    public func makeView() -> AnyView {
        AnyView(WarpSwiftUIRootView(json: json, useIntents: useIntents, widgetId: widgetId))
    }

    /// Pure SwiftUI root for WidgetKit — **do not** wrap in `UIViewControllerRepresentable`.
    public func widgetRootView() -> WarpSwiftUIRootView {
        WarpSwiftUIRootView(json: json, useIntents: useIntents, widgetId: widgetId)
    }

    /// UIKit host keeping the holder’s `useIntents` flag. Not for home-screen widgets.
    public func makeWidgetViewController() -> UIViewController {
        UIHostingController(
            rootView: WarpSwiftUIRootView(json: json, useIntents: useIntents, widgetId: widgetId)
        )
    }

    /// In-app preview host — forces `useIntents: false` (bridge taps in-process).
    public func makePreviewViewController() -> UIViewController {
        UIHostingController(
            rootView: WarpSwiftUIRootView(json: json, useIntents: false, widgetId: widgetId)
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
