# Funcionalidades Futuras Planeadas

## 🗂️ Sistema de Lixeira para Chats

### Descrição
Implementação de um sistema completo de lixeira para permitir aos utilizadores restaurar chats que foram deletados por engano.

### Estado Atual
- ✅ **Backend implementado**: Endpoints e lógica de soft delete/restore
- ❌ **Frontend pendente**: Interface de lixeira não implementada

### Endpoints Disponíveis
- `GET /api/chat/deleted` - Listar chats deletados
- `POST /api/chat/{id}/restore` - Restaurar chat da lixeira
- `DELETE /api/chat/{id}` - Mover chat para lixeira (soft delete)

### Funcionalidades a Implementar

#### 1. Página de Lixeira
- **Localização**: `/chat/trash` ou botão na interface principal
- **Conteúdo**: Lista de todos os chats deletados do utilizador
- **Informações**: Nome do chat, data de deleção, número de perguntas

#### 2. Botões de Ação
- **Restaurar**: Botão para restaurar chat selecionado
- **Deletar Permanentemente**: Opção para remoção definitiva
- **Seleção Múltipla**: Checkbox para restaurar/deletar vários chats

#### 3. Interface de Utilizador
- **Filtros**: Por data de deleção, nome do chat
- **Pesquisa**: Buscar chats deletados por nome
- **Ordenação**: Por data de deleção, nome, etc.

### Benefícios
- **Recuperação de dados**: Evita perda permanente de conversas importantes
- **Experiência do utilizador**: Interface familiar (como Gmail, Windows)
- **Auditoria**: Mantém histórico de ações de deleção/restauração

### Prioridade
- **Baixa**: Funcionalidade útil mas não crítica
- **Implementação**: Pode ser feita quando houver tempo disponível

---

## 📊 Outras Funcionalidades Futuras

### Dashboard de Utilizador
- Estatísticas de uso (chats criados, perguntas feitas)
- Gráficos de atividade ao longo do tempo
- Relatórios de utilização dos LLMs

### Gestão Avançada de Ficheiros
- Organização em pastas
- Tags e categorização
- Compartilhamento entre utilizadores

### Integração com Sistemas Externos
- Exportação de conversas (PDF, Word)
- API para integração com outras aplicações
- Webhooks para notificações

### Melhorias de IA
- Sugestões de perguntas baseadas no contexto
- Análise de sentimento das conversas
- Resumos automáticos de conversas longas

---

## 📝 Notas de Implementação

### Código Preparado
O backend já está preparado para estas funcionalidades:
- Soft delete implementado em `Chat` e `Question`
- Endpoints de restore já funcionais
- Sistema de auditoria com timestamps

### Próximos Passos
1. **Definir prioridades** das funcionalidades
2. **Criar mockups** da interface de lixeira
3. **Implementar frontend** para funcionalidades selecionadas
4. **Testar integração** entre frontend e backend

### Considerações Técnicas
- Manter consistência com design atual
- Implementar responsividade para mobile
- Considerar performance para grandes volumes de dados
- Manter segurança e permissões adequadas 