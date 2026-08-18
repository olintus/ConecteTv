import Foundation

struct PlaylistService {
    func loadChannels(from playlistURL: URL = AppConstants.channelsPlaylistURL) async throws -> [Channel] {
        var request = URLRequest(url: playlistURL)
        request.timeoutInterval = 30
        request.setValue("audio/x-mpegurl, application/x-mpegURL, */*", forHTTPHeaderField: "Accept")
        request.setValue(AppConstants.userAgent, forHTTPHeaderField: "User-Agent")
        let (data, response) = try await URLSession.shared.data(for: request)
        guard let http = response as? HTTPURLResponse, (200...299).contains(http.statusCode) else {
            let code = (response as? HTTPURLResponse)?.statusCode ?? 0
            throw PlaylistError.http(code)
        }
        guard let content = String(data: data, encoding: .utf8) else {
            throw PlaylistError.invalidEncoding
        }
        return parse(content, relativeTo: playlistURL)
    }

    func parse(_ content: String, relativeTo playlistURL: URL) -> [Channel] {
        let pattern = #"([A-Za-z0-9_-]+)="([^"]*)""#
        let regex = try? NSRegularExpression(pattern: pattern)
        var channels: [Channel] = []
        var pendingInfo: String?

        for rawLine in content.components(separatedBy: .newlines) {
            let line = rawLine.trimmingCharacters(in: .whitespacesAndNewlines)
                .replacingOccurrences(of: "\u{FEFF}", with: "")
            if line.lowercased().hasPrefix("#extinf") {
                pendingInfo = line
            } else if line.isEmpty || line.hasPrefix("#") {
                continue
            } else if let info = pendingInfo {
                let attributes = attributes(in: info, regex: regex)
                guard let streamURL = URL(string: line, relativeTo: playlistURL)?.absoluteURL else {
                    pendingInfo = nil
                    continue
                }
                let metadataName = nameAfterMetadata(in: info)
                let displayName = !metadataName.isEmpty
                    ? metadataName
                    : (attributes["tvg-name"].flatMap { $0.isEmpty ? nil : $0 } ?? "Canal \(channels.count + 1)")
                let logoURL = attributes["tvg-logo"]
                    .flatMap { $0.isEmpty ? nil : resolveLogoURL($0, playlistURL: playlistURL) }
                channels.append(Channel(
                    id: "\(channels.count):\(attributes["tvg-id"] ?? ""):\(streamURL.absoluteString)",
                    name: displayName,
                    streamURL: streamURL,
                    logoURL: logoURL
                ))
                pendingInfo = nil
            }
        }
        return channels
    }

    private func attributes(in text: String, regex: NSRegularExpression?) -> [String: String] {
        guard let regex else { return [:] }
        let range = NSRange(text.startIndex..., in: text)
        return regex.matches(in: text, range: range).reduce(into: [:]) { result, match in
            guard let keyRange = Range(match.range(at: 1), in: text),
                  let valueRange = Range(match.range(at: 2), in: text) else { return }
            result[String(text[keyRange]).lowercased()] = String(text[valueRange]).trimmingCharacters(in: .whitespaces)
        }
    }

    private func nameAfterMetadata(in text: String) -> String {
        var insideQuotes = false
        for index in text.indices {
            if text[index] == "\"" { insideQuotes.toggle() }
            if text[index] == ",", !insideQuotes {
                return String(text[text.index(after: index)...]).trimmingCharacters(in: .whitespacesAndNewlines)
            }
        }
        return ""
    }

    // Mantém a mesma regra do Android: caminhos e query vêm do logo, mas o
    // esquema, host e porta são sempre os da playlist.
    private func resolveLogoURL(_ value: String, playlistURL: URL) -> URL? {
        guard let resolved = URL(string: value, relativeTo: playlistURL)?.absoluteURL,
              let playlist = URLComponents(url: playlistURL, resolvingAgainstBaseURL: true),
              var logo = URLComponents(url: resolved, resolvingAgainstBaseURL: true) else {
            return nil
        }
        logo.scheme = playlist.scheme
        logo.host = playlist.host
        logo.port = playlist.port
        return logo.url
    }
}

enum PlaylistError: LocalizedError {
    case http(Int)
    case invalidEncoding

    var errorDescription: String? {
        switch self {
        case .http(let code): return "Servidor retornou HTTP \(code)."
        case .invalidEncoding: return "A playlist possui uma codificação inválida."
        }
    }
}
