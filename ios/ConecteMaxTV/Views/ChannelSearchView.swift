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
                    Image(systemName: "arrow.left")
                        .font(.system(size: 30, weight: .medium))
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
                        Image(systemName: "xmark").frame(width: 48, height: 48)
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
                VStack(spacing: 0) {
                    Image(systemName: "magnifyingglass").font(.system(size: 42))
                    Text("Nenhum canal encontrado").fontWeight(.semibold).padding(.top, 12)
                    Text("Não encontramos resultados para “\(query)”.")
                        .font(.system(size: 13))
                        .foregroundStyle(.white.opacity(0.68))
                        .padding(.top, 4)
                }
                .foregroundStyle(.white.opacity(0.75))
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .padding(24)
            } else {
                ScrollView {
                    LazyVStack(spacing: 6) {
                        ForEach(filteredChannels) { channel in
                            Button { onSelect(channel) } label: {
                                HStack(spacing: 20) {
                                    ChannelLogo(channel: channel, width: 76, height: 62)
                                    Text(channel.name)
                                        .font(.system(size: 20, weight: .bold))
                                        .foregroundStyle(.black)
                                    Spacer()
                                }
                                .padding(.horizontal, 20)
                                .frame(maxWidth: .infinity, minHeight: 104)
                                .background(.white, in: RoundedRectangle(cornerRadius: 16))
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.horizontal, 14)
                    .padding(.bottom, 24)
                }
            }
        }
        .background(AppColors.brandNavy.ignoresSafeArea())
        .onAppear { searchFocused = true }
    }
}
