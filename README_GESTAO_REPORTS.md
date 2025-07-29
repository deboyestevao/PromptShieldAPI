# Gestão de Reports - PromptShield

## Como o Admin Acede aos Reports

### 1. **Acesso ao Dashboard Admin**
- Faça login como administrador
- Aceda ao dashboard em: `/admin/dashboard`
- Clique no card **"Gestão de Reports"**

### 2. **Página de Gestão de Reports**
- URL: `/admin/reports`
- Interface moderna com estatísticas e filtros
- Lista todos os reports de contas desativadas

## Funcionalidades Disponíveis

### 📊 **Estatísticas**
- **Total de Reports**: Número total de solicitações
- **Reports Pendentes**: Aguardando análise do admin
- **Reports Resolvidos**: Aprovados ou rejeitados

### 🔍 **Filtros e Ordenação**
- **Filtro por Estado**: Pendentes, Aprovados, Rejeitados
- **Ordenação**: Mais recentes ou mais antigos

### ✅ **Ações Disponíveis**
Para reports **pendentes**, o admin pode:

#### **Aprovar Report**
- Clica em **"Aprovar"**
- O utilizador é automaticamente **ativado**
- O report fica marcado como **"Aprovado"**
- Regista quem aprovou e quando

#### **Rejeitar Report**
- Clica em **"Rejeitar"**
- O report fica marcado como **"Rejeitado"**
- O utilizador permanece **desativado**
- Regista quem rejeitou e quando

## Fluxo Completo do Sistema

### 1. **Utilizador Desativado**
- Admin desativa conta em `/admin/users`
- Utilizador tenta fazer login

### 2. **Página de Conta Desativada**
- Utilizador é redirecionado para `/account-disabled`
- Interface moderna sem emojis (apenas ícones SVG)
- Opções: "Solicitar Reativação" ou "Terminar Sessão"

### 3. **Solicitação de Reativação**
- Utilizador preenche formulário com motivo
- Report é criado na base de dados
- Status: **PENDING**

### 4. **Análise pelo Admin**
- Admin vê report em `/admin/reports`
- Analisa o motivo da solicitação
- Decide aprovar ou rejeitar

### 5. **Resolução**
- **Se aprovado**: Utilizador é ativado automaticamente
- **Se rejeitado**: Utilizador permanece desativado

## Estrutura da Base de Dados

### Tabela `account_reports`
```sql
- id: Identificador único
- user_id: ID do utilizador que fez o report
- reason: Motivo da solicitação (texto)
- status: PENDING, APPROVED, ou REJECTED
- created_at: Data de criação
- resolved_at: Data de resolução
- resolved_by: ID do admin que resolveu
```

## Migração da Base de Dados

Execute o script `database_migration_reports.sql` na sua base de dados para criar a tabela necessária.

## Benefícios do Sistema

### ✅ **Para Utilizadores**
- Interface clara e moderna
- Processo transparente de solicitação
- Feedback imediato após envio

### ✅ **Para Administradores**
- Dashboard centralizado para gestão
- Histórico completo de reports
- Ações rápidas (aprovar/rejeitar)
- Estatísticas em tempo real

### ✅ **Para o Sistema**
- Rastreabilidade completa
- Auditoria de decisões
- Performance otimizada com índices
- Interface responsiva e moderna 