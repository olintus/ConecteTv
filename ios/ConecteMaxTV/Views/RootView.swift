import SwiftUI

struct RootView: View {
    @EnvironmentObject private var session: SessionViewModel

    var body: some View {
        Group {
            switch session.state {
            case .checking:
                SessionStatusView(message: "Verificando sua assinatura no SGP…", showsProgress: true)
            case .loggedOut(let message):
                LoginView(initialMessage: message)
            case .authenticated(let profile):
                ChannelLoaderView(profile: profile)
            case .blocked(let message):
                SessionStatusView(message: message, showsProgress: false) {
                    Button("Tentar novamente") { Task { await session.restoreSession() } }
                        .buttonStyle(.borderedProminent)
                    Button("Entrar com outra conta") { session.logout() }
                }
            }
        }
        .task { await session.restoreSession() }
    }
}

struct SessionStatusView<Actions: View>: View {
    let message: String
    let showsProgress: Bool
    let actions: () -> Actions

    init(message: String, showsProgress: Bool, @ViewBuilder actions: @escaping () -> Actions) {
        self.message = message
        self.showsProgress = showsProgress
        self.actions = actions
    }

    var body: some View {
        VStack(spacing: 0) {
            BrandLogo()
                .frame(width: 250, height: 141)
                .clipShape(RoundedRectangle(cornerRadius: 24))
            Text(message)
                .font(.system(size: 14))
                .foregroundStyle(Color(hex: 0x5B6472))
                .multilineTextAlignment(.center)
                .padding(.top, 14)
            if showsProgress {
                ProgressView().tint(AppColors.watching).padding(.top, 24)
            } else {
                VStack(spacing: 8) { actions() }.padding(.top, 24)
            }
        }
        .padding(.horizontal, 32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(hex: 0xF0F6FC).ignoresSafeArea())
    }
}

extension SessionStatusView where Actions == EmptyView {
    init(message: String, showsProgress: Bool) {
        self.init(message: message, showsProgress: showsProgress) { EmptyView() }
    }
}
