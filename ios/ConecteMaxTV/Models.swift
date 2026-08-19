import Foundation

struct Channel: Identifiable, Equatable {
    let id: String
    let name: String
    let streamURL: URL
    let logoURL: URL?
}

struct CustomerProfile: Equatable {
    let name: String
    let email: String
    let tvPlan: String
}

struct SavedCredentials: Codable, Equatable {
    let document: String
    let password: String
}

enum AuthenticationResult {
    case authorized(CustomerProfile)
    case invalidCredentials(String)
    case notEntitled(String)
    case unavailable(String)
}

enum SessionState: Equatable {
    case checking
    case loggedOut(message: String? = nil)
    case authenticated(CustomerProfile)
    case blocked(message: String)
}

enum ChannelLoadState: Equatable {
    case loading
    case loaded([Channel])
    case failed(String)
}

enum AppConstants {
    static let centralContractsURL = URL(string: "https://sgp.conecteinternet.com.br/api/central/contratos")!
    static let requiredTVPlan = "Conecte TV"
    static let channelsPlaylistURL = URL(string: "http://138.0.212.26/hls/playlist.m3u")!
    static let userAgent = "ConecteMaxTV/1.0"
}
