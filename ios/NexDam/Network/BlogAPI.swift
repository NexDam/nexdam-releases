import Foundation

enum BlogAPI {
    private static let base = "https://www.nexdam.it/api/blog"

    static func fetchPosts(category: String? = nil) async throws -> [BlogPost] {
        var endpoint = base
        if let category, !category.isEmpty {
            let encoded = category.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? category
            endpoint += "?category=\(encoded)"
        }
        let (data, _) = try await URLSession.shared.data(from: URL(string: endpoint)!)
        return try JSONDecoder().decode(BlogListResponse.self, from: data).posts
    }

    static func fetchPost(slug: String) async throws -> BlogPost? {
        let encoded = slug.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? slug
        let endpoint = "\(base)?slug=\(encoded)"
        let (data, _) = try await URLSession.shared.data(from: URL(string: endpoint)!)
        return try JSONDecoder().decode(BlogDetailResponse.self, from: data).post
    }
}
