import SwiftUI
import GymKit

/// The iOS entry point. Everything on screen is the shared Compose UI from
/// app/src/commonMain; this file only puts it in a window.
@main
struct GymApp: App {
    var body: some Scene {
        WindowGroup {
            GymView()
                // Compose lays out around the notch, home indicator and keyboard
                // itself, exactly as it does edge-to-edge on Android.
                .ignoresSafeArea()
        }
    }
}

struct GymView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
