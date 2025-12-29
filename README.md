# 🔍 Visita Analytics - Sistema de Rastreamento de Visitas

![JavaScript](https://img.shields.io/badge/JavaScript-ES6+-yellow.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Browser](https://img.shields.io/badge/Browser-Compatible-green.svg)

Um sistema de rastreamento de visitas em tempo real para websites e blogs, desenvolvido em JavaScript puro. Rastreie visitantes, tempo de sessão, páginas visitadas e muito mais!

## ✨ Features Principais

- **🎯 Rastreamento por Usuário Único** - Identificação única por dispositivo/navegador
- **🔗 Sessões por Aba** - Suporte a múltiplas abas simultâneas
- **📊 Coleta de Dados** - Informações detalhadas de navegador, localização e comportamento
- **🔄 Suporte a SPAs** - Monitoramento automático de mudanças de rota
- **⚡ Performance** - Uso de Web APIs modernas (sendBeacon, sessionStorage)
- **🔧 Debug Fácil** - API exposta para desenvolvimento e testes

## 🚀 Como Usar

### 1. Inclusão no Site

```html
<!-- Coloque este código antes do </body> -->
<script src="visita-analytics.js"></script>
```

### 2. Configuração Básica

```javascript
// O script se auto-inicializa, mas você pode customizar:
window.VisitaTracker.forceNewVisita(); // Forçar nova visita
```

### 3. Integração com Backend

Configure seu backend para receber os dados:

```javascript
// Endpoints esperados:
POST /api/visita/acesso    // Registra novo acesso
POST /api/visita/saida     // Registra saída
POST /api/visita/pagina    // Registra mudança de página (SPAs)
```

## 📋 Dados Coletados

| Categoria | Dados Coletados | Exemplo |
|-----------|-----------------|---------|
| **Usuário** | ID único, Tab ID | `user-123`, `tab-456` |
| **Navegador** | User Agent, Idioma, Timezone | `Chrome/120`, `pt-BR`, `America/Sao_Paulo` |
| **Dispositivo** | Resolução de tela | `1920x1080` |
| **Navegação** | URL atual, Referer | `https://site.com/blog`, `Google` |
| **Tempo** | Timestamps, Duração | `2024-01-15T10:30:00Z` |

## 🏗️ Arquitetura

### Estrutura de Dados

```javascript
{
  "userId": "uuid-unico-do-usuario",
  "tabId": "id-unico-da-aba",
  "visitaId": "id-da-visita-atual",
  "pagina": "url-atual",
  "referer": "origem-do-acesso",
  "userAgent": "info-do-navegador",
  "screenResolution": "resolucao",
  "language": "idioma",
  "timezone": "fuso-horario",
  "timestamp": "data-hora-iso"
}
```

### Fluxo de Funcionamento

```
1. Carregamento da Página
   ↓
2. Verifica usuário existente ou cria novo
   ↓
3. Cria/recupera ID da aba atual
   ↓
4. Verifica visita ativa válida
   ↓
5. Se nova visita → Envia dados para backend
   ↓
6. Monitora atividade do usuário
   ↓
7. Registra saída quando usuário sai
```

## 🛠️ API do Script

### Métodos Disponíveis

```javascript
// Acessar dados atuais
window.VisitaTracker.getVisitaId();    // → "visita-123"
window.VisitaTracker.getTabId();       // → "tab-456"
window.VisitaTracker.getUserId();      // → "user-789"

// Controle manual
window.VisitaTracker.forceNewVisita(); // Força nova visita
window.VisitaTracker.registrarSaida(); // Registra saída manual
window.VisitaTracker.clearData();      // Limpa dados locais

// Debug no console
console.log(window.VisitaTracker);
```

### Eventos Monitorados

- **DOMContentLoaded** - Inicialização
- **beforeunload** - Saída da página
- **visibilitychange** - Aba oculta/visível
- **Activity Events** - Cliques, scroll, teclas
- **MutationObserver** - Mudanças em SPAs

## 🔧 Configuração Avançada

### Personalizando o Endpoint

```javascript
// Antes de incluir o script principal
window.VISITA_CONFIG = {
  API_URL: 'https://seu-backend.com/api/visita',
  TEMPO_VALIDADE: 1800000, // 30 minutos em ms
  LOG_LEVEL: 'debug'       // 'none', 'error', 'info', 'debug'
};
```

### Integração com Frameworks

**React/Vue/Angular:**
```javascript
// Em seu componente principal
useEffect(() => {
  // O script já monitora SPAs automaticamente
  // Para ações customizadas:
  window.VisitaTracker.coletarInformacoes();
}, [location]);
```

## 📊 Análise de Dados

### Exemplo de Dashboard

```sql
-- Consultas úteis para seu banco de dados:
SELECT COUNT(*) as total_visitas FROM visitas;
SELECT AVG(duracao) as tempo_medio FROM visitas;
SELECT pagina, COUNT(*) as acessos FROM visitas GROUP BY pagina;
SELECT referer, COUNT(*) as origem FROM visitas GROUP BY referer;
```

### Métricas Importantes

- **Taxa de Rejeição** - Visitas com uma página apenas
- **Tempo Médio** - Duração média das sessões
- **Páginas/Sessão** - Engajamento dos usuários
- **Origens** - De onde vêm os visitantes

## 🚨 Considerações de Privacidade

### GDPR e LGPD

1. **Transparência** - Informe os usuários sobre o rastreamento
2. **Consentimento** - Implemente opt-in quando necessário
3. **Anonimização** - Considere hashear IDs sensíveis
4. **Retenção** - Defina política de retenção de dados

### Implementação de Cookie Banner

```html
<div id="cookie-banner">
  Usamos cookies para melhorar sua experiência. 
  <button id="accept-cookies">Aceitar</button>
  <button id="reject-cookies">Rejeitar</button>
</div>

<script>
  document.getElementById('accept-cookies').addEventListener('click', () => {
    localStorage.setItem('cookies-accepted', 'true');
    window.VisitaTracker.coletarInformacoes();
  });
</script>
```

## 🧪 Testes e Debug

### Modo Debug

```javascript
// Ative logs detalhados
localStorage.setItem('visita-debug', 'true');

// Verifique no console:
// - IDs gerados
// - Requisições enviadas
// - Eventos capturados
```

### Ferramentas de Desenvolvimento

1. **Network Tab** - Monitore requisições para `/api/visita`
2. **Application Tab** - Veja localStorage/sessionStorage
3. **Console** - Use `window.VisitaTracker` para debug

## 📈 Melhorias Futuras

### Roadmap Planejado

- [ ] **Eventos Customizados** - `trackEvent('compra', {valor: 100})`
- [ ] **Heatmaps** - Rastreamento de cliques e scroll
- [ ] **AB Testing** - Suporte a experimentos
- [ ] **Offline Support** - Fila de eventos offline
- [ ] **Dashboard Integrado** - Visualização em tempo real

### Contribuindo

1. Fork o projeto
2. Crie uma branch (`git checkout -b feature/nova-feature`)
3. Commit suas mudanças (`git commit -am 'Add nova feature'`)
4. Push para a branch (`git push origin feature/nova-feature`)
5. Crie um Pull Request

## ⚠️ Limitações Conhecidas

1. **Bloqueadores** - Adblockers podem impedir o rastreamento
2. **Incognito** - Dados podem ser perdidos em modo privado
3. **CORS** - Configure headers adequados no backend
4. **JavaScript** - Requer JS habilitado no navegador

## 📄 Licença

MIT License - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 🤝 Suporte

**Problemas Comuns:**
1. **Dados não aparecem?** Verifique console por erros CORS
2. **Visitas duplicadas?** Verifique timeout de sessão
3. **SPA não funciona?** Ative MutationObserver

**Canais de Ajuda:**
- 📖 [Documentação Completa](docs/)
- 🐛 [Reportar Bugs](issues/)
- 💡 [Sugerir Features](issues/)

---

Desenvolvido com ❤️ para a comunidade de desenvolvedores. 

**Estatísticas em tempo real, código open-source.** 🚀