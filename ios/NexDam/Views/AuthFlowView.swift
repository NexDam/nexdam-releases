import SwiftUI

struct AuthFlowView: View {
    @EnvironmentObject var authVM: AuthViewModel
    @State private var showRegister = false

    var body: some View {
        if showRegister {
            RegisterView(onGoToLogin: { showRegister = false })
        } else {
            LoginView(onGoToRegister: { showRegister = true })
        }
    }
}
