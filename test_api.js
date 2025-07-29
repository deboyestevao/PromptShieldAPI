const axios = require('axios');

// Configuração base
const BASE_URL = 'http://localhost:8080';
let authToken = null;

// Função para fazer login
async function login() {
    try {
        console.log('🔐 Fazendo login...');
        const response = await axios.post(`${BASE_URL}/auth/login`, {
            username: 'admin',
            password: 'admin'
        });
        
        authToken = response.data.token;
        console.log('✅ Login realizado com sucesso');
        return true;
    } catch (error) {
        console.error('❌ Erro no login:', error.response?.data || error.message);
        return false;
    }
}

// Função para testar o endpoint de chat
async function testChatEndpoint() {
    try {
        console.log('🧪 Testando endpoint de chat...');
        const response = await axios.get(`${BASE_URL}/api/chat/test`, {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });
        
        console.log('✅ Teste de chat:', response.data);
        return true;
    } catch (error) {
        console.error('❌ Erro no teste de chat:', error.response?.data || error.message);
        return false;
    }
}

// Função para criar um chat
async function createChat() {
    try {
        console.log('📝 Criando chat...');
        const response = await axios.post(`${BASE_URL}/api/chat`, {
            name: 'Chat de Teste'
        }, {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });
        
        console.log('✅ Chat criado:', response.data);
        return response.data.id;
    } catch (error) {
        console.error('❌ Erro ao criar chat:', error.response?.data || error.message);
        return null;
    }
}

// Função para testar envio de mensagem
async function testSendMessage(chatId) {
    try {
        console.log('💬 Testando envio de mensagem...');
        const response = await axios.post(`${BASE_URL}/ai/ask`, {
            question: 'Olá, como você está?',
            chatId: chatId,
            fileIds: []
        }, {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });
        
        console.log('✅ Mensagem enviada:', response.data);
        return true;
    } catch (error) {
        console.error('❌ Erro ao enviar mensagem:', error.response?.data || error.message);
        return false;
    }
}

// Função principal de teste
async function runTests() {
    console.log('🚀 Iniciando testes da API...\n');
    
    // Teste 1: Login
    const loginSuccess = await login();
    if (!loginSuccess) {
        console.log('❌ Falha no login, abortando testes');
        return;
    }
    
    // Teste 2: Endpoint de chat
    const chatTestSuccess = await testChatEndpoint();
    if (!chatTestSuccess) {
        console.log('❌ Falha no teste de chat');
        return;
    }
    
    // Teste 3: Criar chat
    const chatId = await createChat();
    if (!chatId) {
        console.log('❌ Falha ao criar chat');
        return;
    }
    
    // Teste 4: Enviar mensagem
    const messageSuccess = await testSendMessage(chatId);
    if (!messageSuccess) {
        console.log('❌ Falha ao enviar mensagem');
        return;
    }
    
    console.log('\n🎉 Todos os testes passaram!');
}

// Executar testes
runTests().catch(console.error); 