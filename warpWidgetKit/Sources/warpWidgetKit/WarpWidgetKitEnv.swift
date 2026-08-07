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
            theme: Theme.resolve(from: context),
            // widgetRenderingMode is view-only (@Environment); not in timeline environmentVariants.
            renderingMode: .fullColor
        )
    }

    /// SwiftUI widget view path — live [ColorScheme] and [WidgetRenderingMode].
    public static func from(
        colorScheme: ColorScheme,
        family: Family,
        width: CGFloat? = nil,
        height: CGFloat? = nil,
        isPreview: Bool = false,
        widgetRenderingMode: WidgetRenderingMode? = nil
    ) -> WarpWidgetKitEnv {
        let size = defaultSize(for: family, width: width, height: height)
        let locale = Locale.current
        return WarpWidgetKitEnv(
            family: family,
            width: size.width,
            height: size.height,
            isPreview: isPreview,
            localeIdentifier: locale.identifier,
            layoutDirection: locale.language.characterDirection == .rightToLeft ? .rtl : .ltr,
            timeZoneIdentifier: TimeZone.current.identifier,
            displayScale: UIScreen.main.scale,
            fontScale: 1,
            theme: Theme(colorScheme: colorScheme),
            renderingMode: RenderingMode(widgetRenderingMode: widgetRenderingMode)
        )
    }

    /// Cold-start / AppIntent fallback when no [TimelineProvider.Context] is available.
    public static func placeholder(
        family: Family = .systemSmall,
        isPreview: Bool = false
    ) -> WarpWidgetKitEnv {
        let locale = Locale.current
        let size = defaultSize(for: family)
        return WarpWidgetKitEnv(
            family: family,
            width: size.width,
            height: size.height,
            isPreview: isPreview,
            localeIdentifier: locale.identifier,
            layoutDirection: locale.language.characterDirection == .rightToLeft ? .rtl : .ltr,
            timeZoneIdentifier: TimeZone.current.identifier,
            displayScale: UIScreen.main.scale,
            fontScale: 1,
            theme: Theme.resolveFromCurrentTraits(),
            renderingMode: .fullColor
        )
    }

    private static func defaultSize(
        for family: Family,
        width: CGFloat? = nil,
        height: CGFloat? = nil
    ) -> (width: CGFloat, height: CGFloat) {
        if let width, let height {
            return (width, height)
        }
        switch family {
        case .systemSmall:
            return (155, 155)
        case .systemMedium:
            return (329, 155)
        case .systemLarge:
            return (329, 345)
        case .systemExtraLarge:
            return (690, 345)
        case .unknown:
            return (329, 155)
        }
    }
}

public extension WarpWidgetKitEnv.Theme {
    init(colorScheme: ColorScheme) {
        switch colorScheme {
        case .dark:
            self = .dark
        case .light:
            self = .light
        @unknown default:
            self = .unspecified
        }
    }

    static func resolve(from context: TimelineProvider.Context) -> WarpWidgetKitEnv.Theme {
        // WidgetKit lists every colorScheme in environmentVariants (often [.light, .dark]).
        // Never pick schemes[0] — use the trait for *this* render pass (pre-render sets it per variant).
        resolveFromCurrentTraits()
    }

    static func resolveFromCurrentTraits() -> WarpWidgetKitEnv.Theme {
        switch UITraitCollection.current.userInterfaceStyle {
        case .dark:
            return .dark
        case .light:
            return .light
        default:
            return .unspecified
        }
    }
}

public extension WarpWidgetKitEnv.RenderingMode {
    /// Map SwiftUI [WidgetRenderingMode] — only valid inside a widget [View] (`@Environment`).
    init(widgetRenderingMode: WidgetRenderingMode?) {
        guard let widgetRenderingMode else {
            self = .fullColor
            return
        }
        if widgetRenderingMode == .fullColor {
            self = .fullColor
        } else if widgetRenderingMode == .accented {
            self = .accented
        } else if widgetRenderingMode == .vibrant {
            self = .vibrant
        } else {
            self = .fullColor
        }
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
