# API Contract (OpenAPI Simplificado)

Este documento descreve as assinaturas mapeadas dos serviços expostos. Todos os endpoints autenticados injetam sub-rotinas para processar o Bearer Token (`OAuth2 Resource Server`).

## 1. 🛡️ Auth (Público)

### Registrar Usuário & Tenant
- **POST** `/auth/register`
- **Security:** Nenhuma
- **Input (Payload):**
```json
{
  "name": "Jane Doe",
  "email": "jane@email.com",
  "password": "strongPassword123",
  "companyName": "Acme Corp"
}
```
- **Output (200 OK):**
```json
{
  "token": "eyJhbGci...<JWT>"
}
```

### Efetuar Login
- **POST** `/auth/login`
- **Security:** Nenhuma
- **Input (Payload):**
```json
{
  "email": "jane@email.com",
  "password": "strongPassword123"
}
```
- **Output (200 OK):**
```json
{
  "token": "eyJhbGci...<JWT>"
}
```

---

## 2. 👤 Users (Autenticado)

### Perfil Ativo
- **GET** `/api/users/me`
- **Security:** Bearer Token
- **Output (200 OK):**
```json
{
  "id": "123e4567-e89b-12d3... (User ID)",
  "name": "Jane Doe",
  "email": "jane@email.com",
  "tenantId": "987f6543-e21b... (Tenant ID)",
  "tenantName": "Acme Corp",
  "role": "OWNER"
}
```

---

## 3. 📂 Projects (Autenticado & Scope de Tenant)

### Listar Projetos (Paginado)
- **GET** `/api/projects?page=0&size=20&sort=createdAt,desc`
- **Security:** Bearer Token
- **Output (200 OK):**
```json
{
  "content": [
    {
      "id": "a1b2...",
      "name": "Projeto Alpha",
      "tenantId": "c3d4...",
      "createdByName": "Jane Doe",
      "createdAt": "2026-02-21T09:00:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true
}
```

### Criar Projeto
- **POST** `/api/projects`
- **Security:** Bearer Token
- **Input (Payload):**
```json
{
  "name": "Projeto Alpha"
}
```
- **Output (201 Created):**
```json
{
  "id": "a1b2...",
  "name": "Projeto Alpha",
  "tenantId": "c3d4...",
  "createdByName": "Jane Doe",
  "createdAt": "2026-02-21T09:00:00Z"
}
```

Mapeamento secundário padrão (Buscar, Atualizar, Deletar):
- `GET /api/projects/{id}`
- `PUT /api/projects/{id}` (Body: `{"name":"Novo Nome"}`)
- `DELETE /api/projects/{id}`

### Listar Time Entries de um Projeto Específico
- **GET** `/api/projects/{id}/time-entries`
- **Output (200 OK):** Retorna array de `TimeEntryResponseDTO`.

### Resumo Agregado do Projeto
- **GET** `/api/projects/{id}/summary`
- **Security:** Bearer Token
- **Output (200 OK):**
```json
{
  "projectId": "a1b2...",
  "totalDurationSeconds": 14520
}
```
- **Observação:** Calcula a soma total via query nativa PostgreSQL (`SUM(EXTRACT(EPOCH...))`). Considera apenas entries com `endTime` não-nulo.

---

## 4. ⏱️ Time Entries (Autenticado & Scope User/Projeto)

Rotas dividem-se entre **Ações do Live Timer** e **Ações Isoladas**.

### (Live) Iniciar Timer
- **POST** `/api/time-entries/start`
- **Security:** Bearer Token
- **Input:**
```json
{
  "projectId": "id-do-projeto...",
  "description": "Trabalhando em X"
}
```
- **Output (201 Created):** `TimeEntryResponseDTO`
```json
{
  "id": "f5e6...",
  "description": "Trabalhando em X",
  "startTime": "2026-02-21T10:00:00Z",
  "endTime": null,
  "projectId": "id-do-projeto...",
  "projectName": "Projeto Alpha",
  "createdAt": "2026-02-21T10:00:00Z"
}
```

### (Live) Parar Timer
Busca e pausa proativamente qualquer timer aberto para aquele User em cache ativo.
- **PUT** `/api/time-entries/stop`
- **Security:** Bearer Token
- **Output (200 OK):** Retorna a task encerrada.

### (Live) Buscar Timer Ativo
- **GET** `/api/time-entries/active`
- **Output (200 OK ou 204 No Content):** `TimeEntryResponseDTO` caso haja.

### Inserção Manual de Tempo Passado
- **POST** `/api/time-entries`
- **Security:** Bearer Token
- **Input:**
```json
{
  "projectId": "...",
  "description": "Daily Meeting",
  "startTime": "2026-02-21T10:00:00Z",
  "endTime": "2026-02-21T10:30:00Z"
}
```
- **Output (201 Created):** `TimeEntryResponseDTO` com a assinatura completa.

### Listar Histórico (Paginado/Não Paginado)
- **GET** `/api/time-entries`

### 💡 (Especial Inline) Patch de Célula na Grid (Atualização Parcial)
No frontend, em vez de um forms massivo, a tabela será atualizada granularmente através dos eventos *on-blur*.

- **PATCH** `/api/time-entries/{id}`
- **Security:** Bearer Token
- **Input:** Aceita qualquer subconjunto (todos opcionais), exemplo alterou apenas as horas trabalhadas:
```json
{
  "startTime": "2026-02-21T08:00:00Z",
  "endTime": "2026-02-21T12:00:00Z"
}
```
Ou alterou apenas o projeto e uma tag:
```json
{
  "projectId": "novo-id...",
  "description": "Refactoring do login"
}
```

### Deletar Entry
- **DELETE** `/api/time-entries/{id}`
