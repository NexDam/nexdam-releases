import Foundation

struct Project: Codable, Identifiable {
    let id: String
    let title: String
    let description: String?
    let status: String
    let createdAt: String?
    let messages: [ProjectMessage]
    let files: [ProjectFile]
    let invoices: [Invoice]

    enum CodingKeys: String, CodingKey {
        case id, title, description, status
        case createdAt = "created_at"
        case messages = "project_messages"
        case files = "project_files"
        case invoices
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        id = try c.decode(String.self, forKey: .id)
        title = try c.decode(String.self, forKey: .title)
        description = try c.decodeIfPresent(String.self, forKey: .description)
        status = (try? c.decode(String.self, forKey: .status)) ?? "waiting"
        createdAt = try c.decodeIfPresent(String.self, forKey: .createdAt)
        messages = (try? c.decode([ProjectMessage].self, forKey: .messages)) ?? []
        files = (try? c.decode([ProjectFile].self, forKey: .files)) ?? []
        invoices = (try? c.decode([Invoice].self, forKey: .invoices)) ?? []
    }
}

struct ProjectMessage: Codable, Identifiable {
    let id: String
    let body: String?
    let isAdmin: Bool
    let createdAt: String?

    enum CodingKeys: String, CodingKey {
        case id, body
        case isAdmin = "is_admin"
        case createdAt = "created_at"
    }
}

struct ProjectFile: Codable, Identifiable {
    let id: String
    let name: String
    let url: String?
    let sizeLabel: String?
    let uploadedByAdmin: Bool

    enum CodingKeys: String, CodingKey {
        case id, name, url
        case sizeLabel = "size_label"
        case uploadedByAdmin = "uploaded_by_admin"
    }
}

struct Invoice: Codable, Identifiable {
    let id: String
    let amount: Double
    let currency: String
    let status: String
    let description: String?
    let dueDate: String?

    enum CodingKeys: String, CodingKey {
        case id, amount, currency, status, description
        case dueDate = "due_date"
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        id = try c.decode(String.self, forKey: .id)
        amount = try c.decode(Double.self, forKey: .amount)
        currency = (try? c.decode(String.self, forKey: .currency)) ?? "EUR"
        status = (try? c.decode(String.self, forKey: .status)) ?? "pending"
        description = try c.decodeIfPresent(String.self, forKey: .description)
        dueDate = try c.decodeIfPresent(String.self, forKey: .dueDate)
    }
}

struct Profile: Codable, Identifiable {
    let id: String
    let username: String?
    let fullName: String?
    let company: String?
    let phone: String?

    enum CodingKeys: String, CodingKey {
        case id, username, company, phone
        case fullName = "full_name"
    }

    var displayName: String { fullName ?? username ?? "Client" }
    var initials: String {
        let parts = displayName.split(separator: " ").prefix(2)
        return parts.compactMap { $0.first.map(String.init) }.joined().uppercased().ifEmpty("ND")
    }
}

private extension String {
    func ifEmpty(_ fallback: String) -> String { isEmpty ? fallback : self }
}
