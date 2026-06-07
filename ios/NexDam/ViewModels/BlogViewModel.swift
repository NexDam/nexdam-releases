import Foundation
import SwiftUI

@MainActor
class BlogViewModel: ObservableObject {
    @Published var posts: [BlogPost] = []
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var selectedCategory: String?

    @Published var selectedPost: BlogPost?
    @Published var isLoadingDetail = false
    @Published var detailErrorMessage: String?

    func loadPosts(category: String? = nil) {
        selectedCategory = category
        Task {
            isLoading = true
            errorMessage = nil
            do {
                posts = try await BlogAPI.fetchPosts(category: category)
            } catch {
                errorMessage = "Impossibile caricare gli articoli"
            }
            isLoading = false
        }
    }

    func openPost(slug: String) {
        Task {
            isLoadingDetail = true
            detailErrorMessage = nil
            selectedPost = nil
            do {
                if let post = try await BlogAPI.fetchPost(slug: slug) {
                    selectedPost = post
                } else {
                    detailErrorMessage = "Articolo non trovato"
                }
            } catch {
                detailErrorMessage = "Impossibile caricare l'articolo"
            }
            isLoadingDetail = false
        }
    }

    func closePost() {
        selectedPost = nil
        detailErrorMessage = nil
    }
}
