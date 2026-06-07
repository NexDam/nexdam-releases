import SwiftUI

struct ProfileView: View {
    @EnvironmentObject var authVM: AuthViewModel

    var body: some View {
        NavigationStack {
            ZStack {
                NexDamColors.background.ignoresSafeArea()
                VStack(spacing: 24) {
                    Spacer()

                    // Avatar
                    ZStack {
                        Circle()
                            .fill(NexDamColors.primary.opacity(0.15))
                            .frame(width: 80, height: 80)
                        Text("ND")
                            .font(.system(size: 28, weight: .black))
                            .foregroundColor(NexDamColors.primary)
                    }

                    Text("Il mio profilo")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(NexDamColors.onBg)

                    Spacer()

                    Button(action: { authVM.logout() }) {
                        HStack(spacing: 8) {
                            Image(systemName: "rectangle.portrait.and.arrow.right")
                            Text("Esci")
                                .font(.system(size: 15, weight: .bold))
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 50)
                        .background(NexDamColors.danger.opacity(0.15))
                        .foregroundColor(NexDamColors.danger)
                        .cornerRadius(10)
                    }
                    .padding(.horizontal, 24)
                    .padding(.bottom, 32)
                }
            }
            .navigationTitle("Profilo")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}
