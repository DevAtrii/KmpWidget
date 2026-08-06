import Foundation

/// Routes SwiftUI / AppIntent taps back into Kotlin [WarpClicksRegistry].
@objcMembers
public class WarpClickBridge: NSObject {
    public static let shared = WarpClickBridge()

    private var handler: ((String, String) -> Void)?
    private var prepareHandler: (() -> Void)?

    private override init() {
        super.init()
    }

    public func setHandler(_ handler: @escaping (String, String) -> Void) {
        self.handler = handler
    }

    public func setPrepareHandler(_ handler: @escaping () -> Void) {
        prepareHandler = handler
    }

    public func prepareIfNeeded() {
        prepareHandler?()
    }

    public func perform(actionId: String, parametersJson: String) {
        prepareIfNeeded()
        handler?(actionId, parametersJson)
    }
}
