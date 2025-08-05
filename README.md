# PromptShieldAPI - Documentação Completa

## 📋 Índice
1. [Visão Geral do Projeto](#visão-geral-do-projeto)
2. [Arquitetura e Tecnologias](#arquitetura-e-tecnologias)
3. [Estrutura de Ficheiros](#estrutura-de-ficheiros)
4. [Funcionalidades para Utilizadores](#funcionalidades-para-utilizadores)
5. [Funcionalidades para Administradores](#funcionalidades-para-administradores)
6. [Sistema de Segurança](#sistema-de-segurança)
7. [Sistema de Mascaramento de Dados](#sistema-de-mascaramento-de-dados)
8. [Endpoints da API](#endpoints-da-api)
9. [Modelos de Dados](#modelos-de-dados)
10. [Serviços](#serviços)
11. [Configuração e Instalação](#configuração-e-instalação)
12. [Testes](#testes)

---

## 🎯 Visão Geral do Projeto

O **PromptShieldAPI** é uma plataforma de chat inteligente com IA que permite aos utilizadores fazer perguntas e receber respostas de modelos de linguagem (OpenAI e Ollama), com foco especial na segurança e proteção de dados sensíveis.

### Características Principais:
- **Chat com IA**: Integração com OpenAI e Ollama
- **Proteção de Dados**: Sistema avançado de mascaramento de dados sensíveis
- **Gestão de Utilizadores**: Sistema completo de autenticação e autorização
- **Painel Administrativo**: Interface de gestão para administradores
- **Upload de Ficheiros**: Suporte para múltiplos formatos (PDF, DOCX, XLSX, etc.)
- **Histórico de Conversas**: Sistema de chat persistente
- **Relatórios e Monitorização**: Sistema de logs e relatórios

---

## 🏗️ Arquitetura e Tecnologias

### Stack Tecnológico:
- **Backend**: Spring Boot 3.x (Java 17+)
- **Base de Dados**: MySQL/PostgreSQL
- **Segurança**: Spring Security
- **IA**: OpenAI API, Ollama
- **Frontend**: Thymeleaf + CSS + JavaScript
- **Build**: Maven

### Padrões Arquiteturais:
- **MVC**: Model-View-Controller
- **Repository Pattern**: Para acesso a dados
- **Service Layer**: Para lógica de negócio
- **DTO Pattern**: Para transferência de dados

---

## 📁 Estrutura de Ficheiros

```
PromptShieldAPI/
├── src/main/java/com/example/PromptShieldAPI/
│   ├── controller/           # Controladores REST e Web
│   ├── service/             # Lógica de negócio
│   ├── repository/          # Acesso a dados
│   ├── model/              # Entidades JPA
│   ├── dto/                # Data Transfer Objects
│   ├── exception/          # Tratamento de exceções
│   ├── util/               # Utilitários (DataMasker)
│   └── interfaces/         # Interfaces de serviços
├── src/main/resources/
│   ├── templates/          # Templates Thymeleaf
│   ├── static/css/         # Estilos CSS
│   ├── application.properties
│   └── system-prompt.config
└── src/test/               # Testes unitários
```

---

## 👤 Funcionalidades para Utilizadores

### 1. **Autenticação e Registo**
- **Registo**: `/auth/register` - Criar nova conta
- **Login**: `/auth/login` - Autenticação no sistema
- **Logout**: `/auth/logout` - Terminar sessão

### 2. **Chat com IA**
- **Interface Principal**: `/chat` - Página principal do chat
- **Fazer Perguntas**: Endpoint `/ai/ask` - Enviar perguntas para IA
- **Histórico**: Visualizar conversas anteriores
- **Múltiplos Chats**: Criar e gerir diferentes conversas

### 3. **Upload de Ficheiros**
- **Upload**: `/files/upload` - Carregar ficheiros para análise
- **Formatos Suportados**:
    - Documentos: PDF, DOCX, PPTX
    - Folhas de cálculo: XLSX
    - Texto: TXT, CSV, JSON, XML
- **Processamento Seguro**: Todos os ficheiros são processados com mascaramento de dados

### 4. **Gestão de Chats**
- **Criar Chat**: `POST /api/chat` - Nova conversa
- **Listar Chats**: `GET /api/chat` - Ver conversas ativas
- **Atualizar Chat**: `PUT /api/chat/{id}` - Renomear conversa
- **Eliminar Chat**: `DELETE /api/chat/{id}` - Remover conversa
- **Restaurar Chat**: `POST /api/chat/{id}/restore` - Recuperar chat eliminado
- **Ver Perguntas**: `GET /api/chat/{id}/questions` - Histórico de perguntas

### 5. **Preferências de IA**
- **Configurar Preferências**: Escolher entre OpenAI e Ollama
- **Alternar Modelos**: Mudar entre diferentes modelos de IA
- **Ver Status**: Verificar disponibilidade dos modelos

### 6. **Relatórios de Problemas**
- **Reportar Problema**: `/report-account-issue` - Reportar problemas de conta
- **Status da Conta**: Verificar se a conta está ativa/desativada

---

## 🔧 Funcionalidades para Administradores

### 1. **Painel de Controlo**
- **Dashboard**: `/admin/dashboard` - Visão geral do sistema
- **Estatísticas**: Utilizadores ativos, conversas, performance
- **Monitorização**: Status dos modelos de IA

### 2. **Gestão de Utilizadores**
- **Listar Utilizadores**: `/admin/users` - Ver todos os utilizadores
- **Detalhes do Utilizador**: `GET /admin/api/users/{id}` - Informações detalhadas
- **Ativar/Desativar**: `POST /admin/api/users/{id}/activate|deactivate`
- **Eliminar Utilizador**: `DELETE /admin/api/users/{id}` - Soft delete
- **Restaurar Utilizador**: `POST /admin/api/users/{id}/restore`
- **Promover a Admin**: `POST /admin/api/users/{id}/make-admin`
- **Remover Admin**: `POST /admin/api/users/{id}/remove-admin`
- **Lixeira**: `/admin/users/trash` - Utilizadores eliminados

### 3. **Configurações do Sistema**
- **Preferências do Sistema**: `/admin/system-preferences`
- **Configurar IA**: Ativar/desativar OpenAI e Ollama
- **Manutenção**: Desativar temporariamente modelos
- **Histórico de Configurações**: `/admin/config-history`

### 4. **Relatórios e Monitorização**
- **Relatórios de Conta**: `/admin/reports` - Ver reports de utilizadores
- **Aprovar/Rejeitar**: `POST /admin/api/reports/{id}/approve|reject`
- **Estatísticas de Performance**: `GET /admin/api/performance`
- **Contas Inativas**: `GET /admin/api/inactive-accounts`
- **Estatísticas de IA**: `GET /admin/api/llms`

### 5. **Sistema de Notificações**
- **Ver Notificações**: `GET /admin/api/notifications`
- **Marcar como Lida**: `POST /admin/api/notifications/{id}/read`
- **Marcar Todas como Lidas**: `POST /admin/api/notifications/read-all`
- **Contador**: `GET /admin/api/notifications/count`

### 6. **Logs de Atividade**
- **Atividade Recente**: `GET /admin/api/activity`
- **Estatísticas de Logs**: `GET /admin/api/activity/stats`
- **Monitorização**: Acompanhar ações dos utilizadores

---

## 🔒 Sistema de Segurança

### 1. **Autenticação**
- **Spring Security**: Framework de segurança
- **Sessões**: Gestão de sessões de utilizador
- **Password Hashing**: Encriptação de passwords
- **JWT**: Tokens de autenticação (se configurado)

### 2. **Autorização**
- **Roles**: USER, ADMIN
- **Anotações**: `@PreAuthorize("hasRole('ADMIN')")`
- **URL Protection**: Proteção de endpoints por role

### 3. **Proteção de Dados**
- **Mascaramento**: Sistema avançado de mascaramento
- **Validação**: Validação de entrada de dados
- **Sanitização**: Limpeza de dados de entrada

### 4. **Auditoria**
- **Activity Logs**: Registro de todas as ações
- **User Tracking**: Rastreamento de utilizadores
- **Security Events**: Eventos de segurança

---

## 🛡️ Sistema de Mascaramento de Dados

### Classe Principal: `DataMasker.java`

O sistema de mascaramento é o coração da segurança da aplicação, protegendo dados sensíveis antes de serem enviados para modelos de IA.

#### Métodos de Mascaramento:

1. **`maskEmails()`** - Mascara endereços de email
    - Formato: `user@domain.com` → `u***@d***.c***`

2. **`maskCVC()`** - Mascara códigos CVC
    - Formato: `123` → `***`

3. **`maskIBAN()`** - Mascara números IBAN
    - Portugal: `PT50.****.****.***********.**`
    - Angola: `AO06.****.****.***********.**`
    - Moçambique: `MZ59.****.****.***********.**`

4. **`maskVAT()`** - Mascara números de contribuinte
    - Portugal: `PT 123***456`
    - Angola: `AO 123***456`

5. **`maskPhoneNumbers()`** - Mascara números de telefone
    - Portugal: `9** *** **9`
    - Internacional: `351 9** *** **9`

6. **`maskCreditCard()`** - Mascara cartões de crédito
    - Formato: `1234 **** **** 5678`

7. **`maskNineDigitNumber()`** - Mascara números de 9 dígitos
    - Formato: `1** *** **9`

8. **`maskPostalCode()`** - Mascara códigos postais
    - Formato: `****-***`

9. **`maskDates()`** - Mascara datas
    - Formato: `**/**/****`

10. **`maskCardExpiry()`** - Mascara datas de validade
    - Formato: `**/**`

11. **`maskBalance()`** - Mascara valores monetários
    - Formato: `***€` (proporcional ao tamanho)

12. **`maskName()`** - Mascara nomes
    - Formato: `R****** P******`

13. **`maskAddress()`** - Mascara endereços
    - Formato: `Rua E*****, ***`

#### Processo de Mascaramento:
```java
public static MaskingResult maskSensitiveData(String input) {
    // Aplica todos os métodos de mascaramento sequencialmente
    // Retorna texto com dados sensíveis mascarados
}
```

---

## 🌐 Endpoints da API

### Autenticação (`/api/auth`)
- `POST /api/auth/login` - Login de utilizador
- `POST /api/auth/register` - Registo de novo utilizador
- `DELETE /api/auth/delete/{id}` - Eliminar utilizador

### Chat (`/api/chat`)
- `GET /api/chat` - Listar chats do utilizador
- `POST /api/chat` - Criar novo chat
- `PUT /api/chat/{id}` - Atualizar chat
- `DELETE /api/chat/{id}` - Eliminar chat
- `POST /api/chat/{id}/restore` - Restaurar chat
- `GET /api/chat/{id}/questions` - Perguntas do chat
- `GET /api/chat/deleted` - Chats eliminados

### IA (`/ai`)
- `GET /ai/welcome` - Mensagem de boas-vindas
- `POST /ai/ask` - Fazer pergunta à IA

### Ficheiros (`/files`)
- `POST /files/upload` - Upload de ficheiros

### Administração (`/admin`)
- `PATCH /admin/system-preferences` - Atualizar configurações
- `POST /admin/user-preferences` - Preferências de utilizador
- `GET /admin/llm-status` - Status dos modelos de IA
- `POST /admin/llm/temporary-disable` - Desativar temporariamente
- `GET /admin/api/users` - Listar utilizadores
- `GET /admin/api/users/{id}` - Detalhes do utilizador
- `POST /admin/api/users/{id}/activate` - Ativar utilizador
- `POST /admin/api/users/{id}/deactivate` - Desativar utilizador
- `DELETE /admin/api/users/{id}` - Eliminar utilizador
- `POST /admin/api/users/{id}/restore` - Restaurar utilizador
- `POST /admin/api/users/{id}/make-admin` - Promover a admin
- `POST /admin/api/users/{id}/remove-admin` - Remover admin
- `GET /admin/api/performance` - Estatísticas de performance
- `GET /admin/api/activity` - Logs de atividade
- `GET /admin/api/reports` - Relatórios de utilizadores
- `POST /admin/api/reports/{id}/approve` - Aprovar report
- `POST /admin/api/reports/{id}/reject` - Rejeitar report

### Status da Conta
- `GET /check-account-status` - Verificar status da conta
- `GET /account-disabled` - Página de conta desativada
- `GET /account-deleted` - Página de conta eliminada
- `POST /report-account-issue` - Reportar problema

---

## 📊 Modelos de Dados

### User
- `id`: Identificador único
- `username`: Nome de utilizador (email)
- `password`: Password encriptada
- `firstName`: Primeiro nome
- `lastName`: Último nome
- `role`: Role (USER/ADMIN)
- `active`: Se a conta está ativa
- `deleted`: Se a conta foi eliminada
- `lastActive`: Última atividade
- `lastLoginAt`: Último login
- `preferences`: Preferências do utilizador

### Chat
- `id`: Identificador único
- `name`: Nome da conversa
- `user`: Utilizador proprietário
- `deleted`: Se foi eliminado
- `createdAt`: Data de criação

### Question
- `id`: Identificador único
- `question`: Pergunta feita
- `answer`: Resposta da IA
- `model`: Modelo usado (openai/ollama)
- `chat`: Chat associado
- `createdAt`: Data de criação

### SystemConfig
- `id`: Identificador único
- `openaiEnabled`: Se OpenAI está ativo
- `ollamaEnabled`: Se Ollama está ativo
- `version`: Versão da configuração

### UserPreferences
- `id`: Identificador único
- `user`: Utilizador
- `ollamaPreferred`: Se prefere Ollama
- `openaiPreferred`: Se prefere OpenAI

### AccountReport
- `id`: Identificador único
- `user`: Utilizador que reportou
- `reason`: Motivo do report
- `status`: Status (PENDING/APPROVED/REJECTED)
- `createdAt`: Data de criação

### Notification
- `id`: Identificador único
- `title`: Título da notificação
- `message`: Mensagem
- `read`: Se foi lida
- `createdAt`: Data de criação

### ConfigHistory
- `id`: Identificador único
- `action`: Ação realizada
- `details`: Detalhes da mudança
- `performedBy`: Quem fez a mudança
- `timestamp`: Data/hora

---

## ⚙️ Serviços

### AuthService
- **Registo**: Criar novos utilizadores
- **Login**: Autenticação de utilizadores
- **Validação**: Validação de dados de entrada

### AiService
- **OpenAI**: Integração com OpenAI
- **Ollama**: Integração com Ollama
- **Contexto**: Construção de contexto de chat
- **Mascaramento**: Aplicação de mascaramento antes de enviar para IA

### FileService
- **Upload**: Processamento de ficheiros
- **Extração**: Extração de texto de diferentes formatos
- **Mascaramento**: Aplicação de mascaramento ao conteúdo
- **Tokens**: Estimativa de tokens para limites

### AdminService
- **Gestão de Utilizadores**: CRUD completo de utilizadores
- **Configurações**: Gestão de configurações do sistema
- **Relatórios**: Processamento de reports
- **Estatísticas**: Geração de estatísticas

### SystemConfigService
- **Configurações**: Gestão de configurações do sistema
- **Status de IA**: Verificação de status dos modelos
- **Manutenção**: Gestão de modo de manutenção

### NotificationService
- **Criação**: Criar notificações
- **Gestão**: Marcar como lidas
- **Contagem**: Contar notificações não lidas

### ActivityLogService
- **Registo**: Registar atividades
- **Consulta**: Consultar logs
- **Estatísticas**: Gerar estatísticas de atividade

### LLMAutoReactivationService
- **Reativação**: Reativação automática de modelos
- **Verificação**: Verificar se deve reativar
- **Agendamento**: Agendar reativações

---

## 🚀 Configuração e Instalação

### Pré-requisitos
- Java 17+
- Maven 3.6+
- MySQL/PostgreSQL
- OpenAI API Key (opcional)
- Ollama (opcional)

### Configuração da Base de Dados
```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/promptshield
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

### Configuração de IA
```properties
# OpenAI
openai.api.key=your_openai_api_key
openai.model=gpt-3.5-turbo

# Ollama
ollama.base.url=http://localhost:11434
ollama.model=llama2
```

### Execução
```bash
# Compilar
mvn clean compile

# Executar
mvn spring-boot:run

# Ou
java -jar target/PromptShieldAPI-0.0.1-SNAPSHOT.jar
```

### Acesso
- **Aplicação**: http://localhost:8080
- **Login**: http://localhost:8080/auth/login
- **Admin**: http://localhost:8080/admin/dashboard

---

## 🧪 Testes

### Testes Unitários
- **DataMaskerTest**: Testes do sistema de mascaramento
- **AuthServiceTest**: Testes de autenticação
- **AiServiceTest**: Testes de integração com IA
- **FileServiceTest**: Testes de processamento de ficheiros
- **AdminServiceTest**: Testes de funcionalidades administrativas

### Execução de Testes
```bash
# Executar todos os testes
mvn test

# Executar testes específicos
mvn test -Dtest=DataMaskerTest

# Relatório de cobertura
mvn jacoco:report
```

### Testes de Integração
- **ApiControllerTest**: Testes de endpoints da API
- **SecurityTest**: Testes de segurança
- **DatabaseTest**: Testes de persistência

---

## 📝 Notas Importantes

### Segurança
- Todos os dados sensíveis são mascarados antes de serem enviados para IA
- Sistema de roles implementado (USER/ADMIN)
- Validação de entrada em todos os endpoints
- Logs de atividade para auditoria

### Performance
- Sistema de cache para configurações
- Limite de tokens para evitar sobrecarga
- Processamento assíncrono de ficheiros grandes
- Otimização de consultas à base de dados

### Manutenção
- Sistema de notificações para administradores
- Logs detalhados de todas as operações
- Backup automático de configurações
- Monitorização de performance

### Escalabilidade
- Arquitetura modular
- Separação de responsabilidades
- Configuração flexível
- Suporte a múltiplos modelos de IA

---

## 🤝 Contribuição

Para contribuir para o projeto:

1. Fork o repositório
2. Crie uma branch para a sua feature
3. Implemente as mudanças
4. Adicione testes
5. Submeta um Pull Request

### Padrões de Código
- Seguir convenções Java
- Documentar métodos públicos
- Adicionar testes para novas funcionalidades
- Manter cobertura de testes alta

---

## 📄 Licença

Este projeto está licenciado sob a licença MIT. Ver o ficheiro LICENSE para mais detalhes.

---

## 📞 Suporte

Para questões ou problemas:
- Abrir uma issue no GitHub
- Contactar a equipa de desenvolvimento
- Consultar a documentação técnica

---

*Documentação atualizada em: Janeiro 2025* 