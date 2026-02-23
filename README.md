# 🔍 Visita Analytics - Sistema Avançado de Rastreamento

![JavaScript](https://img.shields.io/badge/JavaScript-ES6+-yellow.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Browser](https://img.shields.io/badge/Browser-Compatible-green.svg)
![Size](https://img.shields.io/badge/Size-6.5KB-gree.svg)

Um sistema de rastreamento de visitas profissional com suporte a sessões, múltiplas abas, SPAs e resiliência a falhas.

## 🚀 Instalação Instantânea

Adicione uma única linha ao seu HTML:

```html
<script async src="https://visita.vepo.dev/visita.js"></script>
```

O script carrega de forma assíncrona e se auto-inicializa automaticamente.

## ✨ Características Principais

### 🎯 Identificação Avançada
- **ID de Usuário Persistente** - Armazenado em localStorage
- **ID por Aba/Navegador** - Sessões independentes por aba
- **IDs de Sessão Únicos** - Com fallback para cenários offline

### 🔄 Suporte a SPAs
- **Detecção Automática** - MutationObserver para mudanças de rota
- **Atualização de Sessão** - Sessões mantidas entre navegações
- **Page Views Rastreáveis** - Cada mudança de página registrada

### ⚡ Performance Otimizada
- **sendBeacon API** - Registro de saída sem bloquear descarregamento
- **Request com Retry** - Tenta 3 vezes antes de falhar
- **Armazenamento Local** - Dados persistentes no cliente

### 📊 Coleta de Dados Abrangente
```javascript
{
  userId: "uuid-unico",
  tabId: "tab-unico",
  visitaId: "sessao-atual",
  page: "url-completa",
  referer: "origem",
  userAgent: "navegador",
  screenResolution: "1920x1080",
  colorDepth: 24,
  language: "pt-BR",
  timezone: "America/Sao_Paulo",
  timestamp: "2024-01-15T10:30:00Z"
}
```

## 🏗️ Arquitetura

### Fluxo de Inicialização
```
1. Carrega script → 2. Gera/Carrega IDs → 3. Verifica sessão existente
       ↓                    ↓                       ↓
4. Sessão válida? → Sim: Resgata sessão → 6. Inicia monitoramento
       ↓
Não: Cria nova sessão
       ↓
5. Envia para backend / Fallback local
```

### Gerenciamento de Sessão
- **Timeout**: 30 minutos de inatividade
- **Inatividade**: 5 minutos sem atividade
- **Checagem**: Ping a cada 5 segundos
- **Persistência**: sessionStorage para dados de sessão

## 🛠️ API Pública

### Métodos Disponíveis
```javascript
// Acessar identificadores
window.VisitaAnalytics.getSessionId();    // → Retorna ID da sessão atual
window.VisitaAnalytics.getTabId();        // → Retorna ID da aba
window.VisitaAnalytics.getUserId();       // → Retorna ID do usuário

// Controle manual
window.VisitaAnalytics.forceNewSession(); // Força nova sessão
window.VisitaAnalytics.getSessionData();  // → Retorna todos os dados

// Debug
console.log(window.VisitaAnalytics);      // Inspecione estado interno
```

### Eventos Monitorados Automaticamente
- **beforeunload/pagehide** - Saída da página
- **visibilitychange** - Aba oculta/visível
- **Atividade do usuário** - Cliques, teclas, scroll, mouse
- **Mudanças de DOM** - Para SPAs (via MutationObserver)

## 🔧 Configuração

### Endpoints do Backend
O sistema espera os seguintes endpoints:

```javascript
POST /api/tracking/access     // Início de sessão
POST /api/tracking/exit       // Fim de sessão (com sendBeacon)
POST /api/tracking/view       // Mudança de página (SPAs)
POST /api/tracking/ping       // Keep-alive da sessão
```

### Configurações Internas (Customizáveis)
```javascript
{
  SESSION_TIMEOUT: 30 * 60 * 1000,       // 30 minutos
  INACTIVITY_THRESHOLD: 5 * 60 * 1000,   // 5 minutos
  ACTIVITY_CHECK_INTERVAL: 5 * 1000,     // 5 segundos
  RETRY_ATTEMPTS: 3,                     // Tentativas de requisição
  RETRY_DELAY: 1000                      // Delay entre tentativas
}
```

## 📈 Casos de Uso

### 1. Website Tradicional
```html
<!-- Basta incluir o script -->
<script async src="https://visita.vepo.dev/visita.js" data-token="your-token"></script>
```

### 2. Single Page Application (SPA)
```javascript
// O script detecta automaticamente mudanças de rota
// Para forçar tracking manual:
window.VisitaAnalytics.trackPageView('/nova-rota');
```

### 3. E-commerce
```javascript
// Integre com eventos de conversão
document.querySelector('.checkout-button').addEventListener('click', () => {
  // Sua lógica de checkout...
  console.log('Sessão:', window.VisitaAnalytics.getSessionId());
});
```

## 🚨 Considerações Importantes

### Privacidade e Compliance
1. **Transparência** - Informe usuários sobre coleta de dados
2. **Consentimento** - Implemente opt-in para GDPR/LGPD
3. **Anonimização** - Considere hashear dados sensíveis
4. **Retenção** - Defina política de armazenamento

### Exemplo de Cookie Banner
```html
<div id="cookie-banner" style="display: none;">
  Usamos cookies para analytics. 
  <button onclick="acceptCookies()">Aceitar</button>
  <button onclick="rejectCookies()">Rejeitar</button>
</div>

<script>
function acceptCookies() {
  localStorage.setItem('cookies-accepted', 'true');
  // O VisitaAnalytics já está rodando, só precisa do consentimento
}
</script>
```

## 🔍 Debug e Monitoramento

### Console do Navegador
```javascript
// Verifique o status
window.VisitaAnalytics.logSystemStatus();

// Monitore eventos
// As requisições aparecem na aba Network com filtro: /api/visita

// Verifique armazenamento
console.log('LocalStorage:', localStorage.getItem('visita-user-id'));
console.log('SessionStorage:', sessionStorage.getItem('visita-tab-id'));
```

### Logs Automáticos
O sistema emite logs informativos no console:
- ✅ Inicialização bem-sucedida
- 🔄 Sessão resumida/criada
- 📤 Registro de saída
- ⚠️ Fallbacks e erros tratados

## ⚡ Performance

### Otimizações Implementadas
1. **Carregamento Assíncrono** - Não bloqueia renderização
2. **sendBeacon para Exit** - Não interfere no pagehide
3. **Retry Inteligente** - Fallback para falhas de rede
4. **Armazenamento Local** - Minimiza requisições

### Impacto na Performance
- **Tamanho**: ~6.5KB (minificado)
- **Memória**: Uso mínimo após inicialização
- **Rede**: 1 requisição inicial + pings periódicos
- **CPU**: Quase zero quando inativo

## 🔄 Resiliência e Fallbacks

### Cenários Tratados
1. **Backend Offline** → Sessão local com ID fallback
2. **sendBeacon não suportado** → Fallback para fetch com keepalive
3. **Storage bloqueado** → IDs temporários em memória
4. **Script bloqueado** → Degrade gracefulmente

### Sistema de Retry
```javascript
// Tentativa 1: Requisição normal
// Tentativa 2: Aguarda 1 segundo
// Tentativa 3: Aguarda 2 segundos
// Falha: Cria sessão local
```

## 📊 Análise de Dados

### Métricas Capturadas
- **Sessões Ativas** - Por usuário e por aba
- **Duração de Sessão** - Tempo entre access/exit
- **Inatividade** - Períodos sem interação
- **Origens** - Referers e URLs de entrada
- **Dispositivos** - Resolução, navegador, idioma

### Exemplo de Dashboard SQL
```sql
-- Visitas únicas por dia
SELECT DATE(timestamp), COUNT(DISTINCT userId) 
FROM visitas 
GROUP BY DATE(timestamp);

-- Tempo médio por sessão
SELECT AVG(duracao) 
FROM (
  SELECT visitaId, MAX(timestamp) - MIN(timestamp) as duracao
  FROM eventos 
  GROUP BY visitaId
);

-- Páginas mais visitadas
SELECT page, COUNT(*) as acessos 
FROM page_views 
GROUP BY page 
ORDER BY acessos DESC;
```

## 🧪 Testes

### Cenários Testados
- ✅ Múltiplas abas simultâneas
- ✅ Navegação SPA (React, Vue, Angular)
- ✅ Recarga de página (F5)
- ✅ Navegação entre páginas (link tradicional)
- ✅ Aba em segundo plano (visibility change)
- ✅ Conexão offline/online

### Ferramentas Recomendadas
1. **DevTools Network Tab** - Monitore requisições
2. **DevTools Application Tab** - Verifique storage
3. **Lighthouse** - Audite performance
4. **AdBlock Test** - Verifique compatibilidade

## 🤝 Contribuindo

### Reportando Issues
1. **Contexto** - Navegador, URL, passos para reproduzir
2. **Console Output** - Erros e logs relevantes
3. **Network Tab** - Requisições falhadas
4. **Expected vs Actual** - Comportamento esperado vs real

### Melhorias Planejadas
- [ ] Eventos customizados (`trackEvent()`)
- [ ] Fila de eventos offline
- [ ] Integração com Google Analytics
- [ ] Dashboard em tempo real
- [ ] Webhooks para notificações

## 📄 Licença

MIT License - Veja [LICENSE](LICENSE) para detalhes.

## 🌐 Suporte

### Documentação Online
- **Repositório**: [github.com/vepo/visita](https://github.com/vepo/visita)
- **Demo**: [visita.vepo.dev](https://visita.vepo.dev)

### Canais de Ajuda
1. **Issues do GitHub** - Para bugs e feature requests
2. **Stack Overflow** - Tag `visita-analytics`
3. **Email** - suporte@vepo.dev

---

**Desenvolvido com foco em performance, privacidade e simplicidade.**  
**Uma única linha de código para analytics profissionais.** 🚀

*Última atualização: Janeiro 2024*  
*Versão compatível: Chrome 60+, Firefox 55+, Safari 11+, Edge 79+*