package com.atriidev.warp_ui

import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Full-screen UIKit host for an in-app preview (`useIntents: false` path).
 *
 * Swift name: `makePreviewViewController()` on [WarpSwiftUIView].
 */
@OptIn(ExperimentalForeignApi::class)
fun WarpSwiftUIView.previewViewController() = makePreviewViewController()

/**
 * UIKit host with `useIntents` from the holder (often `true`).
 *
 * **Not for WidgetKit** — home-screen widgets must use pure SwiftUI
 * (`widgetRootView()`), not `UIViewControllerRepresentable`.
 */
@OptIn(ExperimentalForeignApi::class)
fun WarpSwiftUIView.widgetViewController() = makeWidgetViewController()

/**
 * `UIView` for Compose Multiplatform [androidx.compose.ui.viewinterop.UIKitView].
 *
 * Retains the hosting controller on the view (see Swift `makePreviewView()`).
 */
@OptIn(ExperimentalForeignApi::class)
fun WarpSwiftUIView.previewView() = makePreviewView()

/** Convenience: [previewViewController] from an existing holder. */
@OptIn(ExperimentalForeignApi::class)
fun warpWidgetPreviewViewController(holder: WarpSwiftUIView) = holder.previewViewController()
