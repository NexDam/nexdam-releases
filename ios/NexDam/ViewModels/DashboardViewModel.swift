import Foundation
import SwiftUI

@MainActor
class DashboardViewModel: ObservableObject {
    @Published var projects: [Project] = []
    @Published var profile: Profile?
    @Published var isLoading = false
    @Published var error: String?

    func load(accessToken: String, userId: String) {
        Task {
            isLoading = true
            error = nil
            do {
                async let projs = SupabaseManager.fetchProjects(accessToken: accessToken)
                async let prof = SupabaseManager.fetchProfile(userId: userId, accessToken: accessToken)
                projects = try await projs
                profile = try? await prof
            } catch {
                self.error = error.localizedDescription
            }
            isLoading = false
        }
    }
}
