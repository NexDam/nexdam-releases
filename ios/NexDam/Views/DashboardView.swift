import SwiftUI

struct DashboardView: View {
    @EnvironmentObject var authVM: AuthViewModel
    @StateObject private var vm = DashboardViewModel()

    var body: some View {
        NavigationStack {
            ZStack {
                NexDamColors.background.ignoresSafeArea()

                Group {
                    if vm.isLoading {
                        VStack(spacing: 12) {
                            ProgressView().tint(NexDamColors.primary)
                            Text("Caricamento progetti…")
                                .font(.system(size: 13))
                                .foregroundColor(NexDamColors.muted)
                        }
                    } else if let err = vm.error {
                        VStack(spacing: 12) {
                            Text("⚠️").font(.system(size: 40))
                            Text(err)
                                .font(.system(size: 14))
                                .foregroundColor(NexDamColors.danger)
                                .multilineTextAlignment(.center)
                                .padding(.horizontal, 24)
                            Button("Riprova") {
                                loadData()
                            }
                            .foregroundColor(NexDamColors.primary)
                        }
                    } else {
                        projectsList
                    }
                }
            }
            .navigationTitle("NexDam")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: loadData) {
                        Image(systemName: "arrow.clockwise")
                            .foregroundColor(NexDamColors.muted)
                    }
                }
            }
        }
        .onAppear { loadData() }
    }

    private var projectsList: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                // Greeting
                if let profile = vm.profile {
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Ciao, \(profile.displayName) 👋")
                                .font(.system(size: 20, weight: .bold))
                                .foregroundColor(NexDamColors.onBg)
                            Text("\(vm.projects.count) progett\(vm.projects.count == 1 ? "o" : "i") attiv\(vm.projects.count == 1 ? "o" : "i")")
                                .font(.system(size: 13))
                                .foregroundColor(NexDamColors.muted)
                        }
                        Spacer()
                    }
                    .padding(.top, 20)
                }

                // Summary cards
                if !vm.projects.isEmpty {
                    let inProgress = vm.projects.filter { $0.status == "in_progress" }.count
                    let completed = vm.projects.filter { $0.status == "completed" }.count
                    let messages = vm.projects.reduce(0) { $0 + $1.messages.count }

                    HStack(spacing: 10) {
                        SummaryCardView("In corso", "\(inProgress)", NexDamColors.primary)
                        SummaryCardView("Completati", "\(completed)", NexDamColors.success)
                        SummaryCardView("Messaggi", "\(messages)", NexDamColors.warning)
                    }
                }

                if vm.projects.isEmpty {
                    VStack(spacing: 12) {
                        Spacer().frame(height: 40)
                        Text("📋").font(.system(size: 48))
                        Text("Nessun progetto")
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundColor(NexDamColors.onBg)
                        Text("I tuoi progetti appariranno qui")
                            .font(.system(size: 13))
                            .foregroundColor(NexDamColors.muted)
                    }
                } else {
                    Text("I TUOI PROGETTI")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(NexDamColors.muted)
                        .kerning(1.5)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.top, 4)

                    ForEach(vm.projects) { project in
                        NavigationLink(destination: ProjectDetailView(project: project)) {
                            ProjectCardView(project: project)
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 20)
        }
    }

    private func loadData() {
        guard let token = authVM.accessToken, let uid = authVM.userId else { return }
        vm.load(accessToken: token, userId: uid)
    }
}

struct SummaryCardView: View {
    let label: String
    let value: String
    let color: Color

    init(_ label: String, _ value: String, _ color: Color) {
        self.label = label; self.value = value; self.color = color
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(value).font(.system(size: 22, weight: .black)).foregroundColor(color)
            Text(label).font(.system(size: 11)).foregroundColor(color.opacity(0.8))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(12)
        .background(color.opacity(0.08))
        .cornerRadius(12)
    }
}

struct ProjectCardView: View {
    let project: Project

    private var statusColor: Color {
        switch project.status {
        case "in_progress": return NexDamColors.primary
        case "completed": return NexDamColors.success
        default: return NexDamColors.warning
        }
    }

    private var statusLabel: String {
        switch project.status {
        case "in_progress": return "In corso"
        case "completed": return "Completato"
        default: return "In attesa"
        }
    }

    var body: some View {
        HStack(spacing: 0) {
            // Left accent
            statusColor
                .frame(width: 3)
                .cornerRadius(14, corners: [.topLeft, .bottomLeft])

            VStack(alignment: .leading, spacing: 6) {
                HStack {
                    Text(project.title)
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(NexDamColors.onBg)
                    Spacer()
                    Text(statusLabel)
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(statusColor)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(statusColor.opacity(0.12))
                        .cornerRadius(6)
                }
                if let desc = project.description {
                    Text(desc)
                        .font(.system(size: 13))
                        .foregroundColor(NexDamColors.muted)
                        .lineLimit(2)
                }
                Divider().background(NexDamColors.divider).padding(.vertical, 4)
                HStack(spacing: 20) {
                    MiniStatView("message", "\(project.messages.count)", project.messages.isEmpty ? NexDamColors.muted : NexDamColors.primary)
                    MiniStatView("paperclip", "\(project.files.count)", NexDamColors.muted)
                    MiniStatView("doc.text", "\(project.invoices.count)", project.invoices.contains(where: { $0.status == "pending" }) ? NexDamColors.warning : NexDamColors.muted)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
        }
        .background(NexDamColors.surface)
        .cornerRadius(14)
    }
}

struct MiniStatView: View {
    let icon: String
    let value: String
    let color: Color

    init(_ icon: String, _ value: String, _ color: Color) {
        self.icon = icon; self.value = value; self.color = color
    }

    var body: some View {
        HStack(spacing: 4) {
            Image(systemName: icon).font(.system(size: 12)).foregroundColor(color)
            Text(value).font(.system(size: 12)).foregroundColor(color)
        }
    }
}

// Helper for corner radius on specific corners
extension View {
    func cornerRadius(_ radius: CGFloat, corners: UIRectCorner) -> some View {
        clipShape(RoundedCorner(radius: radius, corners: corners))
    }
}

struct RoundedCorner: Shape {
    var radius: CGFloat
    var corners: UIRectCorner
    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(roundedRect: rect, byRoundingCorners: corners, cornerRadii: CGSize(width: radius, height: radius))
        return Path(path.cgPath)
    }
}
