import SwiftUI

struct MainTabView: View {
    @EnvironmentObject var authVM: AuthViewModel

    var body: some View {
        TabView {
            DashboardView()
                .tabItem {
                    Label("Progetti", systemImage: "folder.fill")
                }

            BlogView()
                .tabItem {
                    Label("Blog", systemImage: "doc.text.fill")
                }

            ProfileView()
                .tabItem {
                    Label("Profilo", systemImage: "person.fill")
                }
        }
        .accentColor(NexDamColors.primary)
        .preferredColorScheme(.dark)
    }
}
