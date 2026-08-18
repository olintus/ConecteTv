import Foundation

struct CentralClient {
    private struct Response: Decodable {
        let auth: Bool?
        let contratos: [Contract]?
    }

    private struct Contract: Decodable {
        let status: String?
        let planotv: String?
        let razaosocial: String?
        let emails: [String]?
    }

    func authenticate(document: String, password: String) async -> AuthenticationResult {
        let boundary = "ConecteMax-\(UUID().uuidString)"
        var request = URLRequest(url: AppConstants.centralContractsURL)
        request.httpMethod = "POST"
        request.timeoutInterval = 30
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        request.setValue(AppConstants.userAgent, forHTTPHeaderField: "User-Agent")
        request.httpBody = multipartBody(boundary: boundary, fields: [
            ("cpfcnpj", document),
            ("senha", password)
        ])

        do {
            let (data, response) = try await URLSession.shared.data(for: request)
            guard let http = response as? HTTPURLResponse else {
                return .unavailable("A Central enviou uma resposta inválida.")
            }
            guard (200...299).contains(http.statusCode) else {
                return .unavailable("A Central retornou HTTP \(http.statusCode). Tente novamente.")
            }

            let payload = try JSONDecoder().decode(Response.self, from: data)
            guard payload.auth == true else {
                return .invalidCredentials("CPF/CNPJ ou senha inválidos.")
            }

            var customerName = ""
            var customerEmail = ""
            var activePlan = ""
            for contract in payload.contratos ?? [] {
                if customerName.isEmpty {
                    customerName = contract.razaosocial?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
                }
                if customerEmail.isEmpty {
                    customerEmail = contract.emails?.first?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
                }
                let status = contract.status?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
                let plan = contract.planotv?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
                if status.compare("Ativo", options: .caseInsensitive) == .orderedSame,
                   plan.compare(AppConstants.requiredTVPlan, options: .caseInsensitive) == .orderedSame {
                    activePlan = plan
                    break
                }
            }

            guard !activePlan.isEmpty else {
                return .notEntitled(
                    "Não foi encontrado um contrato ativo do plano \(AppConstants.requiredTVPlan) para este cliente."
                )
            }
            return .authorized(CustomerProfile(
                name: customerName.isEmpty ? "Cliente Conecte TV" : customerName,
                email: customerEmail,
                tvPlan: activePlan
            ))
        } catch let error as URLError where error.code == .timedOut {
            return .unavailable("A Central demorou para responder. Tente novamente.")
        } catch {
            return .unavailable("Não foi possível conectar à Central do Cliente.")
        }
    }

    private func multipartBody(boundary: String, fields: [(String, String)]) -> Data {
        var body = Data()
        for (name, value) in fields {
            body.append("--\(boundary)\r\n")
            body.append("Content-Disposition: form-data; name=\"\(name)\"\r\n")
            body.append("Content-Type: text/plain; charset=UTF-8\r\n\r\n")
            body.append("\(value)\r\n")
        }
        body.append("--\(boundary)--\r\n")
        return body
    }
}

private extension Data {
    mutating func append(_ string: String) {
        append(string.data(using: .utf8) ?? Data())
    }
}
