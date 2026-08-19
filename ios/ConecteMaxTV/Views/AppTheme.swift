import SwiftUI

enum AppColors {
    static let brandNavy = Color(hex: 0x0B1D46)
    static let brandBlue = Color(hex: 0x1768CB)
    static let brandCyan = Color(hex: 0x45C8D8)
    static let brandOrange = Color(hex: 0xF45A0B)
    static let darkBackground = brandNavy
    static let bottomBar = brandNavy
    static let counterBackground = Color(hex: 0xF0F6FC)
    static let watching = brandBlue
    static let appBackground = Color(hex: 0xF7FAFD)
}

struct BrandLogo: View {
    var body: some View {
        Image("BrandLogo")
            .resizable()
            .scaledToFit()
            .accessibilityLabel("Conecte TV")
    }
}

struct PlayerBrandLogo: View {
    var body: some View {
        Image("PlayerBrandLogo")
            .resizable()
            .scaledToFill()
            .accessibilityLabel("Conecte TV")
    }
}

extension Color {
    init(hex: UInt, alpha: Double = 1) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255,
            green: Double((hex >> 8) & 0xFF) / 255,
            blue: Double(hex & 0xFF) / 255,
            opacity: alpha
        )
    }
}

extension String {
    var normalizedForSearch: String {
        folding(options: [.diacriticInsensitive, .caseInsensitive], locale: .current)
            .trimmingCharacters(in: .whitespacesAndNewlines)
    }
}
