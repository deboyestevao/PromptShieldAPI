-- Migração para adicionar preferências padrão aos utilizadores existentes
-- Execute este script na sua base de dados

-- Criar tabela user_preferences se não existir
CREATE TABLE IF NOT EXISTS user_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    openai_preferred BOOLEAN DEFAULT FALSE,
    ollama_preferred BOOLEAN DEFAULT FALSE,
    user_id BIGINT NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Inserir preferências padrão para utilizadores que não as têm
INSERT INTO user_preferences (openai_preferred, ollama_preferred, user_id)
SELECT FALSE, FALSE, u.id
FROM users u
LEFT JOIN user_preferences up ON u.id = up.user_id
WHERE up.id IS NULL;

-- Criar índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_user_preferences_user_id ON user_preferences(user_id);
CREATE INDEX IF NOT EXISTS idx_user_preferences_openai ON user_preferences(openai_preferred);
CREATE INDEX IF NOT EXISTS idx_user_preferences_ollama ON user_preferences(ollama_preferred);

-- Comentários para documentação
COMMENT ON TABLE user_preferences IS 'Preferências de modelos de IA dos utilizadores';
COMMENT ON COLUMN user_preferences.openai_preferred IS 'Indica se o utilizador prefere usar OpenAI';
COMMENT ON COLUMN user_preferences.ollama_preferred IS 'Indica se o utilizador prefere usar Ollama';
COMMENT ON COLUMN user_preferences.user_id IS 'ID do utilizador (chave estrangeira)'; 