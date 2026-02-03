/**
 * Visita Analytics - Professional Tracking Implementation
 * @version 2.0.0
 * @description Advanced user session tracking with cross-tab support
 */

class VisitaAnalytics {
    constructor() {
        this.API_ENDPOINTS = {
            ACCESS: '/access',
            EXIT: '/exit',
            PAGE_VIEW: '/view',
            PING: '/ping'
        };
        
        this.config = {
            SESSION_TIMEOUT: 30 * 60 * 1000, // 30 minutes
            INACTIVITY_THRESHOLD: 5 * 60 * 1000, // 5 minutes
            ACTIVITY_CHECK_INTERVAL: 5 * 1000, // 5 seconds
            RETRY_ATTEMPTS: 3,
            RETRY_DELAY: 1000
        };
        
        this.state = {
            isInitialized: false,
            isUnloading: false,
            lastActivity: Date.now(),
            retryCount: 0
        };
        
        this.identifiers = {
            userId: null,
            tabId: null,
            visitaId: null
        };

        const script = document.currentScript;

        if (script) {
            const token = script.getAttribute('data-token');

            // Validate required configuration
            if (!token) {
                console.error(
                    'Visita: Missing required configuration.\n' +
                    'Please provide data-token attribute:\n\n' +
                    '<script \n' +
                    '  async \n' +
                    '  src="https://visita.vepo.dev/visita.js"\n' +
                    '  data-token="your-auth-token-here">\n' +
                    '</script>\n\n' +
                    'Current configuration:\n' +
                    `- Token: Not provided`
                );

                this.credentials = {
                    domain: window.location.hostname,
                    token: null
                };
            } else {
                this.credentials = {
                    domain: window.location.hostname,
                    token: token
                };

                console.debug('Visita: Configuration loaded successfully');
            }
        } else {
            console.error(
                'Visita: Script element not found.\n' +
                'Ensure the script is loaded directly and not through dynamic injection.\n\n' +
                'Proper installation:\n\n' +
                '<script \n' +
                '  async \n' +
                '  src="https://visita.vepo.dev/visita.js"\n' +
                '  data-token="your-auth-token-here">\n' +
                '</script>'
            );

            this.credentials = {
                domain: window.location.host,
                token: null
            };
        }
        this.init();
    }

    /**
     * Generate UUID v4 compliant identifier
     * @returns {string} UUID v4
     */
    static generateUUID() {
        try {
            return crypto.randomUUID?.() || 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
                const r = Math.random() * 16 | 0;
                const v = c === 'x' ? r : (r & 0x3 | 0x8);
                return v.toString(16);
            });
        } catch (error) {
            console.warn('UUID generation fallback triggered:', error);
            return `fallback-${Date.now()}-${Math.random().toString(36).substring(2, 11)}`;
        }
    }

    /**
     * Generate unique tab identifier
     * @returns {string} Tab identifier
     */
    static generateTabId() {
        return `tab-${Date.now()}-${Math.random().toString(36).substring(2, 11)}`;
    }

    /**
     * Initialize tracking system
     */
    async init() {
        if (this.state.isInitialized) {
            console.warn('VisitaAnalytics already initialized');
            return;
        }

        try {
            this.loadIdentifiers();
            await this.setupEventListeners();
            await this.startSession();
            
            this.state.isInitialized = true;
            this.logSystemStatus();
        } catch (error) {
            console.error('Failed to initialize VisitaAnalytics:', error);
            this.handleInitializationError(error);
        }
    }

    /**
     * Load user and session identifiers
     */
    loadIdentifiers() {
        // Load or create user ID
        this.identifiers.userId = localStorage.getItem('visita-user-id');
        if (!this.identifiers.userId) {
            this.identifiers.userId = VisitaAnalytics.generateUUID();
            localStorage.setItem('visita-user-id', this.identifiers.userId);
        }

        // Load or create tab ID
        this.identifiers.tabId = sessionStorage.getItem('visita-tab-id');
        if (!this.identifiers.tabId) {
            this.identifiers.tabId = VisitaAnalytics.generateTabId();
            sessionStorage.setItem('visita-tab-id', this.identifiers.tabId);
        }
    }

    /**
     * Start new or resume existing session
     */
    async startSession() {
        const storedSession = this.loadStoredSession();
        
        if (storedSession && this.isSessionValid(storedSession)) {
            this.identifiers.visitaId = storedSession.visitaId;
            console.info(`Resuming existing session: ${this.identifiers.visitaId}`);
            return;
        }

        // Clear expired session data
        if (storedSession) {
            this.clearSessionData();
        }

        await this.createNewSession();
    }

    /**
     * Load stored session from sessionStorage
     * @returns {Object|null} Session data
     */
    loadStoredSession() {
        const sessionKey = `visita-session-${this.identifiers.tabId}`;
        const sessionData = sessionStorage.getItem(sessionKey);
        
        if (!sessionData) return null;

        try {
            return JSON.parse(sessionData);
        } catch (error) {
            console.error('Failed to parse session data:', error);
            return null;
        }
    }

    /**
     * Check if session is still valid
     * @param {Object} sessionData 
     * @returns {boolean}
     */
    isSessionValid(sessionData) {
        const now = Date.now();
        const sessionAge = now - sessionData.timestamp;
        return sessionAge < this.config.SESSION_TIMEOUT;
    }

    /**
     * Create new session on server
     */
    async createNewSession() {
        const sessionData = {
            userId: this.identifiers.userId,
            tabId: this.identifiers.tabId,
            page: window.location.href,
            referrer: document.referrer || 'direct',
            userAgent: navigator.userAgent,
            screenResolution: `${window.screen.width}x${window.screen.height}`,
            colorDepth: window.screen.colorDepth,
            language: navigator.language,
            timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
            timestamp: Date.now()
        };

        try {
            const response = await this.sendRequest(
                this.API_ENDPOINTS.ACCESS,
                sessionData,
                { method: 'POST', retry: true }
            );

            if (response?.id) {
                this.identifiers.visitaId = response.id;
                this.storeSessionData(response.id);
                console.info(`New session created: ${response.id}`);
            }
        } catch (error) {
            console.error('Failed to create session:', error);
            this.createFallbackSession();
        }
    }

    /**
     * Create fallback session when server is unavailable
     */
    createFallbackSession() {
        this.identifiers.visitaId = `local-${this.identifiers.tabId}-${Date.now()}`;
        this.storeSessionData(this.identifiers.visitaId);
        console.warn(`Created fallback session: ${this.identifiers.visitaId}`);
    }

    /**
     * Store session data in sessionStorage
     * @param {string} visitaId 
     */
    storeSessionData(visitaId) {
        const sessionKey = `visita-session-${this.identifiers.tabId}`;
        const sessionData = {
            visitaId: visitaId,
            timestamp: Date.now()
        };
        
        try {
            sessionStorage.setItem(sessionKey, JSON.stringify(sessionData));
        } catch (error) {
            console.error('Failed to store session data:', error);
        }
    }

    /**
     * Clear session data
     */
    clearSessionData() {
        const sessionKey = `visita-session-${this.identifiers.tabId}`;
        sessionStorage.removeItem(sessionKey);
        this.identifiers.visitaId = null;
    }

    /**
     * Send HTTP request with retry logic
     * @param {string} endpoint 
     * @param {Object} data 
     * @param {Object} options 
     * @returns {Promise}
     */
    async sendRequest(endpoint, data, options = {}) {
        const url = this.getApiUrl(endpoint);
        const config = {
            method: options.method || 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json',
                'VISITA-DOMAIN-HOSTNAME': this.credentials.domain,
                'VISITA-DOMAIN-TOKEN': this.credentials.token
            },
            body: JSON.stringify(data),
            credentials: 'omit',
            ...options
        };

        for (let attempt = 1; attempt <= this.config.RETRY_ATTEMPTS; attempt++) {
            try {
                if (options.keepalive && endpoint === this.API_ENDPOINTS.EXIT) {
                    return await this.sendBeaconRequest(url, data);
                }

                const response = await fetch(url, config);
                
                if (response.ok) {
                    if (options.no_body) {
                        return response;
                    } else {
                        return await response.json();
                    }
                }
                
                console.warn("Error", response.status, response.statusText);
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            } catch (error) {
                console.error("Error", error);
                if (attempt === this.config.RETRY_ATTEMPTS) {
                    throw error;
                }
                
                console.warn(`Request failed (attempt ${attempt}/${this.config.RETRY_ATTEMPTS}):`, error);
                await this.delay(this.config.RETRY_DELAY * attempt);
            }
        }
    }

    /**
     * Send beacon request for page exit events
     * @param {string} url 
     * @param {Object} data 
     * @returns {Promise}
     */
    async sendBeaconRequest(url, data) {
        return new Promise((resolve) => {
            const blob = new Blob([JSON.stringify(data)], { type: 'application/json' });
            const success = navigator.sendBeacon?.(url, blob);
            
            if (success) {
                resolve({ success: true });
            } else {
                // Fallback to fetch with keepalive
                fetch(url, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data),
                    keepalive: true
                }).then(() => resolve({ success: true }))
                  .catch(() => resolve({ success: false }));
            }
        });
    }

    /**
     * Get API URL with proper protocol
     * @param {string} endpoint 
     * @returns {string}
     */
    getApiUrl(endpoint) {
        const protocol = window.location.protocol === 'file:' ? 'http:' : window.location.protocol;
        return `${protocol}//${DEPLOY_DOMAIN}/api/tracking${endpoint}`;
    }

    /**
     * Delay helper function
     * @param {number} ms 
     * @returns {Promise}
     */
    delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    /**
     * Register page exit
     */
    async registerExit() {
        if (this.state.isUnloading || !this.identifiers.visitaId) return;
        
        this.state.isUnloading = true;
        console.info(`Registering exit for session: ${this.identifiers.visitaId}`);

        const exitData = {
            id: this.identifiers.visitaId,
            userId: this.identifiers.userId,
            tabId: this.identifiers.tabId,
            timestamp: Date.now()
        };

        try {
            await this.sendRequest(
                this.API_ENDPOINTS.EXIT,
                exitData,
                { keepalive: true }
            );
        } catch (error) {
            console.error('Failed to register exit:', error);
        } finally {
            this.clearSessionData();
        }
    }

    async sendPing() {
        if (!this.identifiers.visitaId) return;
        const pingData = { 
            id: this.identifiers.visitaId,
            timestamp: Date.now()
        };

        try {
            var response = await this.sendRequest(this.API_ENDPOINTS.PING, pingData , { no_body: true});
            console.debug("Ping sent!", response);
        } catch (error) {
            console.error('Failed to track page view:', error);
        }
    }

    /**
     * Track page view (for SPAs)
     * @param {string} url 
     */
    async trackPageView(url) {
        if (!this.identifiers.visitaId) return;

        const viewData = {
            id: this.identifiers.visitaId,
            userId: this.identifiers.userId,
            tabId: this.identifiers.tabId,
            timestamp: Date.now(),
            page: url
        };

        try {
            const response = await this.sendRequest(this.API_ENDPOINTS.PAGE_VIEW, viewData);

            if (response?.id && response?.id != this.identifiers.visitaId) {
                this.identifiers.visitaId = response.id;
                this.storeSessionData(response.id);
                console.info(`New session created: ${response.id}`);
            }
        } catch (error) {
            console.error('Failed to track page view:', error);
        }
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        // Page exit events
        window.addEventListener('beforeunload', () => this.registerExit());
        window.addEventListener('pagehide', () => this.registerExit());

        // Visibility change
        document.addEventListener('visibilitychange', () => this.handleVisibilityChange());

        // User activity monitoring
        this.setupActivityMonitoring();

        // SPA navigation tracking
        this.setupNavigationObserver();
    }

    /**
     * Handle visibility change events
     */
    handleVisibilityChange() {
        if (document.visibilityState === 'hidden') {
            this.registerExit();
        } else if (document.visibilityState === 'visible') {
            this.state.lastActivity = Date.now();
            this.startSession(); // Resume or create new session
        }
    }

    /**
     * Setup user activity monitoring
     */
    setupActivityMonitoring() {
        const activityEvents = ['mousedown', 'mousemove', 'keydown', 'scroll', 'touchstart', 'click'];
        
        activityEvents.forEach(event => {
            document.addEventListener(event, () => {
                this.state.lastActivity = Date.now();
            }, { passive: true, capture: true });
        });

        // Check for inactivity periodically
        setInterval(() => this.checkInactivity(), this.config.ACTIVITY_CHECK_INTERVAL);
    }

    /**
     * Check for user inactivity
     */
    checkInactivity() {
        const inactiveTime = Date.now() - this.state.lastActivity;
        
        if (inactiveTime > this.config.INACTIVITY_THRESHOLD && this.identifiers.visitaId) {
            console.info(`User inactive for ${Math.round(inactiveTime / 1000)} seconds`);
            // Could trigger custom inactivity event here
        }
        
        this.sendPing();
    }

    /**
     * Setup navigation observer for SPAs
     */
    setupNavigationObserver() {
        let lastUrl = window.location.href;
        
        const observer = new MutationObserver(() => {
            const currentUrl = window.location.href;
            if (currentUrl !== lastUrl) {
                console.info(`Page navigation: ${lastUrl} -> ${currentUrl}`);
                lastUrl = currentUrl;
                this.trackPageView(currentUrl);
            }
        });

        observer.observe(document, { 
            subtree: true, 
            childList: true,
            attributes: true,
            attributeFilter: ['href']
        });
    }

    /**
     * Handle initialization errors
     * @param {Error} error 
     */
    handleInitializationError(error) {
        // Could send error to error tracking service
        console.error('VisitaAnalytics initialization failed:', error);
        
        // Still create basic identifiers for fallback
        this.loadIdentifiers();
        this.createFallbackSession();
    }

    /**
     * Log system status
     */
    logSystemStatus() {
        console.info(`
            VisitaAnalytics Initialized
            ============================
            User ID: ${this.identifiers.userId}
            Tab ID: ${this.identifiers.tabId}
            Session ID: ${this.identifiers.visitaId}
            API Domain: ${window.DEPLOY_DOMAIN || 'Not set'}
            Status: ${this.state.isInitialized ? 'Active' : 'Failed'}
        `);
    }

    /**
     * Public API methods
     */
    getSessionId() {
        return this.identifiers.visitaId;
    }

    getTabId() {
        return this.identifiers.tabId;
    }

    getUserId() {
        return this.identifiers.userId;
    }

    forceNewSession() {
        this.clearSessionData();
        return this.createNewSession();
    }

    getSessionData() {
        return { ...this.identifiers, lastActivity: this.state.lastActivity };
    }
}

// Initialize tracking
if (typeof window !== 'undefined') {
    window.VisitaAnalytics = new VisitaAnalytics();
}