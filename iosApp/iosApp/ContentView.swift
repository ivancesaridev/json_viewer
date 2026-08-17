import SwiftUI
import UIKit
import ComposeApp

/// Hosts the shared Compose UI.
///
/// `MainViewController()` is the entry point exported from the Kotlin `ComposeApp` framework.
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard)
    }
}
