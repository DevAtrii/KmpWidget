package com.atriidev.warp_ui

import kotlinx.cinterop.ExperimentalForeignApi

/**
 * iOS actual: typealias to the Swift `@objc` class `warpWidgetKit.WarpSwiftUIView`
 * (compiled by spm4Kmp into the KMP framework).
 *
 * Kotlin can construct/return this type. Swift WidgetKit hosts should still build
 * `WarpSwiftUIView` in Swift from [warpWidgetJson] so SwiftUI APIs
 * (`widgetRootView()`) stay visible.
 */
@OptIn(ExperimentalForeignApi::class)
actual typealias WarpSwiftUIView = warpWidgetKit.WarpSwiftUIView
