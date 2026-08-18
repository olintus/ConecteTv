# Conecte Max TV — iOS

Versão nativa iOS do aplicativo Android deste repositório, escrita em Swift 5 e SwiftUI. Não há bibliotecas externas.

## Abrir e executar

1. Em um Mac com Xcode 15 ou mais recente, abra `ConecteMaxTV.xcodeproj`.
2. Na aba **Signing & Capabilities** do target `ConecteMaxTV`, selecione a sua equipe Apple.
3. Se o identificador `br.com.conectemax.tv` já estiver em uso em sua conta, defina um Bundle Identifier exclusivo.
4. Escolha um simulador ou iPhone e use **Product > Run**.

O catálogo inclui apenas o espaço reservado do App Icon. Antes de distribuir, arraste uma arte de 1024 × 1024 px para `Assets.xcassets > AppIcon`.

## Equivalência com Android

- autenticação multipart na Central do Cliente;
- exigência de contrato ativo com o plano `Conecte TV`;
- credenciais protegidas pelo Keychain;
- restauração e revalidação da sessão ao iniciar;
- download e parser da playlist M3U;
- reprodução HLS nativa com AVPlayer;
- AirPlay, busca sem distinção de acentos, tela cheia e grade de canais;
- perfil, logout e reprodução de áudio em segundo plano.

O servidor da playlist e dos streams usa HTTP em um endereço IP. Por isso o `Info.plist` libera cargas HTTP via ATS para manter compatibilidade com o backend atual; a API de autenticação continua apontando para HTTPS. Para distribuição na App Store, a recomendação é servir playlist, logos e streams por HTTPS e então remover `NSAllowsArbitraryLoads`.

## Testes

Execute **Product > Test** no Xcode. Os testes cobrem os pontos mais sensíveis do parser M3U, incluindo URLs relativas e vírgulas dentro de metadados entre aspas.

O arquivo `project.yml` também permite regenerar o projeto com [XcodeGen](https://github.com/yonaskolb/XcodeGen), se desejado:

```sh
cd ios
xcodegen generate
```
