function uuidv4() {
  return "10000000-1000-4000-8000-100000000000".replace(/[018]/g, c =>
    (+c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> +c / 4).toString(16)
  );
}

function loadUserId() {
    let userId = localStorage.getItem('user-id');
    if (!userId) {
        userId = uuidv4();
        localStorage.setItem('user-id', userId);
    }
    return userId;
}

function getTabId() {
    // Cria um ID único para esta aba/sessão do navegador
    let tabId = sessionStorage.getItem('tab-id');
    if (!tabId) {
        tabId = 'tab-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
        sessionStorage.setItem('tab-id', tabId);
    }
    return tabId;
}

// Script de rastreamento para o blog
(function() {
    'use strict';
    let userId = loadUserId();
    let tabId = getTabId();
    
    const API_URL = `${window.location.protocol}//visita.vepo.dev/api/visita`;
    let visitaId = null;
    let isUnloading = false;
    
    // Verificar se já existe uma visita ativa para esta aba
    function loadExistingVisitaId() {
        // Usar o tabId como chave para armazenar o visitaId específico da aba
        const storedVisitaId = sessionStorage.getItem(`visitaId_${tabId}`);
        if (storedVisitaId) {
            console.log('Recuperando visita existente:', storedVisitaId);
            return storedVisitaId;
        }
        return null;
    }
    
    // Armazenar o ID da visita para esta aba específica
    function storeVisitaId(id) {
        visitaId = id;
        // Armazenar com chave específica da aba
        sessionStorage.setItem(`visitaId_${tabId}`, id);
        // Também armazenar timestamp para validade
        sessionStorage.setItem(`visitaTimestamp_${tabId}`, Date.now().toString());
        console.log('Visita armazenada:', id, 'para aba:', tabId);
    }
    
    // Limpar dados da visita para esta aba
    function clearVisitaData() {
        sessionStorage.removeItem(`visitaId_${tabId}`);
        sessionStorage.removeItem(`visitaTimestamp_${tabId}`);
        visitaId = null;
        console.log('Dados de visita limpos para aba:', tabId);
    }
    
    // Verificar se a visita ainda é válida (menos de 30 minutos)
    function isVisitaValid(storedId) {
        const timestamp = sessionStorage.getItem(`visitaTimestamp_${tabId}`);
        if (!timestamp) return false;
        
        const age = Date.now() - parseInt(timestamp, 10);
        // Considerar válida por até 30 minutos (1800000 ms)
        return age < 1800000;
    }
    
    // Função para enviar dados para o servidor
    async function enviarDados(endpoint, dados) {
        const url = new URL(API_URL + endpoint);
        
        // Para requisições POST, enviar como JSON
        if (endpoint === '/acesso') {
            try {
                console.info('Acessing', url, dados)
                const response = await fetch(url.toString(), {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json',
                    },
                    body: JSON.stringify(dados),
                    credentials: 'omit',
                    keepalive: true // Mantém a requisição ativa mesmo após a página fechar
                });
                console.log("Response: ", response);
                if (response.ok) {
                    const result = await response.json();
                    if (result.id) {
                        storeVisitaId(result.id);
                        console.log('Visita criada com ID:', result.id);
                        return result.id;
                    }
                }
            } catch (error) {
                console.error('Erro ao enviar dados de acesso:', error);
                // Fallback: criar ID local
                const localVisitaId = `local-${tabId}-${Date.now()}`;
                storeVisitaId(localVisitaId);
                return localVisitaId;
            }
        } else if (endpoint === '/saida') {
            // Para saída, usar sendBeacon ou fetch com keepalive
            // if (navigator.sendBeacon) {
            //     const blob = new Blob([JSON.stringify(dados)], {type: 'application/json'});
            //     navigator.sendBeacon(url.toString(), blob);
            // } else {
                // Fallback com keepalive
                fetch(url.toString(), {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(dados),
                    credentials: 'omit',
                    keepalive: true
                }).catch(() => {/* Ignorar erros em saída */});
            // }
        }
        return null;
    }
    
    // Coletar informações da visita
    async function coletarInformacoes() {
        // Verificar se já temos uma visita válida para esta aba
        const existingVisitaId = loadExistingVisitaId();
        if (existingVisitaId && isVisitaValid(existingVisitaId)) {
            visitaId = existingVisitaId;
            console.log('Continuando visita existente:', visitaId);
            return;
        }
        
        // Se não tiver visita válida, limpar dados antigos
        if (existingVisitaId) {
            clearVisitaData();
        }
        
        const dados = {
            userId: userId,
            tabId: tabId,
            pagina: window.location.href,
            referer: document.referrer || 'direct',
            userAgent: navigator.userAgent,
            screenResolution: `${window.screen.width}x${window.screen.height}`,
            language: navigator.language,
            timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
            timestamp: Date.now()
        };
        
        // Registrar novo acesso
        const novaVisitaId = await enviarDados('/acesso', dados);
        if (novaVisitaId) {
            visitaId = novaVisitaId;
        }
    }
    
    // Registrar saída
    function registrarSaida() {
        if (isUnloading) return; // Evitar múltiplos envios
        console.log(`Registrando saída.... visitaId=${visitaId}`)
        isUnloading = true;
        
        if (visitaId) {
            const dados = {
                id: visitaId,
                userId: userId,
                tabId: tabId,
                timestamp: new Date().toISOString()
            };
            
            enviarDados('/saida', dados);
            clearVisitaData();
        }
    }
    
    // Monitorar atividade do usuário
    function setupActivityMonitoring() {
        let lastActivity = Date.now();
        const activityEvents = ['mousedown', 'mousemove', 'keydown', 'scroll', 'touchstart'];
        
        activityEvents.forEach(event => {
            document.addEventListener(event, () => {
                lastActivity = Date.now();
            }, { passive: true });
        });
        
        // Verificar periodicamente se o usuário está inativo
        setInterval(() => {
            const now = Date.now();
            const inactiveTime = now - lastActivity;
            
            // Se estiver inativo por mais de 5 minutos, considerar como saída
            if (inactiveTime > 300000 && visitaId) {
                console.log('Usuário inativo por mais de 5 minutos');
                // Opcional: enviar evento de inatividade
            }
        }, 60000); // Verificar a cada minuto
    }
    
    // Evento de saída da página
    window.addEventListener('beforeunload', function(e) {
        if (!isUnloading) {
            registrarSaida();
        }
    });
    
    // Evento de mudança de visibilidade (aba inativa/ativa)
    document.addEventListener('visibilitychange', function() {
        if (document.visibilityState === 'hidden') {
            // Usar sendBeacon para garantir o envio se a página estiver fechando
            if (visitaId && !isUnloading) {
                const dados = {
                    id: visitaId,
                    userId: userId,
                    tabId: tabId,
                    tipo: 'visibility_hidden',
                    timestamp: new Date().toISOString()
                };
                
                if (navigator.sendBeacon) {
                    const blob = new Blob([JSON.stringify(dados)], {type: 'application/json'});
                    navigator.sendBeacon(API_URL + '/saida', blob);
                }
            }
        } else if (document.visibilityState === 'visible') {
            // Quando a aba volta a ficar visível, verificar se precisa renovar a visita
            const existingVisitaId = loadExistingVisitaId();
            if (!existingVisitaId || !isVisitaValid(existingVisitaId)) {
                // Se a visita expirou, coletar informações novamente
                coletarInformacoes();
            }
        }
    });
    
    // Monitorar mudanças de página (para SPAs - Single Page Applications)
    let lastUrl = window.location.href;
    new MutationObserver(() => {
        const currentUrl = window.location.href;
        if (currentUrl !== lastUrl) {
            console.log('Mudança de página detectada:', lastUrl, '->', currentUrl);
            lastUrl = currentUrl;
            
            // Registrar página view para a mesma visita
            if (visitaId) {
                const dados = {
                    visitaId: visitaId,
                    pagina: currentUrl,
                    timestamp: new Date().toISOString()
                };
                
                fetch(API_URL + '/pagina', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(dados)
                }).catch(() => {});
            }
        }
    }).observe(document, { subtree: true, childList: true });
    
    // Iniciar coleta quando o DOM estiver pronto
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function() {
            coletarInformacoes();
            setupActivityMonitoring();
        });
    } else {
        coletarInformacoes();
        setupActivityMonitoring();
    }
    
    // Expor funções globais para debug e integração
    window.VisitaTracker = {
        coletarInformacoes: coletarInformacoes,
        registrarSaida: registrarSaida,
        getVisitaId: function() { return visitaId; },
        getTabId: function() { return tabId; },
        getUserId: function() { return userId; },
        clearData: clearVisitaData,
        forceNewVisita: function() {
            clearVisitaData();
            return coletarInformacoes();
        }
    };
    
    console.log('Visita Analytics carregado com sucesso!');
    console.log('Tab ID:', tabId);
    console.log('User ID:', userId);
})();