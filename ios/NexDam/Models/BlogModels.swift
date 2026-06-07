import Foundation

struct BlogPost: Codable, Identifiable {
    let id: String
    let slug: String
    let title: String
    let excerpt: String?
    let category: String?
    let coverUrl: String?
    let readMinutes: Int
    let publishedAt: String?
    let body: String?

    enum CodingKeys: String, CodingKey {
        case id, slug, title, excerpt, category, body
        case coverUrl = "cover_url"
        case readMinutes = "read_minutes"
        case publishedAt = "published_at"
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        id = try c.decode(String.self, forKey: .id)
        slug = try c.decode(String.self, forKey: .slug)
        title = try c.decode(String.self, forKey: .title)
        excerpt = try c.decodeIfPresent(String.self, forKey: .excerpt)
        category = try c.decodeIfPresent(String.self, forKey: .category)
        coverUrl = try c.decodeIfPresent(String.self, forKey: .coverUrl)
        readMinutes = (try? c.decode(Int.self, forKey: .readMinutes)) ?? 0
        publishedAt = try c.decodeIfPresent(String.self, forKey: .publishedAt)
        body = try c.decodeIfPresent(String.self, forKey: .body)
    }

    var formattedDate: String {
        guard let iso = publishedAt, iso.count >= 10 else { return "" }
        return String(iso.prefix(10))
    }

    var categoryEmoji: String {
        switch category {
        case "Linux & Server": return "🖥️"
        case "Cloudflare & Sicurezza": return "🛡️"
        case "Web Development": return "🌐"
        default: return "📄"
        }
    }
}

struct BlogListResponse: Codable {
    let posts: [BlogPost]
}

struct BlogDetailResponse: Codable {
    let post: BlogPost?
}

let blogCategories: [(value: String?, label: String)] = [
    (nil, "Tutti"),
    ("Linux & Server", "Linux & Server"),
    ("Cloudflare & Sicurezza", "Cloudflare & Sicurezza"),
    ("Web Development", "Web Development")
]
