# Atividade Recente - Nova Implementação

## Visão Geral

Esta implementação substitui o sistema anterior de atividade recente que guardava dados em memória/base de dados por um sistema baseado em ficheiros de log no servidor. Esta abordagem oferece melhor performance e evita o crescimento indefinido da página.

## Funcionalidades Implementadas

### 1. Sistema de Logs de Atividade
- **Localização**: `logs/activity.log` (configurável via `activity.log.path`)
- **Formato**: `[timestamp] type | title | description | username`
- **Limite**: Máximo 1000 entradas (configurável)
- **Exibição**: Últimas 5 entradas na interface

### 2. Tipos de Atividade Registados

#### Utilizadores
- `USER_REGISTER` - Novo utilizador registado
- `USER_LOGIN` - Login de utilizador
- `USER_ACTIVATED` - Utilizador ativado por admin
- `USER_DEACTIVATED` - Utilizador desativado por admin
- `USER_DELETED` - Utilizador eliminado por admin
- `USER_MADE_ADMIN` - Utilizador tornado administrador
- `ADMIN_REMOVED` - Privilégios de admin removidos

#### Reports
- `REPORT_CREATED` - Novo report criado
- `REPORT_APPROVED` - Report aprovado por admin
- `REPORT_REJECTED` - Report rejeitado por admin

#### Sistema
- `SYSTEM_CONFIG_CHANGED` - Configurações do sistema alteradas
- `LLM_TEMPORARILY_DISABLED` - LLM desligado temporariamente
- `LLM_TEMPORARY_DISABLE_REMOVED` - Desligamento temporário removido
- `LLM_AUTO_REACTIVATED` - LLM reativada automaticamente após expiração

### 3. Componentes Principais

#### ActivityLogService
- **Localização**: `src/main/java/com/example/PromptShieldAPI/service/ActivityLogService.java`
- **Responsabilidades**:
  - Gerir o ficheiro de log
  - Adicionar novas entradas
  - Ler últimas entradas
  - Manter limite de entradas
  - Fornecer estatísticas

#### Endpoints da API
- `GET /admin/api/activity` - Obter últimas 5 atividades
- `GET /admin/api/activity/stats` - Obter estatísticas do log

### 4. Integração com Controllers

#### AdminController
- Logs para operações de gestão de utilizadores
- Logs para operações de reports
- Logs para configurações do sistema

#### AuthController
- Logs para login de utilizadores
- Logs para registo de novos utilizadores

#### AccountStatusController
- Logs para criação de reports

## Configuração

### application.properties
```properties
# Configuração do ficheiro de log de atividade
activity.log.path=logs/activity.log
```

### Parâmetros Configuráveis
- `MAX_LOG_ENTRIES`: 1000 (máximo de entradas no ficheiro)
- `DISPLAY_ENTRIES`: 5 (entradas exibidas na interface)

## Vantagens da Nova Implementação

1. **Performance**: Não sobrecarrega a base de dados
2. **Escalabilidade**: Ficheiro de log com limite controlado
3. **Simplicidade**: Interface mostra apenas as últimas 5 atividades
4. **Persistência**: Dados mantidos mesmo após reinicialização
5. **Manutenção**: Limpeza automática de entradas antigas

## Interface do Utilizador

### Dashboard Admin
- Secção "Atividade Recente" mostra as últimas 5 entradas
- Botão "Atualizar" recarrega as atividades do ficheiro
- Ícones específicos para cada tipo de atividade
- Ordenação do mais recente para o mais antigo

### Ícones por Tipo
- 👤 `USER_REGISTER` - Novo utilizador
- 🔐 `USER_LOGIN` - Login
- ✅ `USER_ACTIVATED` - Utilizador ativado
- ❌ `USER_DEACTIVATED` - Utilizador desativado
- 🗑️ `USER_DELETED` - Utilizador eliminado
- 🛡️ `USER_MADE_ADMIN` - Novo admin
- 🚫 `ADMIN_REMOVED` - Admin removido
- 🚩 `REPORT_CREATED` - Novo report
- ✅ `REPORT_APPROVED` - Report aprovado
- ❌ `REPORT_REJECTED` - Report rejeitado
- ⚙️ `SYSTEM_CONFIG_CHANGED` - Configuração alterada
- ⏸️ `LLM_TEMPORARILY_DISABLED` - LLM pausado
- ▶️ `LLM_TEMPORARY_DISABLE_REMOVED` - LLM retomado
- 🔄 `LLM_AUTO_REACTIVATED` - LLM reativada automaticamente

## Manutenção

### Limpeza Automática
- O sistema mantém automaticamente o limite de 1000 entradas
- Entradas antigas são removidas quando o limite é excedido

### Backup
- O ficheiro `logs/activity.log` pode ser copiado para backup
- Formato de texto simples permite fácil análise

### Monitorização
- Endpoint `/admin/api/activity/stats` fornece estatísticas
- Informações sobre total de entradas, tamanho do ficheiro e última modificação

## Exemplo de Entrada no Log
```
[2024-01-15 14:30:25] USER_LOGIN | Login de Utilizador | Utilizador 'jestevao' fez login no sistema | jestevao
[2024-01-15 14:35:10] USER_ACTIVATED | Utilizador Ativado | Utilizador 'novousuario' foi ativado por admin | admin
[2024-01-15 14:40:15] REPORT_CREATED | Novo Report Criado | Utilizador 'user123' criou um report de problema de conta | user123
```

## Considerações de Segurança

1. **Acesso**: Apenas administradores podem aceder aos logs
2. **Localização**: Ficheiro guardado no servidor, não acessível via web
3. **Conteúdo**: Não armazena informações sensíveis (passwords, etc.)
4. **Auditoria**: Mantém histórico de ações administrativas 