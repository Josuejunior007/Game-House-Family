# Game House Family - Status do Projeto

## Informações Gerais

**Projeto:** Game House Family

**Objetivo:** Login pela Steam, família gerenciada pelo app (dono adiciona membros pelo SteamID64, convites, perfil com ID copiável) e sugestões/votação de jogos para a família.

**Última atualização:** 2026-11-24

**Situação:** **Família + sugestões funcionando de ponta a ponta** — API e front testados contra o banco real e verificados num navegador headless (desktop e celular). O login real da Steam já havia sido validado em 2026-11-20; falta apenas repetir com URL pública quando for publicar.

## Stack Atual
- Java 25 (o `pom.xml` compila com `release 25`)
- Spring Boot 4.1.1 (Spring Framework 7, Hibernate 7.4, Tomcat 11)
- Spring Data JPA, Bean Validation, Jackson
- PostgreSQL 17 + Supabase
- JUnit 5 + Mockito (35 testes)
- Maven Wrapper (`mvnw`)
- Front-end: React 19 + TypeScript 7 + Vite 8, CSS próprio (sem lib de UI)

## Arquitetura
Pacotes: config, controller, dto, exception, model, repository, service.
- Controller conversa só com o Service; Service orquestra Repository e SteamService; Service contém as regras.
- DTOs separados das entidades (records). DTOs da Steam espelham o JSON deles e não são expostos.
- Entidades sem setters: mudanças de estado por métodos com nome de regra de negócio.
- Front: `api.ts` (cliente tipado) → `auth.tsx` (sessão) → componentes. Estado com hooks, sem lib de estado.
- Erros da API em um formato único (`ApiError`: `status`, `message`, `fields`).
- Login: `AuthInterceptor` exige sessão; `FamilyInterceptor` exige que o usuário seja membro para `/games`, `/games/search`, `/suggestions` e `/steam/**`.

## Banco de Dados
- `spring.jpa.hibernate.ddl-auto=update`
- Tabelas: `game`, `users`, `suggestion`, `vote`, `family`, `family_member`, `family_invite`
- `family_member` tem `unique (user_id)` (constraint `uk7ldr00f9kc7jydj38i3ie72yx`): uma família por pessoa
- `family_invite` indexa `(steam_id, status)` e `(family_id, status)` para achar convites pendentes
- Ids em saltos de 50 (SEQUENCE do Hibernate com pool): normal, **não são sequenciais**
- `data.sql` cria `ux_suggestion_one_active_per_game` (1 sugestão EM_VOTACAO por jogo) — confirmado no banco
- `vote` tem unique `uk_vote_user_suggestion (user_id, suggestion_id)` — confirmado no banco
- Coluna antiga `avatr_url` **já foi removida**
- Nova coluna `game.image_url` (capa vinda da Steam). Jogos antigos foram preenchidos e, se ficar nula, o DTO monta a URL antiga como fallback.
- Jogos que entram pela busca salvam a capa 460x215 montada a partir da miniatura (`/steam/apps/{appId}/header.jpg` no mesmo CDN)
- **Conexões:** o pooler de sessão do Supabase aceita 15 clientes no total; `APP_DB_POOL_SIZE` (padrão 5) evita o erro `EMAXCONNSESSION`

## Entidades
- **Game:** id, steamAppId (unique), name, imageUrl
- **User:** id, steamId (unique), displayName, avatarUrl; criado/atualizado no login
- **Suggestion:** id, user, game, copies, status, votingDeadline, createdAt. Métodos: votacaoAberta, prazoVencido, aprovar, rejeitar, marcarComoComprada
- **Vote:** id, user, suggestion, choice, createdAt, updatedAt. Método: alterarEscolha
- **Family:** id, name, owner, createdAt. Existe no máximo uma no app
- **FamilyMember:** id, family, user (unique), role (DONO/MEMBRO), joinedAt
- **FamilyInvite:** id, family, steamId, invitedName (snapshot, pode ser nulo), invitedAvatarUrl, invitedBy, status (PENDENTE/ACEITO/RECUSADO/CANCELADO), createdAt, respondedAt
- **Enums:** SuggestionStatus, VoteChoice, FamilyRole, InviteStatus

## Regras de negócio
- **RF-01** Cria-se uma família; quem cria é DONO. A segunda criação → 409. `APP_FAMILY_OWNER_STEAM_ID` opcional restringe quem pode criar.
- **RF-02** Só o dono convida/cancela/remove. O dono não pode sair sozinho (precisa excluir a família).
- **RF-03** Convite pelo SteamID64 (17 dígitos); vira membro só depois de aceitar.
- **RF-04** Uma família por pessoa (`unique user_id` no banco).
- **RF-05** Sem família, só `/auth/*`, `/family`, `/invites` e `/auth/me`; o resto → 403.
- **RN-04/09** Só EM_VOTACAO conta como sugestão ativa; histórico permitido
- **RN-07** copies >= 1; **RN-10/11** prazo de 1 a 30 dias
- **RN-12/13** um voto por usuário por sugestão, alterável enquanto aberta
- **RN-14** APOIO > NAO_APOIO aprova; empate ou zero votos rejeita
- **RN-15** criador encerra a qualquer momento; outros só após o prazo; agendador (60 s) encerra as vencidas
- **RN-16** só APROVADA vira COMPRADA; qualquer membro logado pode marcar
- **RN-18** criador ou dono exclui a sugestão (apaga os votos antes)
- **RN-19** busca de jogos: banco primeiro; se faltar, Steam, e o que veio é salvo

## Endpoints
Ver README.md (tabela completa) e requests.http (roteiro de testes).
Novos: `/family`, `/family/invites`, `/family/leave`, `/family/members/{id}`, `/invites`, `/invites/{id}/accept|decline`, `GET /games/search?q=`, `DELETE /suggestions/{id}`.
`/auth/me` agora devolve `familyId`, `familyName`, `familyRole`.

## Tratamento de erros (GlobalExceptionHandler)
Formato único `{ status, message, fields }` para 400 validação e JSON inválido, 401, 403, 404 (inclusive rota inexistente e corpo vazio), 405, 409, 415, 500 e 502.
Novos mapeamentos: `FamiliaJaExisteException` e `ConviteInvalidoException` → 409; `ConviteNaoEncontradoException` e `MembroNaoEncontradoException` → 404.

## Steam
- `buscarJogo`: appdetails com `cc=br&l=portuguese`; timeouts (5 s conexão / 10 s leitura); falha de rede → 502; resposta nula/`success:false` → 404
- `buscarJogos` (busca): storesearch com `cc=br&l=portuguese`, filtra `type=app`, limita a 10
- `buscarPerfil`: XML do perfil público, com proteção XXE; fallback "Jogador XXXX"
- Capa: usa `header_image` devolvido pela Steam no cadastro; na busca, monta `header.jpg` no mesmo CDN
- Preço em centavos, sob demanda em `GET /games/{id}/price` (não é salvo)
- **Família:** a Steam **não** oferece API pública de Steam Family. O login OpenID só devolve o SteamID64; a família é do app.

## Front-end
- `frontend/` (React + TS + Vite): login só Steam, abas **Sugestões**, **Catálogo**, **Família** e **Convites**.
- **Família:** perfil com avatar, nome, papel e SteamID64 com botão Copiar (Clipboard API + fallback), lista de membros, convite por SteamID (dono), cancelamento, remoção, sair e excluir família.
- **Convites:** badge com a quantidade no cabeçalho, aceitar/recusar; ao aceitar, relê `/auth/me` e mostra a família.
- **Onboarding:** sem família, as abas de sugestões/catálogo somem e aparece o cartão para criar a família ou ver convites.
- **Catálogo:** busca com debounce no topo; o resultado vem do banco ou da Steam (salvo na hora); "Sugerir" abre a votação.
- **Nova sugestão:** busca o jogo por nome no próprio modal (não exige appId).
- **Sugestões:** excluir com confirmação para criador/dono.
- Login antigo de teste (nome/ID) **removido da UI**; `POST /auth/dev-login` continua só para testes de API.
- Datas chegam em UTC e são formatadas em `America/Sao_Paulo` pelo `Intl` do navegador.

## Testes
- Unitários (Mockito, sem banco): SuggestionTest, SuggestionServiceTest, VoteServiceTest, FamilyServiceTest — **35 testes, 0 falhas**
- Verificação de API (2026-11-24, banco real): login dev, criação de família, 409 na segunda, convite válido/duplicado/para si/ inválido (400 com `fields`), 403 para não-membro em `/games`, convite recebido, aceite, 403 do membro tentando convidar/remover, remoção pelo dono, sair, dono não sai, busca com cache (banco → Steam salvando), exclusão de sugestão (criador e 403 para outro), 401 sem sessão.
- Front verificado em navegador headless (Chrome + Puppeteer): onboarding, criação da família, convite, badge, aceite, perfil, cópia do SteamID (toast + clipboard), sugestão criada pela busca, exclusão, catálogo com 10 resultados, layout 1280 px e 390 px, sem erro de console (só os 401 esperados antes do login).

## Pendências
- **Login real da Steam:** validado em 2026-11-20 com a conta do Josué (`steamId 76561199343404894`); repetir com URL pública ao publicar.
- Definir `APP_FAMILY_OWNER_STEAM_ID` antes de expor a API: sem isso, a primeira conta que criar vira dona.
- Rodar `mvn test` e `npm run build` em outra máquina (validar wrapper e Node)
- Se for publicar: servir o `dist/` do front na mesma origem da API (ou ajustar `SameSite`/CORS), definir `APP_FRONTEND_URL` e `APP_DEV_LOGIN_ENABLED=false`
- **Sem DELETE de jogo:** o catálogo/cache cresce com as buscas; avaliar exclusão com bloqueio quando houver sugestões

## Futuro
- DELETE de jogo (com bloqueio quando houver sugestões) e limpeza do cache de busca
- Várias famílias no mesmo banco (`family_id` em `game`/`suggestion`) — hoje o app é de uma família só
- Comentários nas sugestões, compras parciais (quantidade comprada/restante)
- Cargos e permissões além de DONO/MEMBRO, quórum configurável
- Paginação e cache de preços (a Steam aceita vários appIds por chamada)
