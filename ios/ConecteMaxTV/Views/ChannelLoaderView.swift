import SwiftUI

struct ChannelLoaderView: View {
    @StateObject private var model = ChannelViewModel()
    let profile: CustomerProfile

    var body: some View {
        switch model.state {
        case .loading:
            AppMessageView {
                ProgressView().tint(AppColors.watching)
                Text("Carregando canais…").foregroundStyle(Color(hex: 0x4B5563))
            }
            .task { await model.load() }
        case .loaded(let channels):
            HomeView(channels: channels, profile: profile)
        case .failed(let message):
            AppMessageView {
                Text("Não foi possível carregar os canais").fontWeight(.bold)
                Text(message).foregroundStyle(Color(hex: 0x6B7280)).multilineTextAlignment(.center)
                Button("Tentar novamente") { Task { await model.load() } }
                    .buttonStyle(.borderedProminent)
            }
        }
    }
}

private struct AppMessageView<Content: View>: View {
    let content: () -> Content

    init(@ViewBuilder content: @escaping () -> Content) {
        self.content = content
    }

    var body: some View {
        VStack(spacing: 16) { content() }
            .padding(.horizontal, 28)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .safeAreaInset(edge: .top, spacing: 0) { TopBar(onSearch: {}) }
            .safeAreaInset(edge: .bottom, spacing: 0) { BottomBar(onGrid: {}, onMenu: {}) }
    }
}
