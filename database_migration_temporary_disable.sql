-- Migração para adicionar campos de desligamento temporário à tabela SystemConfig
-- Data: 2024-01-XX
-- Descrição: Adiciona campos para controlar desligamento temporário das LLMs
-- Compatível com PostgreSQL

-- Adicionar novos campos à tabela system_config
ALTER TABLE system_config 
ADD COLUMN temporary_disabled BOOLEAN DEFAULT FALSE,
ADD COLUMN temporary_disabled_start TIMESTAMP NULL,
ADD COLUMN temporary_disabled_end TIMESTAMP NULL,
ADD COLUMN temporary_disabled_reason VARCHAR(500) NULL;

-- Adicionar índices para melhor performance
CREATE INDEX idx_system_config_temporary_disabled ON system_config(temporary_disabled);
CREATE INDEX idx_system_config_temporary_disabled_end ON system_config(temporary_disabled_end);

-- Comentários para documentação (PostgreSQL)
COMMENT ON COLUMN system_config.temporary_disabled IS 'Indica se o modelo está desligado temporariamente';
COMMENT ON COLUMN system_config.temporary_disabled_start IS 'Data/hora de início do desligamento temporário';
COMMENT ON COLUMN system_config.temporary_disabled_end IS 'Data/hora de fim do desligamento temporário';
COMMENT ON COLUMN system_config.temporary_disabled_reason IS 'Motivo do desligamento temporário';

-- Verificar se a migração foi aplicada corretamente
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default
FROM information_schema.columns 
WHERE table_name = 'system_config' 
AND column_name IN ('temporary_disabled', 'temporary_disabled_start', 'temporary_disabled_end', 'temporary_disabled_reason')
ORDER BY column_name; 