-- Script de migração para remover o campo isOnline da tabela users
-- Execute este script na sua base de dados

-- Remover índices relacionados ao campo isOnline
DROP INDEX IF EXISTS idx_users_is_online;

-- Remover o campo isOnline da tabela users
ALTER TABLE users DROP COLUMN IF EXISTS is_online;

-- Comentários para documentação
-- O campo is_online foi removido pois a funcionalidade de tracking online foi descontinuada
-- O campo last_active foi mantido para outras funcionalidades que possam precisar 