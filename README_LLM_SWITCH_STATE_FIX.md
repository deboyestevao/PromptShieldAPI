# Correção do Estado do Switch de LLMs - Implementação

## Problema Resolvido

**Issue:** Quando o tempo de desativação temporária termina, o switch de ativação da LLM pode voltar num estado incorreto.

**Comportamento Anterior:**
- O switch voltava ativado ou desativado de forma inconsistente
- Ignorava o estado anterior ao desligamento temporário
- Não havia sincronização entre backend e UI

**Solução Implementada:**
- Guardar o estado original da LLM antes de aplicar a desativação temporária
- Restaurar esse estado corretamente após o tempo programado
- Garantir sincronização entre backend e estado visual apresentado ao utilizador

## Componentes Modificados

### 1. Modelo SystemConfig
**Arquivo:** `src/main/java/com/example/PromptShieldAPI/model/SystemConfig.java`

**Adição:**
```java
// Campo para guardar o estado original antes do desligamento temporário
private Boolean originalEnabledState;
```

### 2. SystemConfigService
**Arquivo:** `src/main/java/com/example/PromptShieldAPI/service/SystemConfigService.java`

**Modificações:**
- **`temporarilyDisableModel()`:** Guarda o estado original antes do desligamento
- **`removeTemporaryDisable()`:** Restaura o estado original ao remover desligamento
- **`updateModelStatusManually()`:** Limpa estado original em alterações manuais

### 3. LLMAutoReactivationService
**Arquivo:** `src/main/java/com/example/PromptShieldAPI/service/LLMAutoReactivationService.java`

**Modificação:**
- **`reactivateLLM()`:** Restaura o estado original em vez de sempre ativar

### 4. AdminController
**Arquivo:** `src/main/java/com/example/PromptShieldAPI/controller/AdminController.java`

**Modificação:**
- **`getLLMStatus()`:** Inclui informações sobre o estado que será restaurado

### 5. Template HTML
**Arquivo:** `src/main/resources/templates/adminSystemPreferences.html`

**Modificação:**
- **`updateConfigOption()`:** Mostra informação sobre o estado que será restaurado

### 6. Estilos CSS
**Arquivo:** `src/main/resources/static/css/adminSystemPreferences.css`

**Adição:**
- Estilos para `.restore-info` que mostra o estado de restauração

## Como Funciona

### Fluxo de Desligamento Temporário
1. **Estado Original:** Sistema guarda o estado atual (`enabled`) em `originalEnabledState`
2. **Desligamento:** Define `enabled = false` e `temporaryDisabled = true`
3. **UI:** Switch fica desabilitado e mostra estado de manutenção

### Fluxo de Reativação Automática
1. **Verificação:** Serviço de background verifica se o tempo expirou
2. **Restauração:** Restaura o valor de `originalEnabledState` para `enabled`
3. **Limpeza:** Remove todos os campos de desligamento temporário
4. **UI:** Switch reflete o estado original correto

### Fluxo de Remoção Manual
1. **Trigger:** Administrador remove desligamento temporário manualmente
2. **Restauração:** Sistema restaura o estado original
3. **UI:** Switch volta ao estado correto

## Base de Dados

### Migration SQL
**Arquivo:** `database_migration_original_state.sql`

```sql
ALTER TABLE system_config ADD COLUMN original_enabled_state BOOLEAN NULL;
```

### Estrutura da Tabela
```sql
system_config:
- id (PK)
- model (ENUM: OLLAMA, OPENAI)
- enabled (BOOLEAN)
- temporary_disabled (BOOLEAN)
- temporary_disabled_start (TIMESTAMP)
- temporary_disabled_end (TIMESTAMP)
- temporary_disabled_reason (VARCHAR)
- original_enabled_state (BOOLEAN) -- NOVO CAMPO
- version (BIGINT)
```

## Interface do Utilizador

### Informações Adicionais
Quando uma LLM está em desligamento temporário, a interface mostra:
- Estado atual: "Em Manutenção"
- Tempo de expiração
- Motivo do desligamento
- **Estado que será restaurado:** "Será restaurado para: Ativo/Inativo"

### Estilo Visual
- Informação de restauração com ícone de "undo"
- Cor azul para destacar a informação
- Borda lateral para melhor visualização

## Casos de Uso

### Cenário 1: LLM Ativa → Desligamento Temporário → Reativação
1. **Estado Inicial:** LLM ativa (switch ON)
2. **Desligamento:** Administrador desliga temporariamente
3. **Durante:** Switch desabilitado, mostra "Em Manutenção"
4. **Após Expiração:** Switch volta para ON (estado original)

### Cenário 2: LLM Inativa → Desligamento Temporário → Reativação
1. **Estado Inicial:** LLM inativa (switch OFF)
2. **Desligamento:** Administrador desliga temporariamente
3. **Durante:** Switch desabilitado, mostra "Em Manutenção"
4. **Após Expiração:** Switch volta para OFF (estado original)

### Cenário 3: Remoção Manual
1. **Estado:** LLM em desligamento temporário
2. **Ação:** Administrador remove desligamento manualmente
3. **Resultado:** Switch volta ao estado original

## Logs e Histórico

### Entradas no Histórico
- **Desligamento:** "admin (desligamento temporário até 2024-01-15T10:30:00)"
- **Reativação Automática:** "sistema (reativação automática - estado original restaurado: ativo/inativo)"
- **Remoção Manual:** "admin (removido desligamento temporário - estado original restaurado: ativo/inativo)"

### Logs de Atividade
- **LLM_AUTO_REACTIVATED:** Inclui informação sobre o estado restaurado
- **LLM_TEMPORARILY_DISABLED:** Mantém informação sobre o desligamento
- **LLM_TEMPORARY_DISABLE_REMOVED:** Inclui informação sobre restauração

## Benefícios

1. **Consistência:** Switch sempre reflete o estado real da LLM
2. **Transparência:** Utilizador sabe exatamente o que vai acontecer
3. **Confiabilidade:** Estado original é preservado e restaurado corretamente
4. **Rastreabilidade:** Histórico completo de todas as alterações
5. **UX Melhorada:** Interface clara sobre o estado futuro

## Testes Recomendados

### Teste 1: LLM Ativa
1. Ativar LLM
2. Aplicar desligamento temporário
3. Aguardar expiração
4. Verificar se switch volta para ON

### Teste 2: LLM Inativa
1. Desativar LLM
2. Aplicar desligamento temporário
3. Aguardar expiração
4. Verificar se switch volta para OFF

### Teste 3: Remoção Manual
1. Aplicar desligamento temporário
2. Remover manualmente
3. Verificar se switch volta ao estado original

## Considerações de Segurança

- Estado original é limpo após restauração
- Alterações manuais limpam o estado original
- Todas as operações são registradas no histórico
- Validação de dados em todas as operações

## Troubleshooting

### Switch não volta ao estado correto
1. Verificar se `original_enabled_state` foi definido
2. Confirmar se a reativação automática executou
3. Verificar logs de erro
4. Executar verificação manual via endpoint

### Estado inconsistente
1. Verificar dados na tabela `system_config`
2. Confirmar se migration foi executada
3. Verificar se não há dados corrompidos
4. Limpar estado manualmente se necessário 