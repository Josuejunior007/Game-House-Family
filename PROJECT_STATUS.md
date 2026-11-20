# Game House Family - Status do Projeto

## Informações Gerais

**Projeto:** Game House Family

**Objetivo:** Sistema para gerenciamento de sugestões e votação de jogos para grupos de Steam/Família Steam.

**Última atualização:** 2026-11-20

**Situação:** **MVP funcional de ponta a ponta** — API e front-end prontos, testados contra o banco real e verificados num navegador (desktop e celular). Falta apenas validar o login real da Steam com um navegador (depende de URL pública).

## Stack Atual
- Java 25 (o `pom.xml` compila com `release 25`; antes compilava 17 sem avisar)
- Spring Boot 4.1.1 (Spring Framework 7, Hibernate 7.4, Tomcat 11)
- Spring Data JPA, Bean Validation, Jackson
- PostgreSQL 17 + Supabase
- JUnit 5 + Mockito (19 testes)
- Maven Wrapper (`mvnw`)
- Front-end: React 19 + TypeScript 7 + Vite 8, CSS próprio (sem lib de UI)

## Arquitetura
Pacotes: config, controller, dto, exception, model, repository, service.
- Controller conversa só com o Service; Service orquestra Repository e SteamService; Service contém as regras.
- DTOs separados das entidades (records). DTOs da Steam espelham o JSON deles e não são expostos.
- Entidades sem setters: mudanças de estado por métodos com nome de regra de negócio.
- Front: `api.ts` (cliente tipado) → `auth.tsx` (sessão) → componentes. Estado com hooks, sem lib de estado.
- Erros da API em um formato único (`ApiError`: `status`, `message`, `fields`).

## Banco de Dados
- `spring.jpa.hibernate.ddl-auto=update`
- Tabelas: `game`, `users`, `suggestion`, `vote`
- Ids em saltos de 50 (SEQUENCE do Hibernate com pool): normal, **não são sequenciais**
- `data.sql` cria `ux_suggestion_one_active_per_game` (1 sugestão EM_VOTACAO por jogo) — confirmado no banco
- `vote` tem unique `uk_vote_user_suggestion (user_id, suggestion_id)` — confirmado no banco
- Coluna antiga `avatr_url` **já foi removida** (verificado em 2026-11-20)
- Nova coluna `game.image_url` (capa vinda da Steam). Jogos antigos foram preenchidos e, se ficar nula, o DTO monta a URL antiga como fallback.
- **Conexões:** o pooler de sessão do Supabase aceita 15 clientes no total; `APP_DB_POOL_SIZE` (padrão 5) evita o erro `EMAXCONNSESSION`

## Entidades
- **Game:** id, steamAppId (unique), name, imageUrl
- **User:** id, steamId (unique), displayName, avatarUrl; criado/atualizado no login
- **Suggestion:** id, user, game, copies, status, votingDeadline, createdAt. Métodos: votacaoAberta, prazoVencido, aprovar, rejeitar, marcarComoComprada
- **Vote:** id, user, suggestion, choice, createdAt, updatedAt. Método: alterarEscolha
- **Enums:** SuggestionStatus (EM_VOTACAO, APROVADA, REJEITADA, COMPRADA), VoteChoice (APOIO, NAO_APOIO)

## Regras de negócio
- RN-04/09: só EM_VOTACAO conta como sugestão ativa; histórico permitido
- RN-07: copies >= 1; RN-10/11: prazo de 1 a 30 dias
- RN-12/13: um voto por usuário por sugestão, alterável enquanto aberta
- RN-14: APOIO > NAO_APOIO aprova; empate ou zero votos rejeita
- RN-15: criador encerra a qualquer momento; outros só após o prazo; agendador (60 s) encerra as vencidas
- RN-16: só APROVADA vira COMPRADA; qualquer membro logado pode marcar
- RN-17: `app.allowed-steam-ids` opcional restringe o login à família

## Endpoints
Ver README.md (tabela completa) e requests.http (roteiro de testes).
Novo: `GET /steam/search?q=` — busca por nome na loja da Steam (o usuário não precisa saber o appId).

## Tratamento de erros (GlobalExceptionHandler)
Formato único `{ status, message, fields }` para 400 validação e JSON inválido, 401, 403, 404 (inclusive rota inexistente e corpo vazio), 405, 409, 415, 500 e 502.
O que foi corrigido: antes o cliente recebia três formatos diferentes (texto puro, mapa de campos e o JSON padrão do Spring sem mensagem).

## Steam
- `buscarJogo`: appdetails com `cc=br&l=portuguese`; timeouts (5 s conexão / 10 s leitura); falha de rede → 502; resposta nula/`success:false` → 404
- `buscarJogos` (busca): storesearch com `cc=br&l=portuguese`, filtra `type=app`, limita a 10; usa a miniatura que a Steam devolve
- `buscarPerfil`: XML do perfil público, com proteção XXE; fallback "Jogador XXXX"
- Capa: usa `header_image` devolvido pela Steam (o CDN mudou; montar a URL na mão deixava jogos sem capa)
- Preço em centavos, sob demanda em `GET /games/{id}/price` (não é salvo)

## Front-end
- `frontend/` (React + TS + Vite): login (Steam + atalho de dev), Sugestões (filtros por status, votar, encerrar, marcar comprada), Catálogo (busca na Steam, adicionar jogo, ver preço, abrir votação), modais, toasts, tema escuro e layout responsivo.
- Sem roteador: navegação por abas em estado local (suficiente para o escopo actual).
- Datas chegam em UTC e são formatadas em `America/Sao_Paulo` pelo `Intl` do navegador.

## Testes
- Unitários (Mockito, sem banco): SuggestionTest, SuggestionServiceTest, VoteServiceTest — `19 testes, 0 falhas`
- Verificação manual automatizada (2026-11-20): ~40 chamadas HTTP contra o Supabase real cobrindo 401/403/404/409/415/201/200, troca de voto, empate → REJEITADA, compra só de APROVADA, logout invalidando sessão, agendador encerrando sugestão vencida, CORS com credenciais
- Front verificado em navegador headless: login, criar sugestão pelo catálogo (pré-seleção correta), votar, busca real na Steam, modais fechando com Esc, layout de 1300 px e 390 px, zero erro de console

## Pendências
- **Login real da Steam: validado em 2026-11-20** com a conta do Josué (`steamId 76561199343404894`): o OpenID foi confirmado pela Steam (`check_authentication`), o usuário foi criado e a sessão aberta. O que falhava era o pós-login (CORS desligado e callback devolvendo JSON em vez de redirecionar) — corrigido.
  - Falta observar o `buscarPerfil` com perfil privado: hoje cai no nome "Jogador XXXX" e registra um WARN no log (era silencioso).
- Definir `APP_ALLOWED_STEAM_IDS` antes de expor a API na internet: vazio = qualquer conta Steam entra (RN-17)
- Rodar `mvn test` e `npm run build` em outra máquina (validar wrapper e Node)
- Se for publicar: servir o `dist/` do front na mesma origem da API (ou ajustar `SameSite`/CORS), definir `APP_FRONTEND_URL` e `APP_DEV_LOGIN_ENABLED=false`
- Commitar as alterações do pós-login (erro de CORS, `FrontendUrl`, log do perfil, banner de conexão no front)

## Futuro
- DELETE de jogo (com bloqueio quando houver sugestões) e de sugestão
- Comentários nas sugestões, compras parciais (quantidade comprada/restante)
- Cargos e permissões, quórum configurável
- Paginação e cache de preços (a Steam aceita vários appIds por chamada)
