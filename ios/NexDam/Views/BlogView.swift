import SwiftUI

struct BlogView: View {
    @StateObject private var vm = BlogViewModel()

    var body: some View {
        NavigationStack {
            ZStack {
                NexDamColors.background.ignoresSafeArea()
                VStack(spacing: 0) {
                    categoryFilters

                    if vm.isLoading {
                        Spacer()
                        ProgressView().tint(NexDamColors.primary)
                        Text("Caricamento articoli…")
                            .font(.system(size: 13))
                            .foregroundColor(NexDamColors.muted)
                            .padding(.top, 8)
                        Spacer()
                    } else if let err = vm.errorMessage {
                        Spacer()
                        VStack(spacing: 12) {
                            Text(err)
                                .font(.system(size: 14))
                                .foregroundColor(NexDamColors.danger)
                            Button("Riprova") { vm.loadPosts(category: vm.selectedCategory) }
                                .foregroundColor(NexDamColors.primary)
                        }
                        Spacer()
                    } else if vm.posts.isEmpty {
                        Spacer()
                        Text("Nessun articolo disponibile")
                            .font(.system(size: 14))
                            .foregroundColor(NexDamColors.muted)
                        Spacer()
                    } else {
                        ScrollView {
                            LazyVStack(spacing: 14) {
                                ForEach(vm.posts) { post in
                                    Button(action: { vm.openPost(slug: post.slug) }) {
                                        BlogPostCardView(post: post)
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                            .padding(16)
                        }
                    }
                }
            }
            .navigationTitle("Blog")
            .navigationBarTitleDisplayMode(.inline)
            .navigationDestination(isPresented: Binding(
                get: { vm.selectedPost != nil || vm.isLoadingDetail || vm.detailErrorMessage != nil },
                set: { if !$0 { vm.closePost() } }
            )) {
                BlogDetailView(vm: vm)
            }
        }
        .onAppear {
            if vm.posts.isEmpty { vm.loadPosts() }
        }
        .preferredColorScheme(.dark)
    }

    private var categoryFilters: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(blogCategories, id: \.label) { item in
                    let selected = vm.selectedCategory == item.value
                    Button(action: { vm.loadPosts(category: item.value) }) {
                        Text(item.label)
                            .font(.system(size: 13, weight: selected ? .semibold : .regular))
                            .foregroundColor(selected ? NexDamColors.background : NexDamColors.muted)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 8)
                            .background(selected ? NexDamColors.primary : NexDamColors.surface)
                            .cornerRadius(20)
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
        }
    }
}

struct BlogPostCardView: View {
    let post: BlogPost

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            ZStack {
                NexDamColors.primary.opacity(0.08)
                Text(post.categoryEmoji).font(.system(size: 36))
            }
            .frame(height: 120)

            VStack(alignment: .leading, spacing: 6) {
                HStack(spacing: 8) {
                    if let category = post.category {
                        Text(category)
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(NexDamColors.primary)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 3)
                            .background(NexDamColors.primary.opacity(0.12))
                            .cornerRadius(6)
                    }
                    Image(systemName: "clock")
                        .font(.system(size: 11))
                        .foregroundColor(NexDamColors.muted)
                    Text("\(post.readMinutes) min")
                        .font(.system(size: 12))
                        .foregroundColor(NexDamColors.muted)
                }

                Text(post.title)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(NexDamColors.onBg)
                    .lineLimit(2)

                if let excerpt = post.excerpt {
                    Text(excerpt)
                        .font(.system(size: 13))
                        .foregroundColor(NexDamColors.muted)
                        .lineLimit(3)
                        .lineSpacing(3)
                }

                HStack(spacing: 4) {
                    Text("Leggi l'articolo")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(NexDamColors.primary)
                    Text("→")
                        .font(.system(size: 13))
                        .foregroundColor(NexDamColors.primary)
                }
                .padding(.top, 4)
            }
            .padding(16)
        }
        .background(NexDamColors.surface)
        .cornerRadius(16)
    }
}

struct BlogDetailView: View {
    @ObservedObject var vm: BlogViewModel

    var body: some View {
        ZStack {
            NexDamColors.background.ignoresSafeArea()

            if vm.isLoadingDetail {
                ProgressView().tint(NexDamColors.primary)
            } else if let err = vm.detailErrorMessage {
                Text(err)
                    .font(.system(size: 14))
                    .foregroundColor(NexDamColors.danger)
            } else if let post = vm.selectedPost {
                ScrollView {
                    VStack(alignment: .leading, spacing: 14) {
                        ZStack {
                            NexDamColors.primary.opacity(0.08)
                            Text(post.categoryEmoji).font(.system(size: 48))
                        }
                        .frame(height: 180)
                        .cornerRadius(16)

                        HStack(spacing: 8) {
                            if let category = post.category {
                                Text(category)
                                    .font(.system(size: 11, weight: .bold))
                                    .foregroundColor(NexDamColors.primary)
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 3)
                                    .background(NexDamColors.primary.opacity(0.12))
                                    .cornerRadius(6)
                            }
                            Image(systemName: "clock")
                                .font(.system(size: 11))
                                .foregroundColor(NexDamColors.muted)
                            Text("\(post.readMinutes) min di lettura · \(post.formattedDate)")
                                .font(.system(size: 12))
                                .foregroundColor(NexDamColors.muted)
                        }

                        Text(post.title)
                            .font(.system(size: 24, weight: .heavy))
                            .foregroundColor(NexDamColors.onBg)

                        Divider().background(NexDamColors.divider)

                        ForEach(Array((post.body ?? post.excerpt ?? "").components(separatedBy: "\n").enumerated()), id: \.offset) { _, paragraph in
                            let trimmed = paragraph.trimmingCharacters(in: .whitespaces)
                            if !trimmed.isEmpty {
                                let isHeading = trimmed.hasPrefix("**") && trimmed.hasSuffix("**")
                                let cleaned = trimmed.trimmingCharacters(in: CharacterSet(charactersIn: "*"))
                                Text(cleaned)
                                    .font(.system(size: isHeading ? 18 : 15, weight: isHeading ? .bold : .regular))
                                    .foregroundColor(isHeading ? NexDamColors.onBg : NexDamColors.muted)
                                    .lineSpacing(6)
                            }
                        }
                    }
                    .padding(16)
                }
            }
        }
        .navigationTitle("Articolo")
        .navigationBarTitleDisplayMode(.inline)
        .preferredColorScheme(.dark)
    }
}
