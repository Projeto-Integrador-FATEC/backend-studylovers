# Backend Study Lovers

## Responsabilidade

Este projeto é um microsserviço Spring Boot dedicado a recomendações de cursos. O Next.js continua responsável pelos fluxos da plataforma e pelo domínio integrado ao Supabase. O backend Java não mantém banco de dados próprio nem administra cursos, usuários ou imagens.

## Componentes

- `AgentController`: expõe health check e as rotas de recomendação.
- `AgentService`: valida a solicitação, aplica o escopo de recomendação, trata comparações simples de carga horária e coordena a execução do agente.
- `RecomendacaoAgent`: monta o agente Google ADK com o catálogo e o histórico enviados pelo Next.js; usa Gemini para recomendações generativas.
- `ApiKeyAuthFilter` e `SecurityConfig`: protegem as rotas do agente com a chave interna compartilhada entre serviços. O health check é público.
- `CorsConfig`: permite origens locais `http://localhost:*`.

## Contrato REST

Porta local padrão: `4080`.

| Método | Rota | Acesso | Finalidade |
|---|---|---|---|
| `GET` | `/api/agent/health` | Público | Health check do microsserviço |
| `POST` | `/api/agent/recomendar` | Chave interna | Recomendação de curso |
| `POST` | `/api/agent/recomendacao` | Chave interna | Alias compatível da rota de recomendação |

O Next.js envia JSON com `mensagem`, `catalogo` e `historico`, além de campos opcionais de usuário, nome e conversa. A autenticação usa o cabeçalho `X-Internal-Key`. A resposta contém `resposta` e `conversationId`.

O serviço não persiste catálogo nem histórico. O Next.js fornece esse contexto a cada chamada. Imagens são armazenadas e lidas pelo Next.js via Supabase Storage; este projeto não recebe uploads nem publica arquivos locais.

## Configuração

- `server.port`: porta HTTP; padrão `4080`.
- `INTERNAL_API_KEY`: chave compartilhada com o Next.js. Configure o mesmo valor nos dois serviços.
- `GOOGLE_API_KEY` ou `GEMINI_API_KEY`: credencial para chamadas ao Gemini.
- `STUDYLOVERS_AI_MODEL`: modelo do agente; o padrão está definido em `application.properties`.

Não versione arquivos `.env` ou credenciais. O arquivo local `.env` é ignorado pelo Git; `env.bat` ajuda a carregar a chave do Gemini em sessões Windows.

## Build e testes

Use `mvnw.cmd test` no Windows ou `./mvnw test` em ambientes Unix. Os testes de serviço e controller não precisam chamar a API externa do Gemini.
