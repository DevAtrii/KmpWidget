# warpWidgetKit

SwiftUI + WidgetKit helpers for [WARP](../README.md) (`WarpSwiftUIRootView`, click bridges, AppIntent registry).

Consumed by:

1. **Kotlin** — via [spm4Kmp](https://spmforkmp.eu/) `localPackage` in `:warp-ui` (`exportToKotlin`)
2. **Xcode widget / app** — Swift Package dependency (`import warpWidgetKit`)

## Local (this monorepo)

Xcode already references this folder as a **local package** (`iosApp` → `../warpWidgetKit`).

```text
iosApp/
warpWidgetKit/          ← SPM root (Package.swift)
warp-ui/                ← KMP; depends on this package through spm4Kmp
```

## Remote (publish later)

1. Push this directory (or extract to its own git repo)
2. Tag a version (`1.0.0`)
3. In the consumer Xcode project, replace the local package with:

```text
https://github.com/<org>/warpWidgetKit.git
```

4. In `:warp-ui` `build.gradle.kts`, switch spm4Kmp from `localPackage` to `remotePackageVersion` / `remotePackageBranch` with the same URL

Keep **one** product name: `warpWidgetKit` — Kotlin `import warpWidgetKit.*` and Swift `import warpWidgetKit` stay stable.

## Widget extension clicks

Extension owns the `AppIntent` only; this package styles buttons.
Install **once per** `WarpWidget.id` (multiple widgets OK):

```swift
struct CounterClickIntent: WarpClickAppIntent { /* … */ }
struct WeatherClickIntent: WarpClickAppIntent { /* … */ }

// WidgetBundle.init:
WarpClickIntentRegistry.install(CounterClickIntent.self, for: CounterWarpWidget.shared.id)
WarpClickIntentRegistry.install(WeatherClickIntent.self, for: WeatherWarpWidget.shared.id)

// When rendering each timeline entry:
WarpSwiftUIRootView(json: json, useIntents: true, widgetId: CounterWarpWidget.shared.id)
```

Do **not** build styled `Button` views in the extension — `WarpSwiftUIRootView` applies chrome.

## Do not

- Copy these sources into the widget target (duplicate `WarpClickBridge` → broken clicks)
- Point `SWIFT_INCLUDE_PATHS` at Gradle `build/spmKmpPlugin/.../scratch`
- Re-style WARP buttons in the extension (use kit chrome; change styles in `WarpSwiftUIRenderer`)
