# Game House Family - Status do Projeto

## Informações Gerais

**Projeto:** Game House Family

**Objetivo:** Sistema para gerenciamento de sugestões e votação de jogos para grupos de Steam/Família Steam.

**Última atualização:** 2026-11-03

## Stack Atual
- Java (pom.xml configurado com 25 - conferir qual versão está instalada)
- Spring Boot 4.1.1
- Spring Data JPA
- spring-boot-starter-validation (Bean Validation)
- PostgreSQL
- Supabase
- Maven
- IntelliJ IDEA
- DBeaver (para visualizar o banco)

## Arquitetura
Pacotes:
- controller
- dto
- exception
- model
- repository
- service

## Banco de Dados
- PostgreSQL hospedado no Supabase, conexão funcionando
- `spring.jpa.hibernate.ddl-auto=update` (o Hibernate cria/ajusta as tabelas; não renomeia nem apaga colunas)
- Ids gerados em saltos de 50 (SEQUENCE do Hibernate reserva blocos de 50). Comportamento normal.
- Tabelas existentes: `game`, `users`, `suggestion`
- Tabela de usuários se chama `users` porque `user` é palavra reservada no Postgres

## Entidades (model)

### Game
- id, steamAppId (String, unique, not null), name
- steamAppId é identificador externo da Steam e não deve ser alterado
- name pode ser atualizado (`atualizarNome`)

### User
- id, steamId (String, unique, not null), displayName (not null), avatarUrl (opcional: conta Steam pode não ter foto)
- Atualização por um único método: `atualizarNomeAvatar`
- Sem CRUD por enquanto: o usuário vai nascer no login pela Steam

### Suggestion
- id, user (@ManyToOne LAZY), game (@ManyToOne LAZY), copies, status, votingDeadline (Instant), createdAt (Instant)
- Status inicial é sempre EM_VOTACAO (definido no construtor), createdAt também
- Só getters (sem setters); mudanças de estado virão por métodos com nome de regra de negócio

### SuggestionStatus (enum)
- EM_VOTACAO, APROVADA, REJEITADA, COMPRADA
- Salvo como STRING no banco (@Enumerated)

## Decisões de regra de negócio
- Só **EM_VOTACAO** conta como sugestão ativa: bloqueia nova sugestão do mesmo jogo. APROVADA não bloqueia, mesmo que a compra ainda não tenha saído.
- Histórico é permitido: o mesmo jogo pode ter várias sugestões encerradas.
- Quantidade de cópias mínima é 1 (RN-07).
- Prazo da votação chega na requisição como **duração em dias** (`votingDays`, de 1 a 30, sem horas). O Service calcula `votingDeadline = agora + dias`.
- `userId` vem no corpo da requisição apenas até o login Steam existir.

## Repositories
### GameRepository
- save(), findAll(), findById()
- existsBySteamAppId(String)

### UserRepository
- vazio por enquanto (só os métodos do JpaRepository)

### SuggestionRepository
- existsByGameIdAndStatus(int gameId, SuggestionStatus status): base da regra "uma sugestão ativa por jogo"
- Lembrete: `exists...` devolve boolean; `find...` devolve objeto/Optional

## Service
### GameService
Dependências: GameRepository e SteamService (injetados pelo construtor).

- cadastrar(GameCreateRequest):
  1. verifica no banco se o steamAppId já existe (GameJaCadastradoException)
  2. busca o jogo na Steam via SteamService (GameNaoEncontradoNaSteamException se não existir)
  3. cria o Game com o nome vindo da Steam
  4. salva no banco
- listar()
- buscarPorID() → Optional<Game>
- atualizar() → Optional<Game>

### SteamService
- Usa `RestClient` com base `https://store.steampowered.com`
- `buscarJogo(String appId)` retorna `Optional<SteamGameData>`
- Chama `/api/appdetails?appids={id}&cc=br&l=portuguese` (preço em BRL, texto em português)
- Resposta vem embrulhada por uma chave igual ao AppID (`Map<String, SteamAppResponse>`)
- `success: false` ou chave ausente → `Optional.empty()`
- Preço vem em **centavos** (4760 = R$ 47,60); jogos gratuitos têm `price_overview` null

## Controller
### GameController
- POST /games → 201 com `GameResponse` (409 se já cadastrado, 404 se não existe na Steam, 400 se steamAppId em branco)
- GET /games → lista de `GameResponse`
- GET /games/{id} → 200 ou 404 (Optional.map + ResponseEntity.of)
- PATCH /games/{id} → atualiza só o nome; 200, 404 ou 400 (name em branco / ausente)

### HomeController
- GET / → "Game House Family"

## Tratamento de erros
`GlobalExceptionHandler` (@RestControllerAdvice):
- GameJaCadastradoException → 409
- GameNaoEncontradoNaSteamException → 404
- MethodArgumentNotValidException → 400 com mapa campo → mensagem

## DTOs
### Internos
- GameCreateRequest(@NotBlank steamAppId)
- GameUpdateRequest(@NotBlank name)
- GameResponse(id, steamAppId, name) com método estático `de(Game)`
- SuggestionCreateRequest: **em andamento** (ver Próximo Passo)

### Steam (espelham o JSON deles e não são expostos pela nossa API)
- SteamPriceData(currency, initial, finalPrice, discountPercent)
- SteamGameData(name, steamAppId, priceOverview)
- SteamAppResponse(success, data)

## Arquivo de Testes
- `requests.http` versionado no projeto. Os links `<> 2026-...json` no final são histórico do IntelliJ e podem ser apagados.

## Próximo Passo
Criar a sugestão (fluxo completo):
1. Terminar o `SuggestionCreateRequest` (record): gameId e userId como `Integer` com @NotNull, copies com @NotNull + @Min(1), votingDays com @NotNull + @Min(1) + @Max(30)
2. Criar o `SuggestionResponse` (DTO de saída)
3. Criar exceções: jogo/usuário não encontrado e já existe sugestão ativa para o jogo (409)
4. `SuggestionService.criar`: buscar User e Game, checar `existsByGameIdAndStatus(..., EM_VOTACAO)`, calcular o prazo, salvar
5. `SuggestionController`: POST /suggestions devolvendo 201
6. Testar no requests.http

## Pendências Técnicas
- Conferir o nome da coluna do avatar em `User` (`@Column(name = "avatr_url")` está com erro de digitação)
- Conferir a versão do Java (pom diz 25, documentação antiga dizia 21)
- Acento na mensagem "Jogo já cadastrado" (conferir o resultado no teste)
- Regra "uma sugestão ativa por jogo" também no banco (índice único parcial no Postgres), além do Service
- SteamService: tratar falha de rede/timeout da Steam e resposta nula
- Estratégia de imagens e de preços (persistir ou consultar sob demanda)
- Avaliar DELETE
- Preencher o README.md

## Futuro
- Votos (APOIO / NAO_APOIO, voto alterável enquanto a votação está aberta, um voto por usuário por sugestão)
- Encerrar votação (aprovar/rejeitar) e fluxo pendente de compra → comprado
- Login pela Steam (OpenID) e usuários da família
- Comentários nas sugestões, compras parciais, cargos e permissões, quórum configurável

## Decisões Arquiteturais
- Controller não acessa Repository diretamente.
- Controller conversa apenas com o Service; o Service orquestra Repository e SteamService.
- Service contém regras de negócio.
- A Steam é uma API externa (chamada HTTP no SteamService), não um repository.
- DTOs separados das entidades; uso de record para DTOs.
- DTOs da Steam espelham o formato deles e não devem ser expostos pela nossa API.
- steamAppId tratado como String, identificador externo.
- Checagens baratas (banco) antes das caras (chamada HTTP à Steam).
- Entidades sem setters públicos: mudanças por métodos com nome de regra de negócio.
- requests.http mantido no repositório.
- Um commit por etapa concluída, mensagem em inglês.