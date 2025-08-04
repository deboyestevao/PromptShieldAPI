# Implementação de Soft Delete para Utilizadores

## ✅ **Alterações Realizadas**

### 1. Modelo de Dados
**Arquivo**: `src/main/java/com/example/PromptShieldAPI/model/User.java`
- **Adicionado**: Campos `deletedAt` e `deletedBy` para soft delete
- **Adicionado**: Métodos `softDelete()`, `isDeleted()` e `restore()`

### 2. Repository
**Arquivo**: `src/main/java/com/example/PromptShieldAPI/repository/UserRepository.java`
- **Adicionado**: `findAllActive()` - Lista apenas utilizadores não deletados
- **Adicionado**: `findAllDeleted()` - Lista utilizadores deletados
- **Adicionado**: `countActiveUsers()` e `countDeletedUsers()` - Contadores

### 3. Controller
**Arquivo**: `src/main/java/com/example/PromptShieldAPI/controller/AdminController.java`
- **Modificado**: `deleteUser()` - Agora usa soft delete em vez de hard delete
- **Modificado**: `getAllUsers()` - Filtra apenas utilizadores ativos
- **Atualizado**: Mensagens de notificação para refletir "lixeira"

### 4. Frontend
**Arquivo**: `src/main/resources/templates/adminUsers.html`
- **Modificado**: Texto do botão para "Mover para Lixeira"
- **Atualizado**: Mensagens de confirmação e notificação
- **Melhorado**: UX para refletir que é soft delete

### 5. Base de Dados
**Arquivo**: `database_migration_user_soft_delete.sql`
- **Criado**: Script de migração para adicionar campos necessários
- **Incluído**: Índices para performance
- **Documentado**: Comentários explicativos

## 🔄 **Como Funciona Agora**

### Antes (Hard Delete):
1. Admin clica "Eliminar" → Utilizador é removido permanentemente
2. Dados perdidos para sempre
3. Logs incompletos

### Agora (Soft Delete):
1. Admin clica "Mover para Lixeira" → Utilizador é marcado como deletado
2. Dados mantidos na base de dados
3. Logs completos preservados
4. Possibilidade de restaurar no futuro

## 📊 **Benefícios**

### ✅ **Segurança**
- Dados nunca são perdidos permanentemente
- Auditoria completa mantida
- Possibilidade de recuperação

### ✅ **Consistência**
- Mesmo padrão usado em Chat e Question
- Código mais organizado e previsível

### ✅ **Flexibilidade**
- Pode implementar interface de lixeira no futuro
- Estatísticas completas mantidas
- Logs de atividade preservados

### ✅ **UX Melhorada**
- Mensagens mais claras sobre o que acontece
- Menos stress para o admin (não é permanente)
- Interface mais amigável

## 🚀 **Próximos Passos**

### Imediato:
1. **Executar migração**: `database_migration_user_soft_delete.sql`
2. **Testar funcionalidade**: Verificar se soft delete funciona
3. **Verificar logs**: Confirmar que auditoria está completa

### Futuro (Opcional):
1. **Interface de lixeira**: Página para ver utilizadores deletados
2. **Funcionalidade de restaurar**: Botão para restaurar utilizadores
3. **Filtros avançados**: Por data de deleção, admin que deletou, etc.

## 🔧 **Detalhes Técnicos**

### Campos Adicionados:
- `deleted_at`: Timestamp quando foi deletado
- `deleted_by`: Username do admin que deletou

### Métodos Disponíveis:
- `softDelete(username)`: Marca como deletado
- `isDeleted()`: Verifica se está deletado
- `restore()`: Remove marcação de deletado

### Queries Otimizadas:
- `findAllActive()`: Apenas utilizadores ativos
- `findAllDeleted()`: Apenas utilizadores deletados
- Índices criados para performance

## 📝 **Notas Importantes**

- **Compatibilidade**: Não afeta utilizadores existentes
- **Performance**: Índices criados para manter performance
- **Segurança**: Mesmas verificações de permissão mantidas
- **Logs**: Auditoria completa preservada 