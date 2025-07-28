# TODO - Problemas Pendentes

## 🔧 Problemas a Resolver

### 1. Last Login At não está a ser atualizado
- **Problema**: O campo `last_login_at` não está a ser preenchido quando os utilizadores fazem login
- **Status**: ❌ Pendente
- **Solução**: Implementar atualização do `last_login_at` no processo de login
- **Prioridade**: Baixa

### 2. Página de Gestão de Utilizadores
- **Problema**: A página não está a mostrar os utilizadores existentes
- **Status**: 🔄 Em desenvolvimento
- **Solução**: Verificar endpoints e implementar funcionalidade de tornar outros utilizadores admin
- **Prioridade**: Alta

## 📝 Notas Técnicas

### Endpoints da API de Utilizadores
- `GET /admin/api/users` - Listar todos os utilizadores
- `GET /admin/api/users/{id}` - Obter detalhes de um utilizador
- `POST /admin/api/users/{id}/activate` - Ativar utilizador
- `POST /admin/api/users/{id}/deactivate` - Desativar utilizador
- `DELETE /admin/api/users/{id}` - Eliminar utilizador

### Regras de Negócio
- Admins podem tornar outros utilizadores admin
- Admins NÃO podem editar-se a si próprios
- Admins NÃO podem eliminar-se a si próprios

## 🚀 Próximos Passos
1. Testar endpoints da API de utilizadores
2. Implementar funcionalidade de tornar outros utilizadores admin
3. Corrigir problema do last_login_at (quando necessário) 