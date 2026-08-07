import Foundation
import SwiftUI
import UIKit
import WidgetKit

/// WidgetKit → WARP env snapshot (no Shared import — avoids SPM ↔ KMP cycle).
///
/// Map to Shared via [asKitFields] → `WarpWidgetHost.iosSession(widget:kitFields:)`
/// (bridge installs inside Kotlin — no `installWarpWidgetKitBridge` in the extension):
/// ```swift
/// let session = WarpWidgetHost.shared.iosSession(
///     widget: myWidget,
///     kitFields: WarpWidgetKitEnv.from(context: context).asKitFields(
///         appGroupId: myWidget.iosGroupId
///     )
/// )
/// ```
/// See `WarpWidgetKitShared` + Kotlin `WarpWidgetKitMapping`.
public struct WarpWidgetKitEnv: Sendable, Equatable {
    public enum Family: String, Sendable {
        case systemSmall
        case systemMedium
        case systemLarge
        case systemExtraLarge
        case unknown
    }

    public enum Theme: String, Sendable {
        case light
        case dark
        case unspecified
    }

    public enum LayoutDirection: String, Sendable {
        case ltr
        case rtl
    }

    public enum RenderingMode: String, Sendable {
        case fullColor
        case accented
        case vibrant
    }

    public var family: Family
    public var width: CGFloat
    public var height: CGFloat
    public var isPreview: Bool
    public var localeIdentifier: String
    public var layoutDirection: LayoutDirection
    public var timeZoneIdentifier: String
    public var displayScale: CGFloat
    public var fontScale: CGFloat
    public var theme: Theme
    public var renderingMode: RenderingMode
    public var marginLeading: CGFloat
    public var marginTop: CGFloat
    public var marginTrailing: CGFloat
    public var marginBottom: CGFloat
    public var showsContainerBackground: Bool

    public init(
        family: Family,
        width: CGFloat,
        height: CGFloat,
        isPreview: Bool,
        localeIdentifier: String,
        layoutDirection: LayoutDirection,
        timeZoneIdentifier: String,
        displayScale: CGFloat,
        fontScale: CGFloat,
        theme: Theme,
        renderingMode: RenderingMode = .fullColor,
        marginLeading: CGFloat = 0,
        marginTop: CGFloat = 0,
        marginTrailing: CGFloat = 0,
        marginBottom: CGFloat = 0,
        showsContainerBackground: Bool = true
    ) {
        self.family = family
        self.width = width
        self.height = height
        self.isPreview = isPreview
        self.localeIdentifier = localeIdentifier
        self.layoutDirection = layoutDirection
        self.timeZoneIdentifier = timeZoneIdentifier
        self.displayScale = displayScale
        self.fontScale = fontScale
        self.theme = theme
        self.renderingMode = renderingMode
        self.marginLeading = marginLeading
        self.marginTop = marginTop
        self.marginTrailing = marginTrailing
        self.marginBottom = marginBottom
        self.showsContainerBackground = showsContainerBackground
    }

    /// Extract env from a timeline / snapshot / placeholder [TimelineProvider.Context].
    public static func from(context: TimelineProvider.Context) -> WarpWidgetKitEnv {
        let display = context.displaySize
        let locale = Locale.current
        let layout: LayoutDirection =
            locale.language.characterDirection == .rightToLeft ? .rtl : .ltr
        return WarpWidgetKitEnv(
            family: Family(widgetFamily: context.family),
            width: display.width,
            height: display.height,
            isPreview: context.isPreview,
            localeIdentifier: locale.identifier,
            layoutDirection: layout,
            timeZoneIdentifier: TimeZone.current.identifier,
            displayScale: UIScreen.main.scale,
            fontScale: 1,
            theme: .unspecified,
            renderingMode: .fullColor
        )
    }

    /// Cold-start / AppIntent fallback when no [TimelineProvider.Context] is available.
    public static func placeholder(
        family: Family = .systemSmall,
        isPreview: Bool = false
    ) -> WarpWidgetKitEnv {
        let locale = Locale.current
        return WarpWidgetKitEnv(
            family: family,
            width: family == .systemSmall ? 155 : 329,
            height: 155,
            isPreview: isPreview,
            localeIdentifier: locale.identifier,
            layoutDirection: locale.language.characterDirection == .rightToLeft ? .rtl : .ltr,
            timeZoneIdentifier: TimeZone.current.identifier,
            displayScale: UIScreen.main.scale,
            fontScale: 1,
            theme: .unspecified
        )
    }
}

public extension WarpWidgetKitEnv.Family {
    init(widgetFamily: WidgetFamily) {
        switch widgetFamily {
        case .systemSmall: self = .systemSmall
        case .systemMedium: self = .systemMedium
        case .systemLarge: self = .systemLarge
        case .systemExtraLarge: self = .systemExtraLarge
        default: self = .unknown
        }
    }
}
