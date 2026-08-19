import AVKit
import SwiftUI

struct PlayerView: View {
    @Environment(\.scenePhase) private var scenePhase
    @ObservedObject var model: PlayerViewModel
    let streamURL: URL
    let backgroundPlaybackEnabled: Bool
    let isFullscreen: Bool
    let onFullscreen: () -> Void

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            PlayerController(
                player: model.player,
                pictureInPictureEnabled: backgroundPlaybackEnabled
            )
            Button(action: onFullscreen) {
                Image(systemName: isFullscreen ? "arrow.down.right.and.arrow.up.left" : "arrow.up.left.and.arrow.down.right")
                    .font(.system(size: 23, weight: .semibold))
                    .foregroundStyle(.white)
                    .frame(width: 48, height: 48)
                    .background(.black.opacity(0.58), in: Circle())
            }
            .padding(12)
        }
        .background(.black)
        .onAppear {
            model.configure(url: streamURL, backgroundPlaybackEnabled: backgroundPlaybackEnabled)
            model.play()
        }
        .onChange(of: streamURL) { model.configure(url: $0, backgroundPlaybackEnabled: backgroundPlaybackEnabled) }
        .onChange(of: backgroundPlaybackEnabled) { model.setBackgroundPlaybackEnabled($0) }
        .onChange(of: scenePhase) { phase in
            if phase == .active { model.play() }
            if phase != .active, !backgroundPlaybackEnabled { model.pause() }
        }
        .onDisappear { model.pause() }
    }
}

@MainActor
final class PlayerViewModel: ObservableObject {
    let player = AVPlayer()
    private var currentURL: URL?

    func configure(url: URL, backgroundPlaybackEnabled: Bool) {
        setBackgroundPlaybackEnabled(backgroundPlaybackEnabled)
        guard currentURL != url else { return }
        currentURL = url
        player.replaceCurrentItem(with: AVPlayerItem(url: url))
        player.play()
    }

    func setBackgroundPlaybackEnabled(_ enabled: Bool) {
        let session = AVAudioSession.sharedInstance()
        if enabled {
            try? session.setCategory(.playback, mode: .moviePlayback)
        } else {
            try? session.setCategory(.ambient, mode: .default)
        }
        try? session.setActive(true)
    }

    func play() { player.play() }
    func pause() { player.pause() }

    deinit { player.pause() }
}

private struct PlayerController: UIViewControllerRepresentable {
    let player: AVPlayer
    let pictureInPictureEnabled: Bool

    func makeUIViewController(context: Context) -> AVPlayerViewController {
        let controller = AVPlayerViewController()
        controller.player = player
        controller.showsPlaybackControls = false
        controller.videoGravity = .resizeAspect
        controller.allowsPictureInPicturePlayback = pictureInPictureEnabled
        controller.canStartPictureInPictureAutomaticallyFromInline = pictureInPictureEnabled
        return controller
    }

    func updateUIViewController(_ controller: AVPlayerViewController, context: Context) {
        controller.player = player
        controller.allowsPictureInPicturePlayback = pictureInPictureEnabled
        controller.canStartPictureInPictureAutomaticallyFromInline = pictureInPictureEnabled
    }
}

struct AirPlayButton: UIViewRepresentable {
    func makeUIView(context: Context) -> AVRoutePickerView {
        let view = AVRoutePickerView()
        view.tintColor = .white
        view.activeTintColor = UIColor(AppColors.watching)
        view.prioritizesVideoDevices = true
        return view
    }

    func updateUIView(_ uiView: AVRoutePickerView, context: Context) {}
}
