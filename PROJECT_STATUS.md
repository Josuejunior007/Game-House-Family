# Game House Family - Status do Projeto

## Informações Gerais

**Projeto:** Game House Family

**Objetivo:** Sistema para gerenciamento de sugestões e votação de jogos para grupos de Steam/Família Steam.

**Última atualização:** 2026-10-19

## Stack Atual
- Java (pom.xml configurado com 25 - conferir qual versão está instalada)
- Spring Boot 4.1.1
- Spring Data JPA
- PostgreSQL
- Supabase
- Maven
- IntelliJ IDEA
- DBeaver (para visualizar o banco)

## Situação Atual

### Banco de Dados
- PostgreSQL hospedado no Supabase
- Conexão funcionando
- Persistência validada
- Ids gerados em saltos de 50 (SEQUENCE do Hibernate reserva blocos de 50). Comportamento normal.

### Entidade Game
Campos:
- id
- steamAppId (String)
- name

Decisões:
- steamAppId é identificador externo da Steam e não deve ser alterado.
- steamAppId é **String** no nosso lado (entidade, repository, request). O `int` fica apenas no `SteamGameData`, que espelha o JSON da Steam.
- name pode ser atualizado.

## Arquitetura
Pacotes:
- controller
- dto
- model
- repository
- service

## Repository
### GameRepository
Utilização atual:
- save()
- findAll()
- findById()
- existsBySteamAppId(String) (novo) - verifica se o jogo já está cadastrado

## Service
### GameService
Dependências: GameRepository e SteamService (injetados pelo construtor).

Métodos implementados:
- cadastrar(GameCreateRequest):
    1. verifica no banco se o steamAppId já existe (existsBySteamAppId)
    2. busca o jogo na Steam via SteamService (orElseThrow se não existir)
    3. cria o Game com o nome vindo da Steam
    4. salva no banco
- listar()
- buscarPorID()
- atualizar()

Erros hoje: ainda são `RuntimeException` genéricas (viram 500).

### SteamService
✅ Implementado e testado
- Usa `RestClient` com base `https://store.steampowered.com`
- Método `buscarJogo(String appId)` retorna `Optional<SteamGameData>`
- Chama `/api/appdetails?appids={id}&cc=br&l=portuguese` (preço em BRL, texto em português)
- A resposta da Steam vem embrulhada por uma chave igual ao AppID, por isso o tipo de retorno é `Map<String, SteamAppResponse>`
- `success: false` ou chave ausente → `Optional.empty()`
- Preço vem em **centavos** (4760 = R$ 47,60)
- Jogos gratuitos não possuem `price_overview` (fica null)

## Controller
### GameController
Endpoints implementados:

#### POST /games
✅ Funcionando
- Agora recebe **apenas** `{ "steamAppId": "..." }` (GameCreateRequest)
- O nome vem da Steam
- Testado com sucesso (ACE COMBAT 8 cadastrado só com o AppID)
- Jogo já cadastrado → hoje devolve 500 (deveria ser 409)
- Jogo inexistente na Steam → hoje devolve 500 (deveria ser 404)
- Sucesso devolve 200 (deveria ser 201)

#### GET /games
✅ Funcionando

#### GET /games/{id}
✅ Funcionando
- Jogo encontrado → 200
- Jogo inexistente → 404

#### PATCH /games/{id}
✅ Funcionando
- Atualiza apenas o nome
- Game encontrado → 200
- Game inexistente → 404

### SteamTesteController (TEMPORÁRIO)
- GET /steam/{appId}
- Criado só para testar o SteamService
- **Deve ser apagado** (expõe o DTO da Steam direto)

## DTOs Internos
### GameUpdateRequest
Campos:
- name

### GameCreateRequest (novo)
Campos:
- steamAppId

## Arquivo de Testes
### requests.http
Versionado no projeto para testes dos endpoints.
- Os links `<> 2026-...json` no final são histórico de respostas do IntelliJ e podem ser apagados.

## Integração com Steam

### Endpoint usado
https://store.steampowered.com/api/appdetails?appids={APP_ID}&cc=br&l=portuguese

### Estrutura JSON
```
{ "<APP_ID>": { "success": true, "data": { name, steam_appid, price_overview } } }
```

## DTOs Steam

### SteamPriceData
Campos:
- currency
- initial
- finalPrice (@JsonProperty("final"))
- discountPercent (@JsonProperty("discount_percent"))

### SteamGameData
Campos:
- name
- steamAppId (@JsonProperty("steam_appid"))
- priceOverview (@JsonProperty("price_overview"))

### SteamAppResponse
Campos:
- success
- data

## Próximo Passo
Tratamento de erros do cadastro:
1. Criar exceções próprias (pacote `exception`): `GameJaCadastradoException` e `GameNaoEncontradoNaSteamException` (extends RuntimeException).
2. Trocar os dois `throw new RuntimeException(...)` do `GameService.cadastrar` por elas.
3. Criar um `@RestControllerAdvice` que traduz as exceções: já cadastrado → 409, não existe na Steam → 404.
4. Fazer o POST /games devolver 201 Created.

## Pendências Técnicas
- Apagar o `SteamTesteController` temporário
- POST /games ainda recebe e devolve a entidade `Game` direto; criar um DTO de resposta
- Conferir a versão do Java (pom diz 25, documentação antiga dizia 21)
- Validação de entrada (`spring-boot-starter-validation`, ex.: @NotBlank no name do PATCH)
- Limpar imports sem uso no GameService e GameController
- Estratégia de imagens e de preços (persistir ou consultar sob demanda)
- Avaliar DELETE
- Preencher o README.md

## Futuro
- Usuários (login pela Steam / OpenID)
- Sugestões (uma sugestão ativa por jogo, prazo de votação de até 1 mês, quantidade de cópias)
- Votações (APOIO / NAO_APOIO, voto alterável enquanto aberta)
- Família Steam

## Decisões Arquiteturais
- Controller não acessa Repository diretamente.
- Controller conversa apenas com o Service; o Service orquestra Repository e SteamService.
- Service contém regras de negócio.
- A Steam é uma API externa (chamada HTTP no SteamService), não um repository.
- DTOs separados das entidades.
- Uso de record para DTOs.
- DTOs da Steam espelham o formato deles e não devem ser expostos pela nossa API.
- steamAppId tratado como identificador externo.
- Checagens baratas (banco) antes das caras (chamada HTTP à Steam).
- requests.http mantido no repositório.