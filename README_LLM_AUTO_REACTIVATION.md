# Reativação Automática de LLMs - Implementação

## Problema Resolvido

**Issue:** Quando uma LLM é desativada temporariamente por um administrador, ela não é reativada automaticamente após o tempo definido.

**Comportamento Anterior:**
- O sistema só reativa a LLM após uma tentativa de interação do utilizador (ex: envio de mensagem)
- A lógica de reativação existia, mas não era disparada automaticamente no tempo programado

**Solução Implementada:**
- Implementação de um mecanismo de background que verifica automaticamente quando o tempo programado termina
- Reativação automática da LLM sem intervenção externa
- Processo ocorre em background assim que o tempo de desativação expira

## Componentes Implementados

### 1. LLMAutoReactivationService
**Arquivo:** `src/main/java/com/example/PromptShieldAPI/service/LLMAutoReactivationService.java`

**Funcionalidades:**
- **Scheduled Task:** Executa a cada minuto para verificar LLMs com desligamento temporário expirado
- **Reativação Automática:** Remove automaticamente o desligamento temporário quando o tempo expira
- **Logging:** Registra todas as atividades de reativação automática
- **Histórico:** Mantém histórico de todas as reativações automáticas

**Métodos Principais:**
```java
@Scheduled(fixedRate = 60000) // Executa a cada minuto
public void checkAndReactivateExpiredLLMs()

private void reactivateLLM(SystemConfig config)

public void manualCheckAndReactivate() // Para verificação manual
```

### 2. Atualização do SystemConfigRepository
**Arquivo:** `src/main/java/com/example/PromptShieldAPI/repository/SystemConfigRepository.java`

**Adição:**
```java
List<SystemConfig> findByTemporaryDisabledTrue();
```

### 3. Endpoint para Verificação Manual
**Arquivo:** `src/main/java/com/example/PromptShieldAPI/controller/AdminController.java`

**Endpoint:** `POST /admin/llm/check-auto-reactivation`
- Permite aos administradores executar uma verificação manual de reativação
- Útil para testes e situações onde é necessário forçar a verificação

### 4. Ativação de Scheduled Tasks
**Arquivo:** `src/main/java/com/example/PromptShieldAPI/PromptShieldAPIApplication.java`

**Adição:**
```java
@EnableScheduling
```

## Como Funciona

### Fluxo Automático
1. **Agendamento:** O serviço executa automaticamente a cada minuto
2. **Verificação:** Busca todas as configurações com `temporaryDisabled = true`
3. **Validação:** Verifica se `temporaryDisabledEnd` já expirou
4. **Reativação:** Se expirou, remove o desligamento temporário
5. **Registro:** Cria entrada no histórico e log de atividades

### Fluxo Manual
1. **Trigger:** Administrador chama o endpoint `/admin/llm/check-auto-reactivation`
2. **Execução:** Mesmo processo do fluxo automático
3. **Log:** Registra a atividade como verificação manual

## Configuração

### Intervalo de Verificação
O intervalo padrão é de **1 minuto** (60000ms). Para alterar:

```java
@Scheduled(fixedRate = 60000) // Alterar este valor
```

**Opções de intervalo:**
- 30000 = 30 segundos
- 60000 = 1 minuto (padrão)
- 300000 = 5 minutos
- 600000 = 10 minutos

### Logs
O serviço registra logs detalhados:
- **DEBUG:** Verificações periódicas
- **INFO:** Reativações bem-sucedidas
- **ERROR:** Erros durante o processo

## Testes

### Script de Teste
**Arquivo:** `test_llm_auto_reactivation.js`

**Funcionalidades:**
- Login como administrador
- Desligamento temporário de LLM por 2 minutos
- Aguardar expiração
- Verificar reativação automática
- Confirmar status final

**Execução:**
```bash
node test_llm_auto_reactivation.js
```

### Teste Manual via API
```bash
# Verificar status atual
curl -X GET "http://localhost:8080/admin/llm-status" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Desligar temporariamente
curl -X POST "http://localhost:8080/admin/llm/temporary-disable" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "OPENAI",
    "disableUntil": "2024-01-15T10:30:00",
    "reason": "Teste de reativação automática"
  }'

# Verificar reativação manual
curl -X POST "http://localhost:8080/admin/llm/check-auto-reactivation" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Monitoramento

### Logs de Atividade
O sistema registra automaticamente:
- `LLM_AUTO_REACTIVATED`: LLM reativada automaticamente
- `LLM_AUTO_REACTIVATION_CHECK`: Verificação manual executada

### Histórico de Configurações
Todas as reativações automáticas são registradas na tabela `config_history` com:
- `changedBy`: "sistema (reativação automática)"
- `enabled`: true
- Timestamp da reativação

## Benefícios

1. **Automatização Completa:** Não requer intervenção manual
2. **Confiabilidade:** Execução garantida a cada minuto
3. **Rastreabilidade:** Logs e histórico completos
4. **Flexibilidade:** Endpoint manual para casos especiais
5. **Performance:** Verificação eficiente apenas de LLMs temporariamente desabilitadas

## Considerações de Segurança

- Apenas administradores podem executar verificação manual
- Todas as atividades são registradas no log
- O serviço de background executa com privilégios do sistema
- Tratamento de erros robusto para evitar falhas

## Troubleshooting

### LLM não reativa automaticamente
1. Verificar logs da aplicação
2. Confirmar se `@EnableScheduling` está ativo
3. Verificar se o serviço está sendo injetado corretamente
4. Executar verificação manual via endpoint

### Erro no scheduled task
1. Verificar logs de erro
2. Confirmar conectividade com banco de dados
3. Verificar permissões do usuário do banco
4. Validar integridade dos dados

### Performance
1. Monitorar tempo de execução das verificações
2. Ajustar intervalo se necessário
3. Verificar impacto no banco de dados
4. Considerar otimizações se houver muitas LLMs 