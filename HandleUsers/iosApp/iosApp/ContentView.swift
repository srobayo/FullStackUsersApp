import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
        guard let apiBaseUrl = Bundle.main.object(forInfoDictionaryKey: "API_BASE_URL") as? String,
              !apiBaseUrl.isEmpty,
              !apiBaseUrl.hasPrefix("$(") else {
            fatalError("API_BASE_URL is missing from the application configuration")
        }

        return MainViewControllerKt.MainViewController(serverUrl: apiBaseUrl)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}
