-- =============================================
-- Qronis - Initial Schema
-- =============================================

-- Tenant: escopo de projetos
CREATE TABLE tenant (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Users: tabela de usuários (evita keyword "user" do Postgres)
CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    timezone    VARCHAR(50)  NOT NULL DEFAULT 'UTC',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Tenant-User: associação entre tenant e usuário com role
CREATE TABLE tenant_user (
    tenant_id   UUID NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role        VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (tenant_id, user_id)
);

-- Project: pertence ao tenant, registra criador
CREATE TABLE project (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    tenant_id   UUID NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    created_by  UUID NOT NULL REFERENCES users(id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Time Entry: lançamento de tempo, pertence ao projeto
CREATE TABLE time_entry (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    description VARCHAR(500),
    start_time  TIMESTAMPTZ NOT NULL,
    end_time    TIMESTAMPTZ,
    project_id  UUID NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    created_by  UUID NOT NULL REFERENCES users(id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Constraint: apenas 1 timer ativo (end_time NULL) por usuário
CREATE UNIQUE INDEX idx_time_entry_active_per_user
    ON time_entry(created_by)
    WHERE end_time IS NULL;
