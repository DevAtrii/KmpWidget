import Foundation

/// Bridges `WarpWidgetKitEnv` → Shared/Kotlin session + environment **without**
/// importing Shared into this package (Shared already depends on warpWidgetKit via
/// spm4Kmp — a Shared import here would be a dependency cycle).
///
/// Kotlin `ensureWarpWidgetKitSharedInstalled()` wires the handlers once.
@objcMembers
public class WarpWidgetKitShared: NSObject {
    public static let shared = WarpWidgetKitShared()

    private var makeSessionHandler: ((NSDictionary) -> Any)?
    private var makeEnvironmentHandler: ((NSDictionary) -> Any)?

    private override init() {
        super.init()
    }

    public static func isInstalled() -> Bool {
        shared.makeSessionHandler != nil && shared.makeEnvironmentHandler != nil
    }

    /// Kotlin: `WarpWidgetKitShared.installMakeSession { dict in … }`
    public static func installMakeSession(_ handler: @escaping (NSDictionary) -> Any) {
        shared.makeSessionHandler = handler
    }

    /// Kotlin: `WarpWidgetKitShared.installMakeEnvironment { dict in … }`
    public static func installMakeEnvironment(_ handler: @escaping (NSDictionary) -> Any) {
        shared.makeEnvironmentHandler = handler
    }

    fileprivate func session(from fields: NSDictionary) -> Any {
        guard let handler = makeSessionHandler else {
            fatalError(
                """
                WarpWidgetKitShared not installed. Prefer \
                WarpWidgetHost.iosSession(widget:kitFields:) (auto-installs). \
                Or call WarpWidgetKitMappingKt.installWarpWidgetKitBridge() once.
                """
            )
        }
        return handler(fields)
    }

    fileprivate func environment(from fields: NSDictionary) -> Any {
        guard let handler = makeEnvironmentHandler else {
            fatalError(
                """
                WarpWidgetKitShared not installed. Prefer \
                WarpWidgetHost.iosSession(widget:kitFields:) (auto-installs). \
                Or call WarpWidgetKitMappingKt.installWarpWidgetKitBridge() once.
                """
            )
        }
        return handler(fields)
    }
}

public extension WarpWidgetKitEnv {
    /// Field bag for `WarpWidgetHost.iosSession(widget:kitFields:)`.
    ///
    /// Returns a Swift dictionary so it matches Kotlin `Map` export (`[AnyHashable: Any]`).
    /// Pass [appGroupId] from Shared `WarpWidget.iosGroupId`.
    func asKitFields(appGroupId: String) -> [AnyHashable: Any] {
        [
            "family": family.rawValue,
            "widthDp": NSNumber(value: Float(width)),
            "heightDp": NSNumber(value: Float(height)),
            "isPreview": NSNumber(value: isPreview),
            "locale": localeIdentifier,
            "layoutDirection": layoutDirection.rawValue,
            "timeZone": timeZoneIdentifier,
            "displayScale": NSNumber(value: Float(displayScale)),
            "fontScale": NSNumber(value: Float(fontScale)),
            "theme": theme.rawValue,
            "renderingMode": renderingMode.rawValue,
            "marginLeading": NSNumber(value: Float(marginLeading)),
            "marginTop": NSNumber(value: Float(marginTop)),
            "marginTrailing": NSNumber(value: Float(marginTrailing)),
            "marginBottom": NSNumber(value: Float(marginBottom)),
            "showsContainerBackground": NSNumber(value: showsContainerBackground),
            "appGroupId": appGroupId,
        ]
    }

    /// Field bag for environment-only mapping (no App Group).
    func asKitFields() -> [AnyHashable: Any] {
        asKitFields(appGroupId: "")
    }

    /**
     * Shared `WarpWidgetSession` via Kotlin bridge (requires bridge installed).
     *
     * Prefer `WarpWidgetHost.iosSession(widget:kitFields:)` — no manual bridge install:
     * ```swift
     * WarpWidgetHost.shared.iosSession(
     *     widget: CounterWarpWidget.shared,
     *     kitFields: WarpWidgetKitEnv.from(context: context).asKitFields(
     *         appGroupId: CounterWarpWidget.shared.iosGroupId
     *     )
     * )
     * ```
     */
    func makeSession<Session>(appGroupId: String) -> Session {
        let object = WarpWidgetKitShared.shared.session(
            from: asKitFields(appGroupId: appGroupId) as NSDictionary
        )
        guard let session = object as? Session else {
            fatalError("makeSession expected Shared.WarpWidgetSession, got \(type(of: object))")
        }
        return session
    }

    /**
     * Shared `WidgetEnvironment` via Kotlin mapping.
     *
     * ```swift
     * let env: WidgetEnvironment = WarpWidgetKitEnv.from(context: context).makeEnvironment()
     * ```
     */
    func makeEnvironment<Environment>() -> Environment {
        let object = WarpWidgetKitShared.shared.environment(
            from: asKitFields() as NSDictionary
        )
        guard let environment = object as? Environment else {
            fatalError("makeEnvironment expected Shared.WidgetEnvironment, got \(type(of: object))")
        }
        return environment
    }
}
