# Game House Family - Status do Projeto

## Informações Gerais

**Projeto:** Game House Family

**Objetivo:** Sistema para gerenciamento de sugestões e votação de jogos para grupos de Steam/Família Steam.

## Stack Atual
- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Supabase
- Maven
- IntelliJ IDEA

## Situação Atual

### Banco de Dados
- PostgreSQL hospedado no Supabase
- Conexão funcionando
- Persistência validada

### Entidade Game
Campos:
- id
- steamAppId
- name

Decisões:
- steamAppId é identificador externo da Steam e não deve ser alterado.
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

## Service
### GameService
Métodos implementados:
- cadastrar()
- listar()
- buscarPorID()
- atualizar()

### SteamService
- Classe criada
- Ainda sem implementação

## Controller
### GameController
Endpoints implementados:

#### POST /games
✅ Funcionando

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

## DTOs Internos
### GameUpdateRequest
Campos:
- name

## Arquivo de Testes
### requests.http
Versionado no projeto para testes dos endpoints.

## Integração com Steam

### Objetivo
Permitir obter informações de um jogo utilizando apenas o AppID da Steam.

Exemplo:
2138720 → REMATCH

### Endpoint estudado
https://store.steampowered.com/api/appdetails?appids={APP_ID}

### Estrutura JSON analisada
- success
- data
    - name
    - steam_appid
    - price_overview

## DTOs Steam criados

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
Implementar SteamService para:
1. Fazer requisição HTTP para a Steam.
2. Receber o JSON.
3. Converter em DTOs.
4. Testar consulta do AppID 2138720.
5. Obter o nome REMATCH via código.

## Funcionalidades Pendentes

### Game
- Integração Steam
- Cadastro usando apenas AppID
- Estratégia de imagens
- Estratégia de preços
- Avaliar DELETE

### Futuro
- Usuários
- Sugestões
- Votações
- Família Steam

## Decisões Arquiteturais
- Controller não acessa Repository diretamente.
- Service contém regras de negócio.
- DTOs separados das entidades.
- Uso de record para DTOs.
- steamAppId tratado como identificador externo.
- requests.http mantido no repositório.
