import SwiftUI
import UIKit

struct HomeView: View {
    @EnvironmentObject private var session: SessionViewModel
    let channels: [Channel]
    let profile: CustomerProfile

    @State private var selectedChannelID: String
    @State private var isFullscreen = false
    @State private var isSearching = false
    @State private var isShowingSettings = false
    @State private var backgroundPlaybackEnabled = true
    @State private var searchQuery = ""

    init(channels: [Channel], profile: CustomerProfile) {
        self.channels = channels
        self.profile = profile
        _selectedChannelID = State(initialValue: channels.first?.id ?? "")
    }

    private var selectedChannel: Channel? {
        channels.first(where: { $0.id == selectedChannelID }) ?? channels.first
    }

    var body: some View {
        Group {
            if isSearching && !isFullscreen {
                ChannelSearchView(
                    channels: channels,
                    query: $searchQuery,
                    onBack: closeSearch,
                    onSelect: { channel in
                        selectedChannelID = channel.id
                        closeSearch()
                    }
                )
            } else if isShowingSettings && !isFullscreen {
                SettingsView(
                    profile: profile,
                    backgroundPlaybackEnabled: $backgroundPlaybackEnabled,
                    onBack: { isShowingSettings = false },
                    onLogout: session.logout
                )
            } else if let selectedChannel {
                homeContent(selectedChannel)
            }
        }
        .onDisappear { setFullscreen(false) }
    }

    @ViewBuilder
    private func homeContent(_ selectedChannel: Channel) -> some View {
        if isFullscreen {
            PlayerView(
                streamURL: selectedChannel.streamURL,
                backgroundPlaybackEnabled: backgroundPlaybackEnabled,
                isFullscreen: true,
                onFullscreen: { setFullscreen(false) }
            )
            .background(.black)
            .ignoresSafeArea()
        } else {
            VStack(spacing: 0) {
                TopBar(onSearch: { isSearching = true })
                PlayerView(
                    streamURL: selectedChannel.streamURL,
                    backgroundPlaybackEnabled: backgroundPlaybackEnabled,
                    isFullscreen: false,
                    onFullscreen: { setFullscreen(true) }
                )
                .aspectRatio(16 / 9, contentMode: .fit)
                ChannelCounter(count: channels.count)
                ScrollView {
                    LazyVStack(spacing: 0) {
                        ForEach(channels) { channel in
                            ChannelRow(
                                channel: channel,
                                isSelected: channel.id == selectedChannel.id,
                                onSelect: { selectedChannelID = channel.id }
                            )
                            Divider().padding(.horizontal, 20)
                        }
                    }
                }
                BottomBar(onGrid: {}, onMenu: { isShowingSettings = true })
            }
            .background(.white)
        }
    }

    private func closeSearch() {
        isSearching = false
        searchQuery = ""
    }

    private func setFullscreen(_ enabled: Bool) {
        isFullscreen = enabled
        AppDelegate.orientationLock = enabled ? .landscape : .portrait
        guard let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene else { return }
        if #available(iOS 16.0, *) {
            let preferences = UIWindowScene.GeometryPreferences.iOS(
                interfaceOrientations: enabled ? .landscape : .portrait
            )
            scene.requestGeometryUpdate(preferences)
        }
        UIViewController.attemptRotationToDeviceOrientation()
    }
}

struct TopBar: View {
    let onSearch: () -> Void

    var body: some View {
        ZStack {
            Text("Conecte Max TV")
                .font(.system(size: 20, weight: .bold))
                .foregroundStyle(.white)
            HStack {
                AirPlayButton().frame(width: 44, height: 44)
                Spacer()
                Button(action: onSearch) {
                    Image(systemName: "magnifyingglass").font(.system(size: 21, weight: .medium))
                }
                .frame(width: 44, height: 44)
                .foregroundStyle(.white)
            }
            .padding(.horizontal, 8)
        }
        .frame(height: 56)
        .background(AppColors.darkBackground)
        .background(AppColors.darkBackground.ignoresSafeArea(edges: .top))
    }
}

struct ChannelCounter: View {
    let count: Int

    var body: some View {
        Text("\(count) CANAIS DISPONÍVEIS")
            .font(.system(size: 12, weight: .bold))
            .tracking(0.8)
            .foregroundStyle(Color(hex: 0x4B5563))
            .frame(maxWidth: .infinity, minHeight: 46)
            .background(AppColors.counterBackground)
    }
}

struct ChannelRow: View {
    let channel: Channel
    let isSelected: Bool
    let onSelect: () -> Void

    var body: some View {
        Button(action: onSelect) {
            HStack(spacing: 16) {
                ChannelLogo(channel: channel, width: 62, height: 62, circular: true)
                VStack(alignment: .leading, spacing: 4) {
                    Text(channel.name)
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundStyle(Color(hex: 0x202633))
                    if isSelected {
                        Text("Assistindo")
                            .font(.system(size: 13))
                            .foregroundStyle(AppColors.watching)
                    }
                }
                Spacer()
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 14)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }
}

struct BottomBar: View {
    let onGrid: () -> Void
    let onMenu: () -> Void

    var body: some View {
        HStack {
            Color.clear.frame(width: 48, height: 48)
            Spacer()
            Button(action: onGrid) {
                Image(systemName: "square.grid.2x2.fill").font(.system(size: 24))
                    .frame(width: 52, height: 52)
                    .background(.white.opacity(0.12), in: Circle())
            }
            Spacer()
            Button(action: onMenu) {
                Image(systemName: "line.3.horizontal").font(.system(size: 27))
                    .frame(width: 48, height: 48)
            }
        }
        .foregroundStyle(.white)
        .padding(.horizontal, 28)
        .frame(height: 76)
        .background(AppColors.bottomBar)
        .clipShape(TopRoundedShape(radius: 38))
    }
}

private struct TopRoundedShape: Shape {
    let radius: CGFloat

    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(
            roundedRect: rect,
            byRoundingCorners: [.topLeft, .topRight],
            cornerRadii: CGSize(width: radius, height: radius)
        )
        return Path(path.cgPath)
    }
}

struct ChannelLogo: View {
    let channel: Channel
    let width: CGFloat
    let height: CGFloat
    var circular = false

    var body: some View {
        Group {
            if let url = channel.logoURL {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .success(let image): image.resizable().scaledToFit()
                    case .failure: fallback
                    default: ProgressView().tint(AppColors.watching)
                    }
                }
            } else {
                fallback
            }
        }
        .frame(width: width, height: height)
        .background(Color(hex: 0xF3F4F6))
        .clipShape(circular ? AnyShape(Circle()) : AnyShape(Rectangle()))
    }

    private var fallback: some View {
        Text(String(channel.name.prefix(2)).uppercased())
            .fontWeight(.bold)
            .foregroundStyle(AppColors.watching)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
