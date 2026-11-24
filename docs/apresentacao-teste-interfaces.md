# Apresentação — Teste de Interfaces Humanas

**Game House Family** · grupo de 3 · vale 2 pontos na AV2

> **Resumo:** o projeto já entrega o essencial do enunciado — é um sistema **real**, com 6 telas + versão mobile de evidência, e as 4 boas práticas aparecem de forma demonstrável na interface e no código. O que falta é **material de apresentação**: slides, dados preparados e um plano B para a demo ao vivo. O roteiro completo está na §4.

---

## 1. Checklist do enunciado

| Exigência do slide | Situação | Onde está |
|---|---|---|
| Desenvolver um **sistema real ou fictício** (site, app, sistema acadêmico…) | ✅ real e funcionando | Spring Boot + React; jar em `target/`, front em `frontend/` |
| Apresentar a **ideia**: para que serve | ✅ | `README.md` (abertura e "Telas") |
| **Quem usa** | ✅ 2 papéis: dono e membro | RF-01/RF-02 no `README.md` |
| **Principais telas** | ✅ 4 abas + login | `docs/screenshots/` (6 telas + mobile) |
| Mostrar **consistência** | ✅ demonstrável | matriz §3.1 |
| Mostrar **feedback** | ✅ demonstrável | §3.2 |
| Mostrar **prevenção e tratamento de erros** | ✅ os dois lados | §3.3 |
| Mostrar **simplicidade** | ✅ | §3.4 |
| **Evidência**: telas, protótipo ou demonstração | ✅ telas + app rodando | screenshots e demo ao vivo |

**Como ler o enunciado:** ele pede uma **análise das 4 boas práticas com evidência visual**, não um teste com usuários. Mas o nome da disciplina é *Teste de Interfaces Humanas*, então tenha uma frase pronta caso o professor cobre "cadê o teste?" (§7, pergunta 2).

> ⚠️ O slide lista **três** itens na linha ("consistência, feedback, prevenção e tratamento de erros e simplicidade") mas fala em **4 práticas**. A leitura mais provável é que "prevenção e tratamento de erros" seja **uma** prática. A apresentação abaixo trata como **4 blocos**, e dentro do terceiro separa **prevenção** (antes do erro) de **tratamento** (depois do erro) — assim funciona nas duas interpretações.

---

## 2. Diagnóstico: o que você tem e o que falta

### 2.1 Já está pronto

| Ativo | Detalhe |
|---|---|
| **7 capturas de tela** | `login`, `familia`, `sugestoes`, `convites`, `catalogo`, `busca-steam` (modal) e `mobile` 390×844. Todas 1280×900, exceto mobile |
| **App completo** | login Steam, onboarding, família, convites, catálogo com busca, sugestão com prazo, votação, compra |
| **Prova de consistência técnica** | um único formato de erro na API (`{status, message, fields}`) e um único parser no front (`frontend/src/api.ts:63-67`) |
| **Prova de prevenção** | botão "Já em votação" desabilitado (`CatalogView.tsx:150-153`), SteamID com 17 dígitos (`FamilyView.tsx:343,351`), confirmações inline (`SuggestionCard.tsx:153-174`) |
| **Prova de feedback** | toasts com `aria-live` (`Toast.tsx:48`), badge de convites (`Header.tsx:90-94`), esqueletos de carregamento, rótulos "Criando…"/"Convidando…" |
| **Prova de simplicidade** | login de 1 botão, 4 abas, 3 passos para sugerir, 1 coluna no mobile (`styles.css:1634`) |
| **Testes** | 35 testes, **0 falhas** (rodado em 24/11/2026): 5 de domínio + 13 de família + 12 de sugestão + 5 de voto |
| **Verificação da interface** | navegador headless em 1280 px e 390 px, sem erro de console (`PROJECT_STATUS.md:97-100`) |
| **Ambiente** | JDK 25 em `~/.jdks/openjdk-25.0.2`, jar já compilado, `node_modules` e `dist` prontos |

### 2.2 O que falta (em ordem de risco)

| # | Falta | Risco | Ação |
|---|---|---|---|
| 1 | **Slides** | alto — sem eles a fala fica solta | usar o roteiro de 11 slides da §6 |
| 2 | **Demo não ensaiada + login Steam** | alto — é o que mais dá errado ao vivo | entrar na Steam **antes**, não reiniciar a API, gravar vídeo de 90 s |
| 3 | **Credenciais do banco fora do shell** | alto — sem elas a API não sobe | rodar pelo IntelliJ (run config já existe) ou exportar as 3 variáveis (§8) |
| 4 | **2 screenshots com toast em cima do conteúdo** | médio — `familia.png` e `catalogo.png` têm avisos sobrepondo botões | refazer essas duas sem toast (ou recortar) |
| 5 | **Dados de demonstração** | médio — o banco pode ter mudado desde as capturas | preparar 1 família com 2 membros, 1 sugestão aberta e 1 comprada (§8) |
| 6 | **Teste formal com usuários** | baixo — não é exigido | se quiser reforçar, fazer uma avaliação heurística curta (§7, resposta 2) |
| 7 | **Inconsistência conhecida**: no modal "Nova sugestão" dá para escolher um jogo que já está em votação (o catálogo bloqueia, o modal não) | baixo — o servidor responde 409 | assumir como limitação na §4.5 **ou** corrigir antes |

> **Sobre os testes:** `./mvnw test` roda em ~4 s e termina com `Tests run: 35, Failures: 0, Errors: 0` + `BUILD SUCCESS`. Se rodar num terminal muito restrito, o Mockito pode falhar ao se anexar ao processo — isso é o terminal, não os testes; rode num terminal normal ou no IntelliJ. Na apresentação, mostre só a linha do `Results`, não os *warnings* do JDK.

---

## 3. Matriz de evidências — as 4 práticas

Use esta seção para montar os slides 5 a 9 e para responder perguntas. Cada prática tem: **o que é**, **onde provar**, **o que mostrar na tela** e **uma frase para decorar**.

### 3.1 Consistência

*O usuário não precisa reaprender a interface a cada tela; a mesma coisa tem sempre o mesmo nome, a mesma cor e o mesmo lugar.*

| Evidência | Onde ver |
|---|---|
| Mesmo cabeçalho e mesmas 4 abas em todas as telas, com a aba atual destacada (`aria-current`) | `Header.tsx:52-95` |
| Tokens únicos de cor, raio e sombra — nada de cor solta | `styles.css:5-21` |
| Mesmo significado visual de botão: azul cheio = principal, contorno = secundária, vermelho = destrutiva | `styles.css` (`.botao--primario`, `--fantasma`, `--perigo`) |
| Status sempre com **palavra + cor**: "Em votação", "Aprovada", "Rejeitada", "Comprada" | `format.ts:54-59`, selos no `SuggestionCard` |
| Um **único formato de erro** na API e um **único parser** no front | `api.ts:63-67`; `README.md` ("Formato de erro") |
| Mesmo componente de erro em toda tela, com "Tentar de novo" | `CatalogView.tsx:99-106`, `InvitesView.tsx:77-84`, `FamilyView.tsx:195-202` |
| Datas em pt-BR e preço em BRL sempre iguais | `format.ts:4-24` |
| Vocabulário fixo: "Apoio"/"Não apoio", "Dono"/"Membro", "Sugestões" | `types.ts`, `format.ts` |

**Mostrar:** navegar pelas 4 abas sem mudar de layout; apontar o mesmo botão azul; apontar o selo de status.
**Frase:** "A pessoa aprende a interface uma vez. O cabeçalho, o botão principal e o erro são sempre iguais; o status nunca é só cor, é sempre palavra + cor."

### 3.2 Feedback

*Toda ação tem resposta visível, na hora, no lugar onde a pessoa olhou.*

| Evidência | Onde ver |
|---|---|
| Toast de sucesso/erro/info, 3,5 s (sucesso) e 6,5 s (erro), clicável para dispensar | `Toast.tsx:27-34,48-59` |
| Contador de convites no cabeçalho, com `aria-label` | `Header.tsx:90-94` |
| Botão muda de rótulo e desabilita durante a ação: "Criando…", "Convidando…", "Entrando…" | `NewSuggestionModal.tsx:198-200`, `FamilyView.tsx:354` |
| Esqueleto de carregamento em vez de tela branca | `CatalogView.tsx:162-167`, `SuggestionsView.tsx:135-140` |
| Spinner na busca enquanto consulta | `CatalogView.tsx:96` |
| Voto muda contador, barra proporcional e `aria-pressed` | `SuggestionCard.tsx:81-116` |
| Prazo em linguagem natural ("faltam 3 dias", "faltam 40 min"), com estado urgente e vencido | `format.ts:33-45` |
| Estado vazio que ensina o próximo passo, em vez de tela em branco | `CatalogView.tsx:168-177`, `InvitesView.tsx:88-98` |
| Preço: "Consultando a Steam…" e erro isolado no item, sem derrubar a lista | `CatalogView.tsx:196-199` |
| Erro de API inacessível na tela de login, com causa e o que conferir | `LoginScreen.tsx:23-31` |

**Mostrar:** copiar o SteamID (toast), convidar (toast + item na lista), votar (número e barra mudam), esperar a busca (spinner).
**Frase:** "O sistema nunca fica calado: quando espera, mostra que está esperando; quando termina, confirma o que aconteceu; quando falha, diz o que houve."

### 3.3 Prevenção e tratamento de erros

**Prevenção — antes do erro acontecer:**

| Evidência | Onde ver |
|---|---|
| Campo de SteamID só aceita dígitos, corta em 17 e o botão só liga com 17 | `FamilyView.tsx:343,351` |
| `inputMode="numeric"` abre teclado numérico no celular | `FamilyView.tsx:344` |
| Jogo com votação aberta: botão vira "Já em votação" e fica desabilitado, com o motivo no `title` | `CatalogView.tsx:150-153,241-245` |
| Servidor também barra o duplicado (índice único parcial + 409) — a UI não é a única defesa | `README.md` RN-04/09 |
| Quem não tem família não vê Catálogo/Sugestões — o app não oferece ação inválida | `App.tsx:53-55` |
| Dono não vê "Sair da família", porque a regra proíbe | `FamilyView.tsx:388-453` |
| Ações sem volta pedem confirmação **no lugar**, com botão que diz o que faz | `SuggestionCard.tsx:153-174`; `FamilyView.tsx:298-327,395-413` |
| Busca com debounce de 400 ms e descarte de resposta velha — evita resultado trocado | `useGameSearch.ts:27-40` |
| Prazo em chips (1/3/7/14/30) + campo limitado a 1–30, em vez de data livre | `NewSuggestionModal.tsx:17,167-190` |

**Tratamento — depois do erro:**

| Evidência | Onde ver |
|---|---|
| 9 códigos HTTP mapeados para mensagens em português; validação devolve o **campo** culpado | `GlobalExceptionHandler`; `README.md` |
| Erro de validação aparece **embaixo do input**, mantendo o que foi digitado | `NewSuggestionModal.tsx:155-190` |
| `fields` do servidor caem no formulário; sem `fields`, vira alerta geral | `NewSuggestionModal.tsx:66-74` |
| Falha de rede vira mensagem clara, não tela travada | `api.ts:43-47` |
| Steam fora do ar: catálogo responde com o que já está no banco | `GameService.java:71-79` |
| Capa que não carrega vira placeholder com o nome do jogo | `Capa.tsx:17-38` |
| Botão "Tentar de novo" recarrega só a seção que falhou | `InvitesView.tsx:80-83` |

**Mostrar:** digitar 16 dígitos no convite (botão desligado); tentar sugerir jogo já em votação (botão desligado); colocar 0 cópias (erro no campo); apontar o JSON de erro no slide.
**Frase:** "A gente prefere impedir a jogada errada a explicar o erro depois. Quando o erro acontece mesmo assim, ele vem em português, no campo certo, sem apagar o que a pessoa digitou."

### 3.4 Simplicidade

*Tirar tudo o que não ajuda a tarefa.*

| Evidência | Onde ver |
|---|---|
| Login com **um** botão: sem cadastro, sem senha nova, sem formulário | `LoginScreen.tsx:33-36` |
| 4 abas no total; quem não tem família vê só 2 e cai no onboarding | `App.tsx:53-55` |
| Criar sugestão = 3 decisões: jogo, cópias, prazo (chips, não calendário) | `NewSuggestionModal.tsx` |
| Ação principal única por tela ("Nova sugestão", "Convidar", "Criar sugestão") | telas |
| Textos curtos em pt-BR, explicando o efeito da ação em vez de jargão | `SuggestionCard`, `FamilyView` |
| Mobile: mesma interface em 1 coluna, sem menu escondido nem tela separada | `styles.css:1634-1731`, `mobile.png` |
| Preço só é consultado quando a pessoa pede ("Ver preço"), não na abertura | `CatalogView.tsx:228-235` |

**Mostrar:** a tela de login; o onboarding; o mobile ao lado do desktop.
**Frase:** "Não tem configuração, não tem menu lateral, não tem tela de preferências. O caminho mais curto entre 'quero sugerir um jogo' e 'a votação está aberta' tem três decisões."

---

## 4. Roteiro de 10 minutos (versão recomendada)

**Divisão:** A abre e fecha · B demonstra · C analisa as práticas.
**Slides:** 1–2 (A) · 3 (A) · 4 em diante com o app (B) · 5–9 (C) · 10–11 (A).

| Tempo | Quem | O quê | Slide/tela |
|---|---|---|---|
| 0:00–1:00 | A | Problema, o que é o sistema, quem usa | 1–2 |
| 1:00–2:00 | A | As 4 telas e a promessa: analisar as 4 práticas | 3 |
| 2:00–5:30 | B | Demo guiada (login no slide + 6 passos no app) | app ao vivo |
| 5:30–8:30 | C | As 4 práticas, 45 s cada, com evidência | 5–9 |
| 8:30–9:40 | A | Limitações honestas + o que aprendemos | 10 |
| 9:40–10:00 | A | Fechamento e perguntas | 11 |

> Se o professor pedir para "mostrar o sistema" antes da análise, inverta B e C — o roteiro funciona nos dois sentidos.

### 4.1 Abertura (A) — 1 min

> "Boa noite. A gente vai apresentar o **Game House Family**, um sistema real que já está rodando — não é maquete.
>
> O problema é este: quando uma família compartilha a conta Steam, decidir o que comprar acontece no WhatsApp. Alguém manda o print de um jogo, três pessoas respondem, e duas semanas depois ninguém lembra o que foi combinado nem quantas cópias eram.
>
> O Game House Family resolve isso: a família entra com a Steam, o dono cadastra as pessoas e cada decisão de compra vira uma **sugestão com prazo e votação**. Quem usa são dois papéis: o **dono**, que organiza a família, e os **membros**, que sugerem, votam e acompanham."

### 4.2 Visão geral (A) — 1 min · slide 3

> "São quatro telas: **Sugestões**, **Catálogo**, **Família** e **Convites** — e o login.
>
> A nossa apresentação tem duas partes. Primeiro o B mostra o sistema funcionando. Depois a C pega o que vocês viram e mostra como cada tela cumpre as quatro boas práticas do enunciado: **consistência**, **feedback**, **prevenção e tratamento de erros** e **simplicidade**. No fim, a gente fala do que ainda não está pronto."

### 4.3 Demonstração (B) — 3,5 min

**Antes de começar:** já estar logado como **dono** (Ana), na aba **Família**, zoom 110–125%, notificações desligadas. Ter uma sugestão aberta e uma comprada no banco.

**Passo 0 — Login (use o slide, não o app).**
> "A única porta de entrada é um botão. Se você tem Steam, você já tem conta: não tem cadastro, não tem senha nova."
*Por que no slide:* o login real sai do site para a Steam. Mostrar ao vivo é o maior risco da apresentação.

**Passo 1 — Família / onboarding.**
> "Quem entra sem família não cai numa tela vazia: o app te leva para a Família e mostra o caminho — criar a família ou ver os convites. As abas de Catálogo e Sugestões nem aparecem, porque sugerir um jogo para uma família que não existe não faz sentido."

**Passo 2 — Copiar o SteamID.**
Clique em **Copiar**.
> "O sistema confirma que copiou. Ninguém precisa decorar 17 números — e o dono recebe esse ID pronto para passar adiante."

**Passo 3 — Convite com prevenção.**
Digite 16 dígitos e pause; complete o 17º.
> "Olha o botão: ele fica desligado até o ID estar completo. O campo só aceita número e corta em 17. A pessoa não descobre o erro depois de clicar: o sistema não deixa ela errar."

Convide e mostre o toast + o convite na lista "Convites pendentes".
> "E toda ação termina com uma confirmação do que aconteceu — no mesmo lugar, com o mesmo formato, em qualquer tela."

**Passo 4 — Convites, do lado de quem recebe (pode ser o screenshot).**
> "Do outro lado, o convite não fica escondido: aparece um número na aba. A pessoa não precisa adivinhar que tem convite esperando."

**Passo 5 — Catálogo e busca.**
Digite `portal` e espere.
> "A busca espera 400 milissegundos para não disparar uma chamada por tecla, e descarta resposta velha: se eu digitar rápido, o resultado da letra 'p' não sobrescreve o de 'portal'."

Aponte o botão desabilitado de um jogo que está em votação.
> "Esse jogo já tem votação aberta, então o botão já nasce desligado e diz por quê. De novo: o erro é impedido antes, não explicado depois."

**Passo 6 — Nova sugestão com validação.**
Abra **Sugerir** em outro jogo. No campo de cópias, digite `0` e clique em criar.
> "O erro aparece embaixo do campo, em português, e o formulário continua preenchido. O servidor valida a mesma regra: a mensagem vem pronta do back, no campo certo."

Corrija para 1, escolha o prazo de 7 dias e crie.
> "Criar uma sugestão são três decisões: o jogo, quantas cópias e o prazo. O prazo são cinco opções, não um campo de data."

**Passo 7 — Votação e compra.**
No card, clique em **Apoio**.
> "O voto muda o contador e a barra na hora — sem precisar fazer conta."

Clique em **Encerrar votação** (você é o criador), mostre a confirmação inline e confirme. Depois **Já comprei**.
> "Encerrar não tem volta, então pede confirmação ali mesmo, com o botão dizendo o que faz — não é um 'OK/Cancelar' genérico. E o status muda para Aprovada, depois Comprada, sempre com a mesma palavra e a mesma cor."

**Frase de saída da demo:**
> "Isso é o fluxo inteiro: entrar, formar a família, escolher o jogo, votar e comprar. A C vai voltar nesses pontos e mostrar a prática por trás de cada um."

**Plano B por passo:** se o app travar, o login falhar ou a internet cair, **não conserte ao vivo**. Diga "vou mostrar na captura" e use o PDF das telas. Tenha o vídeo de 90 s aberto em outra janela.

### 4.4 Análise (C) — 3 min · slides 5–9

Uma prática por slide (~45 s). Cada slide: **1 frase de definição + 2 capturas + 3 bullets**.

**Consistência (slide 5).**
> "Consistência é não precisar reaprender a interface a cada tela. O cabeçalho, as abas e o lugar da ação principal são sempre os mesmos. O botão azul cheio é sempre a ação principal, o contorno é secundária e o vermelho é destrutiva. O status do jogo é sempre a mesma palavra com a mesma cor — nunca só cor, porque cor sozinha exclui quem não distingue verde de vermelho.
>
> E isso não é só visual: o back devolve **um** formato de erro, com status, mensagem e os campos que falharam, e o front tem **um** parser. Então a mensagem de erro é igual em qualquer tela."

**Feedback (slide 6).**
> "Feedback é o sistema dizer o que aconteceu, na hora. Copiou o ID: confirma. Criou a sugestão: o card aparece com o prazo. Votou: o número e a barra mudam. Enquanto espera, o botão muda de rótulo — 'Criando…', 'Convidando…' — e desabilita, então não dá para clicar duas vezes. E quando ainda não tem nada, a tela não fica vazia: ela explica o que fazer."

**Prevenção (slide 7).**
> "Prevenção é impedir o erro antes dele acontecer. O SteamID só aceita 17 dígitos e o botão só liga quando está completo. O jogo que já está em votação aparece com o botão desligado. E o que não tem volta — remover membro, excluir família, encerrar votação — pede confirmação ali mesmo, sem modal que cobre a tela.
>
> Detalhe importante: o dono não vê o botão 'Sair da família', porque a regra não permite. A interface não oferece o que o sistema vai recusar."

**Tratamento (slide 8).**
> "Tratamento é o que acontece depois do erro. O back mapeia nove códigos HTTP para mensagens em português e diz qual campo falhou; o front usa isso para colocar o erro embaixo do input, sem apagar o que a pessoa digitou. Se a API estiver fora do ar, a tela de login diz exatamente isso e o que conferir, em vez de fingir que a senha está errada. Se a Steam cair, o catálogo responde com o que já está salvo no banco. Se a capa não carregar, aparece um placeholder — a tela não quebra."

**Simplicidade (slide 9).**
> "Simplicidade é o que não está lá. Login é um botão. São quatro abas, e quem não tem família vê duas. Criar uma sugestão são três decisões, e o prazo é um chip, não um calendário. No celular, a mesma interface vira uma coluna, sem menu escondido e sem tela separada — a pessoa não aprende um segundo app."

### 4.5 Limitações e fechamento (A) — 1 min 10 s · slides 10–11

> "O que a gente sabe que ainda não está pronto:
>
> 1. **A Steam não oferece API da família oficial**, então a família é gerenciada pelo app, amarrada ao SteamID64. Isso está explicado no README.
> 2. O app é de **uma família só** — para várias, precisaria de `family_id` nos jogos e sugestões.
> 3. Ações destrutivas têm confirmação, mas **não têm desfazer**.
> 4. O preço vem da Steam na hora e pode demorar de 5 a 10 segundos — a interface avisa, mas não tem cache.
> 5. Uma inconsistência que a gente já mapeou: no modal de nova sugestão ainda dá para escolher um jogo que já está em votação. O catálogo bloqueia; o modal ainda não. O servidor protege com 409, mas o certo é bloquear na tela também.
>
> O que a gente aprendeu: a maior parte do trabalho de usabilidade não foi fazer a tela bonita, foi decidir **o que não deixar acontecer** — e garantir que toda ação tivesse uma resposta visível. O sistema é pequeno, mas cada uma das quatro práticas está no código, não só no slide. Obrigado — perguntas?"

---

## 5. Versões de 5 e 15 minutos

**5 minutos** (só o essencial): A 40 s (problema + 4 telas) · B 90 s (convite com botão desabilitado + votar) · C 90 s (4 práticas, 20 s cada, uma evidência por prática) · A 20 s (1 limitação + obrigado).

**15 minutos**: mantenha o roteiro e acrescente 3 min de **avaliação heurística** (tabela abaixo), 1 min de **acessibilidade** (foco visível, `aria-live`, `aria-pressed`, contraste) e 3 min de Q&A. Se possível, faça um teste-relâmpago com 2 colegas: peça "crie uma sugestão para um jogo" e cronometre; relate onde travaram.

| Heurística | Evidência no sistema |
|---|---|
| Visibilidade do status | toast, badge, spinner, esqueleto, prazo |
| Correspondência com o mundo real | "Apoio/Não apoio", "Já comprei", prazo em dias |
| Controle e liberdade | confirmações, "Trocar" jogo no modal, cancelar |
| Consistência e padrões | §3.1 |
| Prevenção de erros | §3.3 (prevenção) |
| Reconhecer em vez de lembrar | SteamID copiável, capas, status por extenso |
| Flexibilidade e eficiência | busca com cache, prazo em chips |
| Estética e minimalismo | 4 abas, 1 ação principal por tela |
| Ajuda a reconhecer/diagnosticar erros | §3.3 (tratamento) |
| Ajuda e documentação | textos de apoio nas telas, estado vazio que ensina |

---

## 6. Slides sugeridos (11)

| # | Slide | Conteúdo | Quem |
|---|---|---|---|
| 1 | Capa | Game House Family · "Teste de Interfaces Humanas" · integrantes | A |
| 2 | Problema | 3 bullets: decisão no WhatsApp, sem registro, sem prazo | A |
| 3 | Ideia e telas | o que é, dono/membro, miniatura das 4 telas | A |
| 4 | "Vamos ver funcionando" | título + o fluxo em 5 passos (entrar → família → escolher → votar → comprar) | B |
| 5 | Consistência | `familia.png` + `sugestoes.png` + 3 bullets | C |
| 6 | Feedback | `convites.png` (badge) + `sugestoes.png` (barra) + toast | C |
| 7 | Prevenção | print do campo de convite + botão "Já em votação" + confirmação | C |
| 8 | Tratamento | print do erro no campo + JSON `{status, message, fields}` | C |
| 9 | Simplicidade | `login.png` + `mobile.png` lado a lado | C |
| 10 | Limitações e próximos passos | 5 bullets honestos | A |
| 11 | Obrigado / perguntas | nome do repo + QR do GitHub (opcional) | A |

> Repositório: `github.com/Josuejunior007/Game-House-Family`. Se for mostrar, deixe o link no slide 11 — não leia a URL em voz alta.

---

## 7. Perguntas prováveis e respostas

**1. "Por que só login pela Steam? E quem não tem conta?"**
> "Porque o sistema só faz sentido para quem já tem biblioteca na Steam — o login é a identidade que amarra a pessoa à família. Para quem não tem conta, o sistema não teria o que gerenciar. Na demo, eu já estou logado; a API também tem um login de desenvolvimento para testes, mas ele fica desligado em produção."

**2. "E o teste com usuários? Cadê?"**
> "O enunciado pede a análise das quatro práticas com evidência de telas ou demonstração, e é isso que a gente está mostrando. O que a gente fez de verificação foi funcional: 35 testes automatizados das regras e uma passagem em navegador headless no desktop (1280 px) e no celular (390 px), sem erro de console. Teste de usabilidade formal com participantes a gente não fez — se o senhor quiser, o próximo passo seria esse, com 3 pessoas e a tarefa 'crie uma sugestão e vote'."

**3. "Como vocês sabem que é simples?"**
> "Pelo caminho: da tela inicial até a votação aberta são três decisões — jogo, cópias, prazo. Quatro abas no total, e duas delas só aparecem quando fazem sentido. A gente não mediu tempo de tarefa com usuários; o que a gente pode afirmar é que o fluxo não tem etapa opcional nem campo que não seja usado."

**4. "O que acontece se a Steam cair no meio?"**
> "A busca tenta a Steam; se falhar e já tiver resultado no banco, responde com o banco. Se não tiver, devolve 502 com mensagem em português. O preço é consultado sob demanda e o erro fica só naquele item — não derruba a lista."

**5. "Duas pessoas votando ao mesmo tempo?"**
> "O banco tem `unique (user_id, suggestion_id)` no voto e um índice único parcial que garante uma sugestão em votação por jogo. A troca de voto é um update, não um insert novo, então o placar não duplica."

**6. "Por que o dono não pode sair da família?"**
> "Porque a família ficaria sem dono. Ele precisa excluir a família ou, no futuro, transferir a posse. Hoje não existe transferência — é uma limitação conhecida."

**7. "Isso é acessível?"**
> "Parcialmente: tem `aria-live` nos avisos, `aria-pressed` nos votos, `aria-current` nas abas, foco visível e nenhum status depende só de cor. Não foi auditado com leitor de tela nem com checklist WCAG completo — é o próximo passo."

**8. "Por que o preço demora?"**
> "Porque é consultado na hora na Steam, jogo por jogo, e a Steam leva de 5 a 10 segundos. A interface avisa 'Consultando a Steam…' e deixa o resto da tela utilizável. O próximo passo é cache e consulta em lote."

**9. "Onde está a consistência no código, e não só na tela?"**
> "Em três lugares: os tokens de cor e espaçamento em `styles.css`, o formato único de erro da API e o único parser no `api.ts`, e os componentes reaproveitados (`Modal`, `Toast`, `Capa`, `SuggestionCard`). Não tem estilo inline nem mensagem de erro escrita à mão em cada tela."

**10. "Se o professor pedir para rodar agora?"**
> "A API sobe pelo IntelliJ ou pelo jar; o front é `npm run dev`. Os testes são `mvnw test`: 35 testes, zero falhas, em uns 4 segundos — se quiser, eu rodo agora."

---

## 8. Checklist de ensaio e de dia de apresentação

### Na véspera

- [ ] Refazer `familia.png` e `catalogo.png` **sem toast** sobrepondo botões (ou recortar as existentes).
- [ ] Gravar um vídeo de **90 segundos** do fluxo completo (Xbox Game Bar: `Win+G`) — é o plano B.
- [ ] Exportar as 7 telas em um PDF único — plano C.
- [ ] Preparar os dados: 1 família ("Família do Josué"), 2 membros (Ana dona, Beto membro), **1 sugestão aberta**, **1 comprada**, 1 convite pendente.
- [ ] Ensaiar 2 vezes cronometrado; ajustar o que passar do tempo.
- [ ] Confirmar quem responde o quê nas perguntas (A: produto/regras · B: fluxo/demo · C: usabilidade/código).

### No dia, antes de projetar

- [ ] Fechar notificações, Slack/WhatsApp/Discord; modo "não perturbe".
- [ ] Zoom do navegador em 110–125% (a turma precisa ler); janela ~1280×800.
- [ ] **Entrar na Steam antes** e **não reiniciar a API** depois — a sessão fica em memória e um restart desloga.
- [ ] Conferir que a API responde: abrir `http://localhost:8081/auth/me` (deve dar 401 ou o usuário, não erro de conexão).
- [ ] Deixar 2 abas abertas: app e slides. Segunda janela anônima com o **Beto** só se for mostrar o aceite do convite.
- [ ] Testar a internet do local: capas e busca vêm da Steam.

### Comandos (PowerShell)

```powershell
# 1) API — confira que o java é o 25 (`java -version`); se não for, aponte o JAVA_HOME:
#    $env:JAVA_HOME = "$env:USERPROFILE\.jdks\openjdk-25.0.2"
$env:SUPABASE_DB_URL      = "jdbc:postgresql://..."
$env:SUPABASE_DB_USERNAME = "..."
$env:SUPABASE_DB_PASSWORD = "..."
.\mvnw.cmd spring-boot:run
# ou, mais rápido, o jar já compilado:
# & "$env:JAVA_HOME\bin\java.exe" -jar target\game-house-family-1.0-SNAPSHOT.jar

# 2) Front — em outra janela
cd frontend
npm run dev        # http://localhost:5173
```

> Se preferir, rode a API pelo IntelliJ com a run config **GameHouseFamilyApplication** que já existe — só confira se as variáveis `SUPABASE_DB_*` estão no ambiente dela.

---

## Anexo — colinha para levar na mão

**Fluxo da demo (5 cliques):** Família → Copiar → Convite (16→17 dígitos) → Catálogo "portal" → Sugerir → 0 cópias (erro) → 1 cópia + 7 dias → Apoio → Encerrar → Já comprei.

**As 4 frases-chave:**
1. **Consistência** — "A pessoa aprende uma vez: mesmo cabeçalho, mesmo botão, mesmo erro, status sempre palavra + cor."
2. **Feedback** — "Nunca fica calado: esperando mostra spinner, terminou confirma, falhou explica."
3. **Prevenção/erro** — "Impede a jogada errada antes; se errar, o erro vem no campo certo, em português, sem apagar o que foi digitado."
4. **Simplicidade** — "Um botão de login, quatro abas, três decisões para sugerir, uma coluna no celular."

**Números para citar:** 4 telas + login · 2 papéis · 4 boas práticas · 35 testes · 1280 px e 390 px · 3 passos para criar uma sugestão.
