import Foundation

enum SupabaseManager {
    static let url = "https://ggzuryrpwygedwpnzrjj.supabase.co"
    static let anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdnenVyeXJwd3lnZWR3cG56cmpqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODAxNDQ2NjgsImV4cCI6MjA5NTcyMDY2OH0.Y-SvKM67TX6UbyFgLOSfZBlqTdy0WzsT1xNWcCHnX28"

    // MARK: - Auth

    static func signIn(email: String, password: String) async throws -> AuthResponse {
        let endpoint = "\(url)/auth/v1/token?grant_type=password"
        var request = URLRequest(url: URL(string: endpoint)!)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue(anonKey, forHTTPHeaderField: "apikey")
        request.httpBody = try JSONEncoder().encode(["email": email, "password": password])
        let (data, response) = try await URLSession.shared.data(for: request)
        guard let http = response as? HTTPURLResponse, (200..<300).contains(http.statusCode) else {
            let err = try? JSONDecoder().decode(SupabaseError.self, from: data)
            throw NSError(domain: "Supabase", code: 0, userInfo: [NSLocalizedDescriptionKey: err?.message ?? "Credenziali non valide"])
        }
        return try JSONDecoder().decode(AuthResponse.self, from: data)
    }

    static func signUp(email: String, password: String, fullName: String) async throws {
        let endpoint = "\(url)/auth/v1/signup"
        var request = URLRequest(url: URL(string: endpoint)!)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue(anonKey, forHTTPHeaderField: "apikey")
        let body: [String: Any] = [
            "email": email,
            "password": password,
            "data": ["full_name": fullName]
        ]
        request.httpBody = try JSONSerialization.data(withJSONObject: body)
        let (data, response) = try await URLSession.shared.data(for: request)
        guard let http = response as? HTTPURLResponse, (200..<300).contains(http.statusCode) else {
            let err = try? JSONDecoder().decode(SupabaseError.self, from: data)
            throw NSError(domain: "Supabase", code: 0, userInfo: [NSLocalizedDescriptionKey: err?.message ?? "Registrazione fallita"])
        }
    }

    // MARK: - Projects

    static func fetchProjects(accessToken: String) async throws -> [Project] {
        let endpoint = "\(url)/rest/v1/projects?select=*,project_messages(*),project_files(*),invoices(*)"
        var request = URLRequest(url: URL(string: endpoint)!)
        request.setValue(anonKey, forHTTPHeaderField: "apikey")
        request.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        let (data, _) = try await URLSession.shared.data(for: request)
        return try JSONDecoder().decode([Project].self, from: data)
    }

    // MARK: - Profile

    static func fetchProfile(userId: String, accessToken: String) async throws -> Profile {
        let endpoint = "\(url)/rest/v1/profiles?id=eq.\(userId)&select=*"
        var request = URLRequest(url: URL(string: endpoint)!)
        request.setValue(anonKey, forHTTPHeaderField: "apikey")
        request.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        let (data, _) = try await URLSession.shared.data(for: request)
        let profiles = try JSONDecoder().decode([Profile].self, from: data)
        guard let profile = profiles.first else {
            throw NSError(domain: "Supabase", code: 0, userInfo: [NSLocalizedDescriptionKey: "Profilo non trovato"])
        }
        return profile
    }
}

// MARK: - Response models

struct AuthResponse: Codable {
    let accessToken: String
    let userId: String

    enum CodingKeys: String, CodingKey {
        case accessToken = "access_token"
        case userId = "user"
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        accessToken = try container.decode(String.self, forKey: .accessToken)
        let userContainer = try container.nestedContainer(keyedBy: UserCodingKeys.self, forKey: .userId)
        userId = try userContainer.decode(String.self, forKey: .id)
    }

    private enum UserCodingKeys: String, CodingKey { case id }
}

struct SupabaseError: Codable {
    let message: String
}
