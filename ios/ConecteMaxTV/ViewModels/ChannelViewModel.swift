import Foundation

@MainActor
final class ChannelViewModel: ObservableObject {
    @Published private(set) var state: ChannelLoadState = .loading
    private let service = PlaylistService()

    func load() async {
        state = .loading
        do {
            let channels = try await service.loadChannels()
            state = channels.isEmpty
                ? .failed("A playlist não contém canais válidos.")
                : .loaded(channels)
        } catch {
            state = .failed(error.localizedDescription.isEmpty
                ? "Não foi possível carregar a lista de canais."
                : error.localizedDescription)
        }
    }
}

