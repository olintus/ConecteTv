import SwiftUI

struct SettingsView: View {
    let profile: CustomerProfile
    @Binding var backgroundPlaybackEnabled: Bool
    let onBack: () -> Void
    let onLogout: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            Text("Conecte Max TV")
                .font(.system(size: 23, weight: .bold))
                .foregroundStyle(.white)
                .frame(maxWidth: .infinity, minHeight: 96)
                .background(.black)

            VStack(spacing: 0) {
                Image(systemName: "person.fill")
                    .font(.system(size: 78))
                    .foregroundStyle(Color(hex: 0x969696))
                    .frame(width: 120, height: 120)
                    .background(Color(hex: 0xF0F0F0), in: Circle())
                    .overlay(Circle().stroke(AppColors.watching, lineWidth: 3))
                Text(profile.name)
                    .font(.system(size: 22, weight: .semibold))
                    .foregroundStyle(Color(hex: 0x161616))
                    .padding(.top, 16)
                if !profile.email.isEmpty {
                    Text(profile.email)
                        .font(.system(size: 14))
                        .foregroundStyle(Color(hex: 0x4B4B4B))
                        .padding(.top, 6)
                }
                Text(profile.tvPlan)
                    .font(.system(size: 13, weight: .medium))
                    .foregroundStyle(AppColors.watching)
                    .padding(.top, 5)
            }
            .padding(.top, 28)
            .padding(.bottom, 24)
            .frame(maxWidth: .infinity)

            Divider().padding(.horizontal, 24)
            SettingsRow(icon: "play.circle.fill", label: "Habilitar em segundo plano") {
                Toggle("", isOn: $backgroundPlaybackEnabled).labelsHidden()
            }
            SettingsRow(icon: "globe", label: "Idioma") {
                Text("Português").font(.system(size: 13)).foregroundStyle(Color(hex: 0x6B7280))
                Image(systemName: "chevron.right")
            }
            SettingsRow(icon: "lock.fill", label: "Alterar senha do Controle Parental") {
                Image(systemName: "chevron.right")
            }

            Spacer()
            Button(action: onLogout) {
                HStack(spacing: 24) {
                    Image(systemName: "rectangle.portrait.and.arrow.right").font(.system(size: 27))
                    Text("Sair").font(.system(size: 20))
                    Spacer()
                }
                .foregroundStyle(Color(hex: 0x202020))
                .padding(.horizontal, 48)
                .padding(.vertical, 18)
            }
            .buttonStyle(.plain)
            Text("Versão: 1.0")
                .font(.system(size: 11))
                .foregroundStyle(Color(hex: 0x4B4B4B))
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.leading, 18)
                .padding(.bottom, 12)
            BottomBar(onGrid: onBack, onMenu: {})
        }
        .background(.white)
    }
}

private struct SettingsRow<Trailing: View>: View {
    let icon: String
    let label: String
    let trailing: () -> Trailing

    init(icon: String, label: String, @ViewBuilder trailing: @escaping () -> Trailing) {
        self.icon = icon
        self.label = label
        self.trailing = trailing
    }

    var body: some View {
        HStack(spacing: 24) {
            Image(systemName: icon).font(.system(size: 27)).frame(width: 30)
            Text(label).font(.system(size: 17)).foregroundStyle(Color(hex: 0x202020))
            Spacer()
            HStack(spacing: 4) { trailing() }
        }
        .foregroundStyle(Color(hex: 0x333333))
        .padding(.horizontal, 48)
        .frame(height: 72)
    }
}
