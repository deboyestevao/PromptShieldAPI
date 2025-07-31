-- Migration para adicionar campo originalEnabledState à tabela system_config
-- Este campo guarda o estado original da LLM antes do desligamento temporário

-- Adicionar a nova coluna
ALTER TABLE system_config ADD COLUMN original_enabled_state BOOLEAN NULL;

-- Comentário explicativo
COMMENT ON COLUMN system_config.original_enabled_state IS 'Estado original da LLM antes do desligamento temporário. NULL quando não há desligamento temporário ativo.';

-- Verificar se a coluna foi adicionada corretamente
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'system_config' AND column_name = 'original_enabled_state'; 