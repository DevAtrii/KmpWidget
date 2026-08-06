package com.atriidev.kmpwidget

/**
 * App Group id shared by the iOS app and widget extension.
 *
 * Must match:
 * - Xcode entitlements (`com.apple.security.application-groups`)
 * - Swift `warpAppGroupId` in `WarpWidgetBridge.swift`
 * - [KmpDataStore] `UserDefaults(suiteName:)`
 */
const val APP_GROUP_ID = "group.com.atriidev.kmpwidget"
