import SwiftUI
import UIKit

/// `UIViewControllerRepresentable` for **in-app** previews only.
///
/// Embeds a UIKit controller from Kotlin (`makePreviewViewController()`, etc.).
///
/// **Do not use in WidgetKit** — home-screen widgets require pure SwiftUI
/// (`WarpSwiftUIRootView` / `widgetRootView()`). Representables show a placeholder icon.
public struct WarpUiHostView: UIViewControllerRepresentable {
    private let controller: UIViewController

    public init(controller: UIViewController) {
        self.controller = controller
    }

    public func makeUIViewController(context: Context) -> UIViewController {
        controller
    }

    public func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
