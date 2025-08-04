# Resumo da Atualização do Campo lastActive

## Problema Identificado

O campo `lastActive` da entidade `User` estava a ficar `null` porque não estava a ser atualizado nas ações principais dos utilizadores.

## Solução Implementada

Adicionei a atualização do campo `lastActive` nos seguintes métodos onde os utilizadores realizam ações importantes:

### 1. Fazer Perguntas à AI
**Arquivo**: `src/main/java/com/example/PromptShieldAPI/implementations/QuestionServiceImpl.java`
- **Método**: `saveQuestion()`
- **Alteração**: Adicionado `user.setLastActive(java.time.LocalDateTime.now())` após salvar a pergunta

### 2. Criar Chats
**Arquivo**: `src/main/java/com/example/PromptShieldAPI/controller/ChatController.java`
- **Método**: `createChat()`
- **Alteração**: Adicionado `user.setLastActive(java.time.LocalDateTime.now())` após criar o chat

### 3. Atualizar Chats
**Arquivo**: `src/main/java/com/example/PromptShieldAPI/controller/ChatController.java`
- **Método**: `updateChat()`
- **Alteração**: Adicionado `user.setLastActive(java.time.LocalDateTime.now())` quando o nome do chat é atualizado

### 4. Remover Chats
**Arquivo**: `src/main/java/com/example/PromptShieldAPI/controller/ChatController.java`
- **Método**: `deleteChat()`
- **Alteração**: Adicionado `user.setLastActive(java.time.LocalDateTime.now())` após fazer soft delete do chat

### 5. Restaurar Chats
**Arquivo**: `src/main/java/com/example/PromptShieldAPI/controller/ChatController.java`
- **Método**: `restoreChat()`
- **Alteração**: Adicionado `user.setLastActive(java.time.LocalDateTime.now())` após restaurar o chat

### 6. Alterar Preferências de LLM
**Arquivo**: `src/main/java/com/example/PromptShieldAPI/service/AdminService.java`
- **Método**: `updateUserLLMPreferences()`
- **Alteração**: Adicionado `user.setLastActive(java.time.LocalDateTime.now())` após atualizar as preferências

### 7. Upload de Ficheiros
**Arquivo**: `src/main/java/com/example/PromptShieldAPI/controller/FileController.java`
- **Método**: `uploadFiles()`
- **Alteração**: Adicionado `user.setLastActive(java.time.LocalDateTime.now())` após fazer upload dos ficheiros

## Funcionalidades Mantidas

- **Login/Logout**: O `lastActive` já estava a ser atualizado no `AuthService` e `AuthControllerWeb`
- **Campo existente**: Não foi necessário criar novos campos, apenas atualizar o campo `lastActive` existente

## Benefícios

1. **Rastreamento de atividade**: Agora é possível saber quando foi a última vez que cada utilizador realizou uma ação importante
2. **Auditoria**: Facilita a monitorização da atividade dos utilizadores
3. **Análise de uso**: Permite identificar utilizadores ativos vs. inativos
4. **Manutenção**: O campo não fica mais `null`, mantendo dados consistentes

## Próximos Passos

1. **Testar aplicação**: Verificar se todas as ações atualizam corretamente o `lastActive`
2. **Monitorizar logs**: Confirmar que não há erros relacionados às atualizações
3. **Verificar performance**: As atualizações são mínimas e não devem impactar a performance

## Notas Técnicas

- Todas as atualizações usam `java.time.LocalDateTime.now()` para consistência
- As atualizações são feitas dentro das transações existentes
- Não foram necessárias alterações no modelo de dados
- O código mantém a mesma estrutura e padrões existentes 