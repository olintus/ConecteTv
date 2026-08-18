import SwiftUI

struct ChannelSearchView: View {
    let channels: [Channel]
    @Binding var query: String
    let onBack: () -> Void
    let onSelect: (Channel) -> Void
    @FocusState private var searchFocused: Bool

    private var filteredChannels: [Channel] {
        let normalized = query.normalizedForSearch
        return normalized.isEmpty
            ? channels
            : channels.filter { $0.name.normalizedForSearch.contains(normalized) }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(spacing: 0) {
                Button(action: onBack) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 25, weight: .semibold))
                        .frame(width: 58, height: 58)
                }
                TextField("Buscar canais", text: $query)
                    .font(.system(size: 18))
                    .focused($searchFocused)
                    .submitLabel(.search)
                if query.isEmpty {
                    Color.clear.frame(width: 48, height: 48)
                } else {
                    Button { query = "" } label: {
                        Image(systemName: "xmark.circle.fill").frame(width: 48, height: 48)
                    }
                }
            }
            .foregroundStyle(Color(hex: 0x24272B))
            .frame(height: 72)
            .background(.white, in: RoundedRectangle(cornerRadius: 20))
            .padding(.horizontal, 8)
            .padding(.vertical, 10)

            Text("Grade Aberta")
                .font(.system(size: 25, weight: .bold))
                .foregroundStyle(.white)
                .padding(.leading, 26)
                .padding(.top, 24)
                .padding(.bottom, 16)

            if filteredChannels.isEmpty {
                VStack(spacing: 8) {
                    Image(systemName: "magnifyingglass").font(.system(size: 42))
                    Text("Nenhum canal encontrado").fontWeight(.semibold).padding(.top, 4)
                    Text("Não encontramos resultados para “\(query)”.")
                        .font(.system(size: 13))
                        .foregroundStyle(.white.opacity(0.68))
                }
                .foregroundStyle(.white.opacity(0.75))
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .padding(24)
            } else {
                ScrollView {
                    LazyVStack(spacing: 8) {
                        ForEach(filteredChannels) { channel in
                            Button { onSelect(channel) } label: {
                                HStack(spacing: 28) {
                                    ChannelLogo(channel: channel, width: 92, height: 76)
                                    Text(channel.name)
                                        .font(.system(size: 23, weight: .bold))
                                        .foregroundStyle(.black)
                                    Spacer()
                                }
                                .padding(.horizontal, 28)
                                .frame(maxWidth: .infinity, minHeight: 126)
                                .background(.white, in: RoundedRectangle(cornerRadius: 18))
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.horizontal, 14)
                    .padding(.bottom, 24)
                }
            }
        }
        .background(Color.black.ignoresSafeArea())
        .onAppear { searchFocused = true }
    }
}
