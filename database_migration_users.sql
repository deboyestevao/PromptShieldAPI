-- Migração para adicionar novos campos à tabela users
-- Execute este script na sua base de dados

-- Adicionar novos campos à tabela users
ALTER TABLE users
ADD COLUMN first_name VARCHAR(255) NULL,
ADD COLUMN last_name VARCHAR(255) NULL,
ADD COLUMN active BOOLEAN DEFAULT TRUE,
ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN last_login_at TIMESTAMP NULL;

-- Atualizar registos existentes com valores padrão
UPDATE users 
SET 
    first_name = COALESCE(first_name, 'Utilizador'),
    last_name = COALESCE(last_name, ''),
    active = COALESCE(active, TRUE),
    created_at = COALESCE(created_at, CURRENT_TIMESTAMP)
WHERE first_name IS NULL OR last_name IS NULL OR active IS NULL OR created_at IS NULL;

-- Criar índices para melhor performance
CREATE INDEX idx_users_active ON users(active);
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_users_last_login_at ON users(last_login_at);

-- Comentários para documentação
COMMENT ON COLUMN users.first_name IS 'Primeiro nome do utilizador';
COMMENT ON COLUMN users.last_name IS 'Último nome do utilizador';
COMMENT ON COLUMN users.active IS 'Indica se o utilizador está ativo';
COMMENT ON COLUMN users.created_at IS 'Data de criação da conta';
COMMENT ON COLUMN users.last_login_at IS 'Data do último login'; 