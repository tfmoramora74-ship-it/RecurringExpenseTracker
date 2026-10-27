import UIKit
import SwiftUI
import App

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
                // Draw edge-to-edge; Compose applies the safe area insets itself
                // and also has its own keyboard handler.
                .ignoresSafeArea()
    }
}



