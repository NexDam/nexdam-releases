import SwiftUI

struct RegisterView: View {
    @EnvironmentObject var authVM: AuthViewModel
    var onGoToLogin: () -> Void

    @State private var fullName = ""
    @State private var email = ""
    @State private var password = ""
    @State private var passwordVisible = false
    @State private var showEmailConfirm = false

    var body: some View {
        ZStack {
            NexDamColors.background.ignoresSafeArea()

            if showEmailConfirm {
                emailConfirmView
            } else {
                registerFormView
            }
        }
    }

    private var emailConfirmView: some View {
        VStack(spacing: 0) {
            Spacer()
            VStack(spacing: 0) {
                ZStack {
                    RoundedRectangle(cornerRadius: 20)
                        .fill(NexDamColors.primary.opacity(0.12))
                        .frame(width: 80, height: 80)
                    Image(systemName: "envelope.fill")
                        .font(.system(size: 36))
                        .foregroundColor(NexDamColors.primary)
                }
                Spacer().frame(height: 24)
                Text("Controlla la tua email")
                    .font(.system(size: 22, weight: .bold))
                    .foregroundColor(NexDamColors.onBg)
                Spacer().frame(height: 12)
                Text("Abbiamo inviato un link di conferma a:\n\(email)\n\nClicca il link per attivare il tuo account, poi accedi.")
                    .font(.system(size: 14))
                    .foregroundColor(NexDamColors.muted)
                    .multilineTextAlignment(.center)
                    .lineSpacing(6)
                Spacer().frame(height: 32)
                Button(action: onGoToLogin) {
                    Text("Vai al Login")
                        .font(.system(size: 15, weight: .bold))
                        .frame(maxWidth: .infinity)
                        .frame(height: 50)
                        .background(NexDamColors.primary)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }
            }
            .padding(.horizontal, 32)
            Spacer()
        }
    }

    private var registerFormView: some View {
        ScrollView {
            VStack(spacing: 0) {
                Spacer().frame(height: 60)

                Text("Crea account")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(NexDamColors.onBg)
                Text("NexDam Client Portal")
                    .font(.system(size: 14))
                    .foregroundColor(NexDamColors.muted)
                    .padding(.bottom, 32)

                NexDamTextField(text: $fullName, placeholder: "Nome completo", systemIcon: "person")
                Spacer().frame(height: 12)
                NexDamTextField(text: $email, placeholder: "Email", systemIcon: "envelope", keyboardType: .emailAddress)
                Spacer().frame(height: 12)
                NexDamSecureField(text: $password, placeholder: "Password", systemIcon: "lock", isVisible: $passwordVisible)

                if let err = authVM.errorMessage {
                    Text(err)
                        .font(.system(size: 13))
                        .foregroundColor(NexDamColors.danger)
                        .padding(.top, 8)
                }

                Spacer().frame(height: 24)

                Button(action: {
                    authVM.register(email: email, password: password, fullName: fullName) {
                        showEmailConfirm = true
                    }
                }) {
                    Group {
                        if authVM.isLoading {
                            ProgressView().tint(.white)
                        } else {
                            Text("Registrati").font(.system(size: 15, weight: .bold))
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 50)
                    .background(NexDamColors.primary)
                    .foregroundColor(.white)
                    .cornerRadius(10)
                }
                .disabled(authVM.isLoading)

                Spacer().frame(height: 16)

                Button(action: onGoToLogin) {
                    Text("Hai già un account? Accedi")
                        .font(.system(size: 14))
                        .foregroundColor(NexDamColors.primary)
                }

                Spacer().frame(height: 40)
            }
            .padding(.horizontal, 24)
        }
    }
}
