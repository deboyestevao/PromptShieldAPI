-- Script de migração para adicionar soft delete às tabelas existentes
-- Execute este script na sua base de dados

-- Adicionar colunas de soft delete à tabela chat
ALTER TABLE chat 
ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN deleted_at TIMESTAMP NULL,
ADD COLUMN deleted_by VARCHAR(255) NULL;

-- Adicionar colunas de soft delete à tabela question
ALTER TABLE question 
ADD COLUMN deleted_at TIMESTAMP NULL,
ADD COLUMN deleted_by VARCHAR(255) NULL;

-- Criar índices para melhor performance
CREATE INDEX idx_chat_deleted_at ON chat(deleted_at);
CREATE INDEX idx_chat_user_active ON chat(user_id, deleted_at);
CREATE INDEX idx_question_deleted_at ON question(deleted_at);
CREATE INDEX idx_question_chat_active ON question(chat_id, deleted_at);

-- Comentários sobre as alterações
-- As colunas deleted_at e deleted_by permitem soft delete
-- Quando deleted_at é NULL, o registro está ativo
-- Quando deleted_at tem um valor, o registro foi "deletado" (soft delete)
-- deleted_by armazena quem deletou o registro 