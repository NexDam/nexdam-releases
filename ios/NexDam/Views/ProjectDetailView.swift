import SwiftUI

struct ProjectDetailView: View {
    let project: Project

    private var statusColor: Color {
        switch project.status {
        case "in_progress": return NexDamColors.primary
        case "completed": return NexDamColors.success
        default: return NexDamColors.warning
        }
    }

    var body: some View {
        ZStack {
            NexDamColors.background.ignoresSafeArea()
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    // Header
                    VStack(alignment: .leading, spacing: 6) {
                        HStack {
                            let label = project.status == "in_progress" ? "In corso" :
                                        project.status == "completed" ? "Completato" : "In attesa"
                            Text(label)
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(statusColor)
                                .padding(.horizontal, 8).padding(.vertical, 4)
                                .background(statusColor.opacity(0.12))
                                .cornerRadius(6)
                            Spacer()
                        }
                        if let desc = project.description {
                            Text(desc)
                                .font(.system(size: 14))
                                .foregroundColor(NexDamColors.muted)
                        }
                    }
                    .padding(16)
                    .background(NexDamColors.surface)
                    .cornerRadius(14)

                    // Messages
                    if !project.messages.isEmpty {
                        sectionTitle("Messaggi")
                        ForEach(project.messages) { msg in
                            HStack(alignment: .top, spacing: 10) {
                                Circle()
                                    .fill(msg.isAdmin ? NexDamColors.primary : NexDamColors.muted)
                                    .frame(width: 8, height: 8)
                                    .padding(.top, 5)
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(msg.isAdmin ? "NexDam" : "Tu")
                                        .font(.system(size: 11, weight: .bold))
                                        .foregroundColor(msg.isAdmin ? NexDamColors.primary : NexDamColors.muted)
                                    Text(msg.body ?? "")
                                        .font(.system(size: 13))
                                        .foregroundColor(NexDamColors.onBg)
                                }
                                Spacer()
                            }
                            .padding(12)
                            .background(NexDamColors.surface)
                            .cornerRadius(10)
                        }
                    }

                    // Files
                    if !project.files.isEmpty {
                        sectionTitle("File")
                        ForEach(project.files) { file in
                            HStack(spacing: 10) {
                                Image(systemName: "paperclip")
                                    .foregroundColor(NexDamColors.primary)
                                Text(file.name)
                                    .font(.system(size: 13))
                                    .foregroundColor(NexDamColors.onBg)
                                Spacer()
                                if let size = file.sizeLabel {
                                    Text(size)
                                        .font(.system(size: 11))
                                        .foregroundColor(NexDamColors.muted)
                                }
                            }
                            .padding(12)
                            .background(NexDamColors.surface)
                            .cornerRadius(10)
                        }
                    }

                    // Invoices
                    if !project.invoices.isEmpty {
                        sectionTitle("Fatture")
                        ForEach(project.invoices) { inv in
                            let isPending = inv.status == "pending"
                            HStack {
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(inv.description ?? "Fattura")
                                        .font(.system(size: 13))
                                        .foregroundColor(NexDamColors.onBg)
                                    if let due = inv.dueDate {
                                        Text("Scadenza: \(due.prefix(10))")
                                            .font(.system(size: 11))
                                            .foregroundColor(NexDamColors.muted)
                                    }
                                }
                                Spacer()
                                VStack(alignment: .trailing, spacing: 2) {
                                    Text("€\(String(format: "%.0f", inv.amount))")
                                        .font(.system(size: 14, weight: .bold))
                                        .foregroundColor(NexDamColors.onBg)
                                    Text(isPending ? "Da pagare" : "Pagata")
                                        .font(.system(size: 11, weight: .bold))
                                        .foregroundColor(isPending ? NexDamColors.warning : NexDamColors.success)
                                }
                            }
                            .padding(12)
                            .background(NexDamColors.surface)
                            .cornerRadius(10)
                        }
                    }
                }
                .padding(16)
            }
        }
        .navigationTitle(project.title)
        .navigationBarTitleDisplayMode(.large)
        .preferredColorScheme(.dark)
    }

    private func sectionTitle(_ title: String) -> some View {
        Text(title.uppercased())
            .font(.system(size: 11, weight: .bold))
            .foregroundColor(NexDamColors.muted)
            .kerning(1.5)
    }
}
