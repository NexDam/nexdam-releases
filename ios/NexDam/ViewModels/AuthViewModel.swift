import Foundation
import SwiftUI

@MainActor
class AuthViewModel: ObservableObject {
    @Published var isLoggedIn = false
    @Published var isLoading = false
    @Published var errorMessage: String?

    private(set) var accessToken: String?
    private(set) var userId: String?

    // Persist session across launches
    private let tokenKey = "nexdam_access_token"
    private let userIdKey = "nexdam_user_id"

    init() {
        if let token = UserDefaults.standard.string(forKey: tokenKey),
           let uid = UserDefaults.standard.string(forKey: userIdKey) {
            accessToken = token
            userId = uid
            isLoggedIn = true
        }
    }

    func login(email: String, password: String) {
        Task {
            isLoading = true
            errorMessage = nil
            do {
                let response = try await SupabaseManager.signIn(email: email, password: password)
                accessToken = response.accessToken
                userId = response.userId
                UserDefaults.standard.set(response.accessToken, forKey: tokenKey)
                UserDefaults.standard.set(response.userId, forKey: userIdKey)
                isLoggedIn = true
            } catch {
                errorMessage = error.localizedDescription
            }
            isLoading = false
        }
    }

    func register(email: String, password: String, fullName: String, onSuccess: @escaping () -> Void) {
        Task {
            isLoading = true
            errorMessage = nil
            do {
                try await SupabaseManager.signUp(email: email, password: password, fullName: fullName)
                onSuccess()
            } catch {
                errorMessage = error.localizedDescription
            }
            isLoading = false
        }
    }

    func logout() {
        accessToken = nil
        userId = nil
        UserDefaults.standard.removeObject(forKey: tokenKey)
        UserDefaults.standard.removeObject(forKey: userIdKey)
        isLoggedIn = false
    }
}
