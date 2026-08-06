package com.atriidev.warp_ui

/**
 * iOS-only native widget surface (SwiftUI via spm4Kmp `warpWidgetKit.WarpSwiftUIView`).
 *
 * On Android this type exists for common API shape but [warpRender] always throws.
 *
 * ### Swift APIs (on the real Swift class)
 * - `widgetRootView()` — WidgetKit (pure SwiftUI)
 * - `makePreviewView()` — Compose `UIKitView`
 * - `makePreviewViewController()` / `makeWidgetViewController()` — UIKit hosts
 */
expect class WarpSwiftUIView
