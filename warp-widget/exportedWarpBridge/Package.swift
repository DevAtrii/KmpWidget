
// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "exportedWarpBridge",
    platforms: [.iOS("17.0"),.macOS("10.13"),.tvOS("12.0"),.watchOS("4.0")],
    products: [
        .library(
            name: "exportedWarpBridge",
            type: .static,
            targets: ["exportedWarpBridge"])
    ],
    dependencies: [
        .package(path: "/Users/athargul/Coding_2025/App/KMP/Learning/KmpWidget/warpWidgetKit")
    ],
    targets: [
        .target(
            name: "exportedWarpBridge",
            dependencies: [
                .product(name: "warpWidgetKit", package: "warpWidgetKit")
            ],
            path: "Sources"
            
            
        )
        
    ]
)
        