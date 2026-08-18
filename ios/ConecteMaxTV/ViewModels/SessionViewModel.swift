import Foundation

@MainActor
final class SessionViewModel: ObservableObject {
    @Published private(set) var state: SessionState = .checking
    @Published var isLoggingIn = false
    @Published var loginMessage: String?

    private let centralClient = CentralClient()
    private let credentialStore = KeychainCredentialStore()

    func restoreSession() async {
        state = .checking
        guard let credentials = credentialStore.load() else {
            state = .loggedOut()
            return
        }
        applyRestorationResult(await centralClient.authenticate(
            document: credentials.document,
            password: credentials.password
        ))
    }

    func login(document: String, password: String) async {
        guard !isLoggingIn else { return }
        let digits = document.filter(\.isNumber)
        guard [11, 14].contains(digits.count) else {
            loginMessage = "Informe um CPF ou CNPJ válido."
            return
        }
        guard !password.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            loginMessage = "Informe a senha da Central do Cliente."
            return
        }

        isLoggingIn = true
        loginMessage = nil
        defer { isLoggingIn = false }
        let result = await centralClient.authenticate(document: digits, password: password)
        switch result {
        case .authorized(let profile):
            do {
                try credentialStore.save(SavedCredentials(document: digits, password: password))
                state = .authenticated(profile)
            } catch {
                loginMessage = "Não foi possível salvar a sessão com segurança."
            }
        case .invalidCredentials(let message), .notEntitled(let message), .unavailable(let message):
            loginMessage = message
        }
    }

    func logout() {
        credentialStore.clear()
        loginMessage = nil
        state = .loggedOut()
    }

    private func applyRestorationResult(_ result: AuthenticationResult) {
        switch result {
        case .authorized(let profile):
            state = .authenticated(profile)
        case .invalidCredentials:
            credentialStore.clear()
            state = .loggedOut(message: "Sua sessão expirou. Entre novamente.")
        case .notEntitled(let message), .unavailable(let message):
            state = .blocked(message: message)
        }
    }
}

