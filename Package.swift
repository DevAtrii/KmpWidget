// swift-tools-version: 5.9
import PackageDescription

/// WARP WidgetKit SwiftUI surface.
///
/// **Local (this repo):** Xcode → local package `../warpWidgetKit`
/// **Remote (future):** replace with something like:
/// ```swift
/// .package(url: "https://github.com/DevAtrii/Warp.git", from: "1.0.0")
/// ```
let package = Package(
    name: "Warp",
    platforms: [
        .iOS(.v17)
    ],
    products: [
        .library(
            name: "warpWidgetKit",
            targets: ["warpWidgetKit"]
        )
    ],
    targets: [
        .target(
            name: "warpWidgetKit",
            path: "warpWidgetKit/Sources/warpWidgetKit"
        )
    ]
)