# Funcionalidade de Desligamento Temporário de LLMs

## Visão Geral

Esta funcionalidade permite aos administradores desligar temporariamente modelos LLM (OpenAI e Ollama) por um período específico, evitando reativações automáticas durante manutenções, testes ou outras situações administrativas.

## Características Principais

### 1. Desligamento Temporário
- **Controle Administrativo**: Apenas utilizadores com role de ADMIN podem utilizar esta funcionalidade
- **Tempo Definido**: O admin pode especificar uma data/hora de fim para o desligamento
- **Motivo Obrigatório**: É necessário fornecer um motivo para o desligamento temporário
- **Prevenção de Reativação**: Durante o período de desligamento, o sistema não reativa automaticamente o modelo

### 2. Reativação Automática
- **Expiração**: Quando o tempo definido expira, o modelo é automaticamente reativado
- **Histórico**: Todas as alterações são registadas no histórico de configurações
- **Notificações**: O sistema cria notificações para informar sobre as alterações

### 3. Reativação Manual
- **Remoção Antecipada**: O admin pode remover o desligamento temporário antes da expiração
- **Controle Total**: Mantém o controlo administrativo sobre o estado dos modelos

## Interface de Utilizador

### Painel de Estado
- Mostra o status atual de cada modelo (Online/Offline)
- Exibe informações sobre desligamentos temporários ativos
- Indica data/hora de fim e motivo do desligamento

### Seção de Desligamento Temporário
- Botões para desligar temporariamente cada modelo
- Formulário para definir data/hora de fim e motivo
- Opção para remover desligamento temporário ativo

## Endpoints da API

### 1. Desligar Temporariamente
```
POST /admin/llm/temporary-disable
Content-Type: application/json

{
    "model": "OPENAI" | "OLLAMA",
    "disableUntil": "2024-01-15T18:00:00",
    "reason": "Manutenção programada"
}
```

### 2. Remover Desligamento Temporário
```
POST /admin/llm/remove-temporary-disable
Content-Type: application/json

{
    "model": "OPENAI" | "OLLAMA"
}
```

### 3. Status Detalhado
```
GET /admin/llm-status

Response:
{
    "openai": true|false,
    "ollama": true|false,
    "openaiDetails": {
        "enabled": true|false,
        "temporaryDisabled": true|false,
        "temporaryDisabledUntil": "2024-01-15T18:00:00",
        "temporaryDisabledReason": "Manutenção programada"
    },
    "ollamaDetails": {
        // mesma estrutura
    }
}
```

## Estrutura da Base de Dados

### Tabela: system_config
Novos campos adicionados:

```sql
ALTER TABLE system_config 
ADD COLUMN temporary_disabled BOOLEAN DEFAULT FALSE,
ADD COLUMN temporary_disabled_start DATETIME NULL,
ADD COLUMN temporary_disabled_end DATETIME NULL,
ADD COLUMN temporary_disabled_reason VARCHAR(500) NULL;
```

### Índices
```sql
CREATE INDEX idx_system_config_temporary_disabled ON system_config(temporary_disabled);
CREATE INDEX idx_system_config_temporary_disabled_end ON system_config(temporary_disabled_end);
```

## Fluxo de Funcionamento

### 1. Desligamento Temporário
1. Admin acede à página de configurações do sistema
2. Clica em "Desligar Temporariamente" para o modelo desejado
3. Preenche data/hora de fim e motivo
4. Sistema desliga o modelo e registra no histórico
5. Modelo permanece desligado até à expiração ou remoção manual

### 2. Verificação Automática
1. Sistema verifica periodicamente o status dos modelos
2. Se um modelo está em desligamento temporário, não o reativa automaticamente
3. Quando o tempo expira, reativa automaticamente o modelo
4. Registra a reativação no histórico

### 3. Reativação Manual
1. Admin pode remover o desligamento temporário a qualquer momento
2. Modelo volta ao estado anterior (ligado/desligado)
3. Sistema registra a remoção no histórico

## Segurança

- **Autorização**: Apenas utilizadores com role ADMIN podem utilizar esta funcionalidade
- **Validação**: Todos os campos são validados no backend
- **Auditoria**: Todas as alterações são registadas no histórico
- **Logs**: Sistema mantém logs de todas as operações

## Casos de Utilização

### 1. Manutenção Programada
- Desligar modelo durante atualizações de sistema
- Evitar interrupções durante manutenção
- Controlar quando o modelo volta a estar disponível

### 2. Testes de Sistema
- Desligar modelo para testar comportamento sem ele
- Verificar fallbacks e alternativas
- Testar performance com um modelo apenas

### 3. Gestão de Custos
- Desligar modelo durante períodos de baixa utilização
- Controlar gastos com APIs externas
- Otimizar utilização de recursos

### 4. Resolução de Problemas
- Isolar problemas específicos de um modelo
- Investigar questões de conectividade
- Testar configurações alternativas

## Monitorização

### Histórico de Alterações
- Todas as alterações são registadas na tabela `config_history`
- Inclui informações sobre desligamentos temporários
- Permite auditoria completa das alterações

### Notificações
- Sistema cria notificações para alterações importantes
- Admin é informado sobre expirações de desligamentos
- Alertas para problemas de conectividade

## Limitações e Considerações

### 1. Performance
- Verificações automáticas podem impactar performance
- Índices na base de dados otimizam consultas
- Cache pode ser implementado para melhorar performance

### 2. Disponibilidade
- Desligamento temporário pode afetar utilizadores
- Sistema deve ter fallbacks adequados
- Comunicação clara sobre indisponibilidade

### 3. Segurança
- Validação rigorosa de datas e motivos
- Prevenção de desligamentos muito longos
- Logs de auditoria para todas as operações

## Futuras Melhorias

### 1. Agendamento
- Permitir agendar desligamentos futuros
- Interface de calendário para visualização
- Notificações antecipadas

### 2. Templates
- Templates de motivos comuns
- Configurações predefinidas
- Reutilização de configurações

### 3. Relatórios
- Relatórios de utilização durante desligamentos
- Análise de impacto nas métricas
- Dashboard de estatísticas

### 4. Integração
- Integração com sistemas de monitorização
- Alertas automáticos baseados em métricas
- APIs para integração externa 