import SwiftUI

enum AppColors {
    static let darkBackground = Color(hex: 0x111827)
    static let bottomBar = Color(hex: 0x172033)
    static let counterBackground = Color(hex: 0xF1F3F6)
    static let watching = Color(hex: 0x5680A6)
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

