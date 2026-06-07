import SwiftUI
import WebKit

struct BlogView: View {
    var body: some View {
        NavigationStack {
            ZStack {
                NexDamColors.background.ignoresSafeArea()
                BlogWebView(url: URL(string: "https://www.nexdam.it/blog")!)
            }
            .navigationTitle("Blog")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

struct BlogWebView: UIViewRepresentable {
    let url: URL

    func makeCoordinator() -> Coordinator { Coordinator() }

    func makeUIView(context: Context) -> WKWebView {
        let config = WKWebViewConfiguration()
        let webView = WKWebView(frame: .zero, configuration: config)
        webView.navigationDelegate = context.coordinator
        webView.backgroundColor = UIColor(NexDamColors.background)
        webView.isOpaque = false
        webView.load(URLRequest(url: url))
        return webView
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {}

    class Coordinator: NSObject, WKNavigationDelegate {
        func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {}
    }
}
