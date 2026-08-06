import Foundation

/// ObjC singleton bridging SwiftUI / AppIntent taps into Kotlin `WarpClicksRegistry`.
///
/// ### Kotlin installs
/// `registerWarpClicks` / `warpRender` → `WarpIosBridge.installClickHandler()`
/// → [setHandler] with a closure that calls `WarpClicksRegistry.dispatch`.
///
/// ### Also
/// [setPrepareHandler] — cold-start re-register (e.g. `prepareCounterWidgetHandlers`)
/// before the first [perform] when the extension process just launched.
///
/// ### Warning
/// One instance per process. Compiling this file into both Shared **and** the widget
/// extension creates two singletons → clicks no-op. Import the prebuilt
/// `warpWidgetKit` module; do not copy sources into the extension.
@objcMembers
public class WarpClickBridge: NSObject {
    public static let shared = WarpClickBridge()

    private var handler: ((String, String) -> Void)?
    private var prepareHandler: (() -> Void)?

    private override init() {
        super.init()
    }

    /// Kotlin click dispatch: `(actionId, parametersJson) → Void`.
    public func setHandler(_ handler: @escaping (String, String) -> Void) {
        self.handler = handler
    }

    /// Optional warm-up before [perform] (register handlers on cold start).
    public func setPrepareHandler(_ handler: @escaping () -> Void) {
        prepareHandler = handler
    }

    public func prepareIfNeeded() {
        prepareHandler?()
    }

    /// Invoked from in-app `Button` closures (and legacy paths).
    /// Widget home-screen taps should go through an extension `AppIntent` → Kotlin
    /// `dispatchCounterWidgetClick` instead.
    public func perform(actionId: String, parametersJson: String) {
        prepareIfNeeded()
        handler?(actionId, parametersJson)
    }
}
