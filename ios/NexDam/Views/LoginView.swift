import SwiftUI

struct LoginView: View {
    @EnvironmentObject var authVM: AuthViewModel
    var onGoToRegister: () -> Void

    @State private var email = ""
    @State private var password = ""
    @State private var passwordVisible = false

    var body: some View {
        ZStack {
            NexDamColors.background.ignoresSafeArea()

            VStack(spacing: 0) {
                // Top accent bar
                NexDamColors.primary
                    .frame(height: 3)
                    .frame(maxWidth: .infinity)

                Spacer()

                VStack(spacing: 0) {
                    // Logo
                    ZStack {
                        RoundedRectangle(cornerRadius: 20)
                            .fill(NexDamColors.primary.opacity(0.12))
                            .frame(width: 72, height: 72)
                        Text("ND")
                            .font(.system(size: 26, weight: .black))
                            .foregroundColor(NexDamColors.primary)
                    }

                    Spacer().frame(height: 16)
                    Text("NexDam")
                        .font(.system(size: 28, weight: .black))
                        .foregroundColor(NexDamColors.onBg)
                    Text("CLIENT PORTAL")
                        .font(.system(size: 13, weight: .regular))
                        .foregroundColor(NexDamColors.muted)
                        .kerning(2)
                        .padding(.bottom, 40)

                    // Card
                    VStack(alignment: .leading, spacing: 0) {
                        Text("Accedi")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(NexDamColors.onBg)
                        Text("Inserisci le tue credenziali")
                            .font(.system(size: 13))
                            .foregroundColor(NexDamColors.muted)
                            .padding(.top, 4)
                            .padding(.bottom, 20)

                        NexDamTextField(text: $email, placeholder: "Email", systemIcon: "envelope", keyboardType: .emailAddress)

                        Spacer().frame(height: 12)

                        NexDamSecureField(text: $password, placeholder: "Password", systemIcon: "lock", isVisible: $passwordVisible)

                        if let err = authVM.errorMessage {
                            Text(err)
                                .font(.system(size: 13))
                                .foregroundColor(NexDamColors.danger)
                                .padding(.top, 10)
                        }

                        Spacer().frame(height: 20)

                        Button(action: { authVM.login(email: email, password: password) }) {
                            Group {
                                if authVM.isLoading {
                                    ProgressView().tint(.white)
                                } else {
                                    Text("Accedi").font(.system(size: 15, weight: .bold))
                                }
                            }
                            .frame(maxWidth: .infinity)
                            .frame(height: 50)
                            .background(NexDamColors.primary)
                            .foregroundColor(.white)
                            .cornerRadius(10)
                        }
                        .disabled(authVM.isLoading || email.isEmpty || password.isEmpty)
                    }
                    .padding(20)
                    .background(NexDamColors.surface)
                    .cornerRadius(16)

                    Spacer().frame(height: 20)

                    HStack(spacing: 0) {
                        Text("Non hai un account?")
                            .foregroundColor(NexDamColors.muted)
                            .font(.system(size: 14))
                        Button(action: onGoToRegister) {
                            Text("  Registrati")
                                .font(.system(size: 14, weight: .semibold))
                                .foregroundColor(NexDamColors.primary)
                        }
                    }
                }
                .padding(.horizontal, 28)

                Spacer()

                Text("© 2025 NexDam · Secure Web Infrastructure")
                    .font(.system(size: 11))
                    .foregroundColor(NexDamColors.muted.opacity(0.5))
                    .padding(.bottom, 16)
            }
        }
    }
}
