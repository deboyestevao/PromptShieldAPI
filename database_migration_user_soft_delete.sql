-- Script de migração para adicionar soft delete à tabela users
-- Execute este script na sua base de dados

-- Adicionar campos para soft delete
ALTER TABLE users
ADD COLUMN deleted_at TIMESTAMP NULL,
ADD COLUMN deleted_by VARCHAR(255) NULL;

-- Criar índices para melhor performance
CREATE INDEX idx_users_deleted_at ON users(deleted_at);
CREATE INDEX idx_users_deleted_by ON users(deleted_by);

-- Comentários para documentação
COMMENT ON COLUMN users.deleted_at IS 'Data/hora quando o utilizador foi movido para a lixeira (soft delete)';
COMMENT ON COLUMN users.deleted_by IS 'Username do admin que moveu o utilizador para a lixeira';

-- Nota: Os utilizadores existentes terão deleted_at = NULL (não deletados) 