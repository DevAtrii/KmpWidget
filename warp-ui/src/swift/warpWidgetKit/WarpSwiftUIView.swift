import SwiftUI
import UIKit
import ObjectiveC

/// Kotlin-callable holder for a WARP SwiftUI tree.
@objcMembers
public class WarpSwiftUIView: NSObject {
    private let json: String
    private let useIntents: Bool

    public init(json: String, useIntents: Bool) {
        self.json = json
        self.useIntents = useIntents
    }

    public func makeView() -> AnyView {
        AnyView(WarpSwiftUIRootView(json: json, useIntents: useIntents))
    }

    /// UIKit host for in-app preview (`useIntents: false` → [WarpClickBridge]).
    public func makePreviewViewController() -> UIViewController {
        UIHostingController(
            rootView: WarpSwiftUIRootView(json: json, useIntents: false)
        )
    }

    /// UIView for Compose [UIKitView] interop — hosting controller retained on the view.
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
