// Script para testar a funcionalidade de atividade recente
const axios = require('axios');

const BASE_URL = 'http://localhost:8080';

async function testActivityLog() {
    console.log('🧪 Testando funcionalidade de Atividade Recente...\n');

    try {
        // 1. Testar endpoint de atividade recente
        console.log('1. Testando endpoint /admin/api/activity...');
        const activityResponse = await axios.get(`${BASE_URL}/admin/api/activity`);
        console.log('✅ Resposta:', activityResponse.data);
        console.log('📊 Número de atividades:', activityResponse.data.length);
        console.log('');

        // 2. Testar endpoint de estatísticas
        console.log('2. Testando endpoint /admin/api/activity/stats...');
        const statsResponse = await axios.get(`${BASE_URL}/admin/api/activity/stats`);
        console.log('✅ Estatísticas do log:', statsResponse.data);
        console.log('');

        // 3. Verificar estrutura das atividades
        if (activityResponse.data.length > 0) {
            console.log('3. Verificando estrutura das atividades...');
            const activity = activityResponse.data[0];
            console.log('📋 Estrutura da primeira atividade:');
            console.log('   - Type:', activity.type);
            console.log('   - Title:', activity.title);
            console.log('   - Description:', activity.description);
            console.log('   - Username:', activity.username);
            console.log('   - Timestamp:', activity.timestamp);
            console.log('');

            // 4. Verificar tipos de atividade
            console.log('4. Tipos de atividade encontrados:');
            const types = [...new Set(activityResponse.data.map(a => a.type))];
            types.forEach(type => {
                const count = activityResponse.data.filter(a => a.type === type).length;
                console.log(`   - ${type}: ${count} entradas`);
            });
            console.log('');

            // 5. Verificar ordenação (mais recente primeiro)
            console.log('5. Verificando ordenação (mais recente primeiro)...');
            const timestamps = activityResponse.data.map(a => new Date(a.timestamp));
            let isOrdered = true;
            for (let i = 1; i < timestamps.length; i++) {
                if (timestamps[i] > timestamps[i-1]) {
                    isOrdered = false;
                    break;
                }
            }
            console.log(isOrdered ? '✅ Ordenação correta' : '❌ Ordenação incorreta');
            console.log('');

        } else {
            console.log('3. Nenhuma atividade encontrada no log.');
            console.log('');
        }

        // 6. Verificar limite de 5 entradas
        console.log('6. Verificando limite de 5 entradas...');
        if (activityResponse.data.length <= 5) {
            console.log('✅ Limite de 5 entradas respeitado');
        } else {
            console.log('❌ Limite de 5 entradas excedido');
        }
        console.log('');

        console.log('🎉 Teste concluído com sucesso!');

    } catch (error) {
        console.error('❌ Erro durante o teste:', error.message);
        if (error.response) {
            console.error('Status:', error.response.status);
            console.error('Dados:', error.response.data);
        }
    }
}

// Executar teste
testActivityLog(); 