# Publicação do Conecte TV na Google Play

## Estado técnico

- Application ID: `br.com.conectemax.tv`
- Nome: `Conecte TV`
- Version code: `1`
- Version name: `1.0`
- Target/compile SDK: `36`
- Formato de publicação: Android App Bundle (`.aab`)
- Play App Signing: recomendado
- Ícone da Play Store: `app/play-store-icon-512.png`

O bundle atual é apenas de validação e está **sem assinatura**. Não o envie antes
de criar a chave de upload e gerar novamente o release assinado.

## 1. Conta e teste

- Criar o app no Play Console com idioma padrão `Português (Brasil)`.
- Selecionar `App`, não `Jogo`.
- Selecionar distribuição gratuita, salvo decisão comercial diferente.
- Se a conta for pessoal e tiver sido criada após 13/11/2023, executar teste
  fechado com pelo menos 12 participantes inscritos continuamente por 14 dias.

## 2. Chave de upload

No Android Studio:

1. Abrir `Build > Generate Signed Bundle / APK`.
2. Selecionar `Android App Bundle`.
3. Criar uma chave de upload `.jks` em uma pasta segura fora do OneDrive e do
   repositório.
4. Usar um alias como `conecte-tv-upload`.
5. Guardar a chave e as senhas em um gerenciador de senhas e em backup seguro.
6. Copiar `keystore.properties.example` para `keystore.properties` e preencher
   localmente. Esse arquivo e as chaves são ignorados pelo Git.
7. Gerar novamente com `gradlew :app:bundleRelease`.

Ativar o Play App Signing ao criar a primeira versão no Play Console.

## 3. Ficha da loja

### Nome

`Conecte TV`

### Descrição curta sugerida

`Assista aos canais do seu plano Conecte TV com praticidade.`

### Descrição completa sugerida

`Conecte TV é o aplicativo de TV para clientes com assinatura ativa do serviço.`

`Entre com o CPF e a senha da Central do Cliente para validar seu plano e acessar os canais disponíveis. Pesquise canais, alterne a transmissão com facilidade, use o modo de tela cheia e continue assistindo em Picture-in-Picture quando disponível no dispositivo.`

`O acesso ao conteúdo depende de uma assinatura Conecte TV ativa e de conexão com a internet. A quantidade e a disponibilidade dos canais podem variar conforme o plano e a região.`

### Ativos obrigatórios

- Ícone PNG 512 x 512, até 1 MB: já disponível.
- Feature graphic PNG/JPEG 1024 x 500, sem transparência: pendente.
- Pelo menos 2 screenshots reais; recomendado 4 em 1080 x 1920: pendente.
- E-mail de suporte: pendente.
- Site: pendente.
- URL pública da política de privacidade: pendente e obrigatória para este app.

## 4. Acesso para revisão

Em `Política e programas > Conteúdo do app > Acesso ao app`, informar que todo
ou parte do app é restrito e fornecer uma conta exclusiva de teste:

- CPF de teste: `PREENCHER`
- Senha de teste: `PREENCHER`
- Instrução: `Entre com o CPF e a senha informados. A conta possui contrato Ativo e plano de TV exatamente “Conecte TV”.`

A conta deve funcionar continuamente, sem código temporário, restrição de IP ou
dependência de um usuário real. Nunca fornecer credenciais de cliente em produção.

## 5. Privacidade e segurança de dados

O preenchimento final deve ser conferido com o responsável jurídico/privacidade.
Pelo comportamento atual do app, considerar declarar:

- Dados pessoais/autenticação: CPF (identificador do usuário) e senha.
- Dados de perfil recebidos do SGP: nome, e-mail e plano contratado.
- Finalidade: autenticação e gerenciamento/fornecimento da funcionalidade do app.
- Compartilhamento: confirmar a relação entre Conecte e o fornecedor do SGP.
- Dados de autenticação transmitidos por HTTPS.
- Credenciais salvas localmente com criptografia baseada no Android Keystore.
- Sem anúncios e sem SDK de rastreamento no código atual.
- Canal para solicitar correção ou exclusão de dados: pendente.

A política pública deve explicar o uso do CPF, autenticação no SGP, retenção das
credenciais no aparelho, proteção, contato do controlador e procedimento de
exclusão/correção.

## 6. Conteúdo e declarações

- Declarar corretamente a faixa etária e responder ao questionário de conteúdo.
- Não selecionar crianças como público-alvo sem uma revisão específica da política
  para famílias.
- Declarar que o app não contém anúncios, se isso continuar verdadeiro.
- Confirmar e documentar os direitos de distribuição de todos os canais exibidos.
- O app é de consumo de uma assinatura existente e não oferece compra ou link de
  pagamento dentro do aplicativo atual.

## 7. Ordem recomendada no Play Console

1. Criar app.
2. Completar detalhes e contato da ficha.
3. Publicar política de privacidade.
4. Enviar ícone, feature graphic e screenshots.
5. Preencher segurança de dados, acesso ao app, anúncios, público-alvo e conteúdo.
6. Criar chave de upload e gerar `.aab` assinado.
7. Enviar primeiro para teste interno.
8. Criar teste fechado, quando exigido.
9. Corrigir avisos do relatório de pré-lançamento.
10. Solicitar acesso à produção e enviar para revisão.
