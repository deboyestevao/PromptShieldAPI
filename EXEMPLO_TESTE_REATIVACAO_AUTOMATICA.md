# Exemplo de Teste - Reativação Automática de LLMs

## 🎯 Objetivo
Testar a funcionalidade de reativação automática de LLMs quando o desligamento temporário expira.

## 📋 Cenário de Teste

### 1. Configurar Desligamento Temporário
1. Aceder ao dashboard admin
2. Ir para "Configurações" > "Desligamento Temporário"
3. Configurar um desligamento temporário para um LLM com duração curta (ex: 1 minuto)
4. Verificar que o LLM fica desligado

### 2. Aguardar Expiração
1. Aguardar que o tempo de desligamento expire
2. Durante este tempo, tentar usar o chat (deve mostrar mensagem de manutenção)

### 3. Testar Reativação Automática
1. Após a expiração, tentar usar o chat novamente
2. O LLM deve estar automaticamente reativado
3. Verificar a secção "Atividade Recente" no dashboard

## 🔍 Verificações

### No Dashboard Admin
- **Atividade Recente**: Deve mostrar uma entrada com tipo `LLM_AUTO_REACTIVATED`
- **Ícone**: 🔄 (fas fa-sync-alt)
- **Descrição**: "Modelo [OpenAI/Ollama] foi automaticamente reativado após expiração do desligamento temporário"
- **Utilizador**: "sistema"

### No Ficheiro de Log
```bash
# Verificar o ficheiro logs/activity.log
tail -f logs/activity.log
```

Exemplo de entrada esperada:
```
[2024-01-15 15:30:25] LLM_AUTO_REACTIVATED | LLM Reativada Automaticamente | Modelo OpenAI foi automaticamente reativado após expiração do desligamento temporário | sistema
```

### Via API
```bash
# Verificar atividades recentes
curl -X GET http://localhost:8080/admin/api/activity

# Verificar estatísticas
curl -X GET http://localhost:8080/admin/api/activity/stats
```

## 🧪 Script de Teste Automatizado

Executar o script `test_llm_auto_reactivation.js`:

```bash
node test_llm_auto_reactivation.js
```

Este script irá:
1. Verificar o status atual dos LLMs
2. Procurar por atividades de reativação automática
3. Mostrar estatísticas do log
4. Listar todas as atividades relacionadas a LLMs

## 📊 Resultados Esperados

### ✅ Sucesso
- Atividade `LLM_AUTO_REACTIVATED` aparece no log
- LLM volta a funcionar automaticamente após expiração
- Interface mostra a atividade com ícone correto
- Histórico de configuração é atualizado

### ❌ Problemas Comuns
- **LLM não reativa**: Verificar se o tempo de expiração foi definido corretamente
- **Atividade não aparece**: Verificar se o ActivityLogService está configurado
- **Erro no log**: Verificar permissões de escrita no diretório `logs/`

## 🔧 Configuração para Teste Rápido

Para testar rapidamente, pode configurar um desligamento temporário com duração de apenas 1 minuto:

1. **Desligar LLM**: Configurar desligamento até "agora + 1 minuto"
2. **Aguardar**: 1 minuto
3. **Testar**: Tentar usar o chat
4. **Verificar**: Dashboard admin > Atividade Recente

## 📝 Notas Importantes

- A reativação automática só acontece quando alguém tenta usar o LLM
- O sistema verifica a expiração no método `isModelActuallyEnabled()`
- A atividade é registada com utilizador "sistema"
- O ícone usado é `fas fa-sync-alt` (🔄) 