-- Migração para adicionar campos de tracking online à tabela users
-- Execute este script na sua base de dados

-- Adicionar campos para tracking online
ALTER TABLE users
ADD COLUMN is_online BOOLEAN DEFAULT FALSE,
ADD COLUMN last_active TIMESTAMP NULL;

-- Atualizar registos existentes
UPDATE users 
SET 
    is_online = FALSE,
    last_active = COALESCE(last_login_at, created_at)
WHERE is_online IS NULL OR last_active IS NULL;

-- Criar índices para melhor performance
CREATE INDEX idx_users_is_online ON users(is_online);
CREATE INDEX idx_users_last_active ON users(last_active);

-- Comentários para documentação
COMMENT ON COLUMN users.is_online IS 'Indica se o utilizador está online neste momento';
COMMENT ON COLUMN users.last_active IS 'Data/hora da última atividade do utilizador'; 