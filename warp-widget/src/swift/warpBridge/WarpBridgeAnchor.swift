import Foundation
import warpWidgetKit

/// Keeps the spm4Kmp bridge package non-empty.
/// Real WidgetKit / SwiftUI types come from the `warpWidgetKit` SPM package.
@objcMembers
public class WarpBridgeAnchor: NSObject {
    public static let packageName = "warpWidgetKit"
}
