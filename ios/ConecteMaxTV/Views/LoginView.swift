import SwiftUI

struct LoginView: View {
    @EnvironmentObject private var session: SessionViewModel
    @State private var document = ""
    @State private var password = ""
    @State private var showsInitialMessage = true
    @FocusState private var focusedField: Field?
    let initialMessage: String?

    private enum Field { case document, password }

    var body: some View {
        VStack(spacing: 0) {
            Image(systemName: "lock.fill")
                .font(.system(size: 34))
                .foregroundStyle(.white)
                .frame(width: 76, height: 76)
                .background(AppColors.darkBackground, in: Circle())
            Text("Conecte Max TV")
                .font(.system(size: 26, weight: .bold))
                .foregroundStyle(AppColors.darkBackground)
                .padding(.top, 20)
            Text("Entre com os dados da Central do Cliente")
                .font(.system(size: 14))
                .foregroundStyle(Color(hex: 0x6B7280))
                .padding(.top, 6)

            VStack(spacing: 14) {
                TextField("CPF ou CNPJ", text: $document)
                    .keyboardType(.numberPad)
                    .textContentType(.username)
                    .focused($focusedField, equals: .document)
                    .onChange(of: document) { newValue in
                        document = String(newValue.filter(\.isNumber).prefix(14))
                        session.loginMessage = nil
                        showsInitialMessage = false
                    }
                    .submitLabel(.next)
                    .onSubmit { focusedField = .password }
                SecureField("Senha", text: $password)
                    .textContentType(.password)
                    .focused($focusedField, equals: .password)
                    .onChange(of: password) { _ in
                        session.loginMessage = nil
                        showsInitialMessage = false
                    }
                    .submitLabel(.done)
                    .onSubmit(submit)
            }
            .textFieldStyle(ConecteTextFieldStyle())
            .disabled(session.isLoggingIn)
            .padding(.top, 28)

            if let message = session.loginMessage ?? (showsInitialMessage ? initialMessage : nil) {
                Text(message)
                    .font(.system(size: 13))
                    .foregroundStyle(.red)
                    .multilineTextAlignment(.center)
                    .padding(.top, 12)
            }

            Button(action: submit) {
                Group {
                    if session.isLoggingIn {
                        ProgressView().tint(.white)
                    } else {
                        Text("ENTRAR").fontWeight(.bold)
                    }
                }
                .frame(maxWidth: .infinity, minHeight: 52)
            }
            .buttonStyle(.borderedProminent)
            .disabled(session.isLoggingIn)
            .padding(.top, 22)
        }
        .frame(maxWidth: 440)
        .padding(.horizontal, 28)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(hex: 0xF3F5F8).ignoresSafeArea())
    }

    private func submit() {
        focusedField = nil
        Task {
            await session.login(document: document, password: password)
            if case .authenticated = session.state { password = "" }
        }
    }
}

private struct ConecteTextFieldStyle: TextFieldStyle {
    func _body(configuration: TextField<Self._Label>) -> some View {
        configuration
            .padding(.horizontal, 14)
            .frame(height: 54)
            .background(.white, in: RoundedRectangle(cornerRadius: 8))
            .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color(hex: 0x9CA3AF)))
    }
}
