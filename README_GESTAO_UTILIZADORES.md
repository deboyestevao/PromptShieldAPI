# Gestão de Utilizadores - PromptShield

## 📋 Descrição

A página de Gestão de Utilizadores permite aos administradores gerir todos os utilizadores da plataforma de forma eficiente e intuitiva.

## 🚀 Funcionalidades

### 📊 Dashboard de Estatísticas
- **Total de Utilizadores**: Número total de contas registadas
- **Utilizadores Ativos**: Contas atualmente ativas
- **Novos Utilizadores**: Registos dos últimos 30 dias

### 🔍 Pesquisa e Filtros
- **Pesquisa em tempo real** por nome, email ou username
- **Filtro por estado**: Ativos, Inativos ou Todos
- **Ordenação**: Por data de registo, nome (A-Z/Z-A)

### 👥 Gestão de Utilizadores
- **Visualizar detalhes** de cada utilizador
- **Ativar/Desativar** contas
- **Eliminar** utilizadores (com confirmação)
- **Contagem de chats** por utilizador

### 📱 Interface Responsiva
- Design moderno e elegante
- Funciona em desktop e mobile
- Animações suaves
- Notificações em tempo real

## 🛠️ Instalação

### 1. Executar Migração da Base de Dados

Execute o script SQL para adicionar os novos campos:

```sql
-- Execute o conteúdo do arquivo database_migration_users.sql
```

### 2. Reiniciar a Aplicação

```bash
mvn spring-boot:run
```

## 📖 Como Usar

### Aceder à Página
1. Faça login como administrador
2. Vá para o Dashboard Admin
3. Clique em "Gerir Utilizadores"

### Pesquisar Utilizadores
- Use a barra de pesquisa para encontrar utilizadores por nome, email ou username
- Os resultados são filtrados em tempo real

### Filtrar e Ordenar
- **Estado**: Selecione "Ativos", "Inativos" ou "Todos"
- **Ordenação**: Escolha como ordenar os resultados

### Gerir Utilizadores

#### Ver Detalhes
- Clique no ícone de "olho" 👁️
- Visualize informações completas do utilizador
- Veja estatísticas de atividade

#### Ativar/Desativar
- Clique no ícone de "ativar" ✅ ou "desativar" ⚠️
- Confirme a ação no modal
- O estado é atualizado imediatamente

#### Eliminar Utilizador
- Clique no ícone de "lixeira" 🗑️
- Confirme a eliminação no modal
- **⚠️ Atenção**: Esta ação não pode ser desfeita

## 🔧 Endpoints da API

### Listar Utilizadores
```
GET /admin/api/users
```

### Obter Detalhes de um Utilizador
```
GET /admin/api/users/{id}
```

### Ativar Utilizador
```
POST /admin/api/users/{id}/activate
```

### Desativar Utilizador
```
POST /admin/api/users/{id}/deactivate
```

### Eliminar Utilizador
```
DELETE /admin/api/users/{id}
```

## 🎨 Design

### Características Visuais
- **Gradientes suaves** em azul
- **Glassmorphism** para elementos modernos
- **Animações fluidas** de entrada e hover
- **Ícones SVG** para melhor qualidade

### Cores Principais
- **Azul primário**: `#3b82f6`
- **Azul escuro**: `#1e40af`
- **Verde sucesso**: `#22c55e`
- **Vermelho erro**: `#ef4444`
- **Cinza neutro**: `#6b7280`

## 🔒 Segurança

### Permissões
- Apenas utilizadores com role `ADMIN` podem aceder
- Proteção contra eliminação da própria conta
- Validação de dados no backend

### Validações
- Verificação de existência do utilizador
- Confirmação para ações destrutivas
- Tratamento de erros robusto

## 📱 Responsividade

### Breakpoints
- **Desktop**: > 768px
- **Tablet**: 768px - 1024px
- **Mobile**: < 768px

### Adaptações Mobile
- Tabela com scroll horizontal
- Botões de ação empilhados
- Modais otimizados para touch
- Pesquisa e filtros adaptados

## 🐛 Troubleshooting

### Problemas Comuns

#### Utilizadores não aparecem
- Verifique se executou a migração da base de dados
- Confirme que tem permissões de admin
- Verifique os logs da aplicação

#### Erro ao ativar/desativar
- Verifique se o utilizador existe
- Confirme que não está a tentar modificar a própria conta
- Verifique a conectividade com a base de dados

#### Página não carrega
- Verifique se o arquivo `adminUsers.html` existe
- Confirme que o CSS está a ser carregado
- Verifique a consola do browser para erros JavaScript

## 🔄 Atualizações Futuras

### Funcionalidades Planeadas
- [ ] Exportação de dados (CSV/Excel)
- [ ] Filtros avançados por data
- [ ] Bulk actions (ações em lote)
- [ ] Histórico de ações do admin
- [ ] Notificações por email
- [ ] Relatórios de atividade

### Melhorias Técnicas
- [ ] Paginação no servidor
- [ ] Cache de dados
- [ ] WebSocket para atualizações em tempo real
- [ ] Logs de auditoria detalhados

## 📞 Suporte

Para questões ou problemas:
1. Verifique este README
2. Consulte os logs da aplicação
3. Teste em diferentes browsers
4. Contacte a equipa de desenvolvimento

---

**Desenvolvido com ❤️ para o PromptShield** 