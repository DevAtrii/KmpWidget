import UIKit
import SwiftUI
import WidgetKit
import Shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    init() {
        WidgetCenterBridge.shared.reloadHandler = { kind in
            WidgetCenter.shared.reloadTimelines(ofKind: kind)
        }
    }

    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}
