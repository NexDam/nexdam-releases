import SwiftUI

struct NexDamTextField: View {
    @Binding var text: String
    let placeholder: String
    let systemIcon: String
    var keyboardType: UIKeyboardType = .default
    @FocusState private var focused: Bool

    var body: some View {
        HStack(spacing: 10) {
            Image(systemName: systemIcon)
                .font(.system(size: 16))
                .foregroundColor(NexDamColors.muted)
                .frame(width: 20)
            TextField(placeholder, text: $text)
                .keyboardType(keyboardType)
                .autocapitalization(.none)
                .disableAutocorrection(true)
                .foregroundColor(NexDamColors.onBg)
                .focused($focused)
        }
        .padding(.horizontal, 14)
        .frame(height: 50)
        .background(NexDamColors.surface)
        .overlay(
            RoundedRectangle(cornerRadius: 10)
                .stroke(focused ? NexDamColors.primary : NexDamColors.divider, lineWidth: 1)
        )
        .cornerRadius(10)
    }
}

struct NexDamSecureField: View {
    @Binding var text: String
    let placeholder: String
    let systemIcon: String
    @Binding var isVisible: Bool
    @FocusState private var focused: Bool

    var body: some View {
        HStack(spacing: 10) {
            Image(systemName: systemIcon)
                .font(.system(size: 16))
                .foregroundColor(NexDamColors.muted)
                .frame(width: 20)

            ZStack(alignment: .leading) {
                if isVisible {
                    TextField(placeholder, text: $text)
                        .autocapitalization(.none)
                        .disableAutocorrection(true)
                        .foregroundColor(NexDamColors.onBg)
                        .focused($focused)
                } else {
                    SecureField(placeholder, text: $text)
                        .foregroundColor(NexDamColors.onBg)
                        .focused($focused)
                }
            }

            Button(action: { isVisible.toggle() }) {
                Image(systemName: isVisible ? "eye.slash" : "eye")
                    .font(.system(size: 16))
                    .foregroundColor(NexDamColors.muted)
            }
        }
        .padding(.horizontal, 14)
        .frame(height: 50)
        .background(NexDamColors.surface)
        .overlay(
            RoundedRectangle(cornerRadius: 10)
                .stroke(focused ? NexDamColors.primary : NexDamColors.divider, lineWidth: 1)
        )
        .cornerRadius(10)
    }
}
