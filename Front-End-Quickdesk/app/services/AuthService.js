class AuthService {
    constructor() {
        this.baseUrl = 'https://0413f8c3b52a.ngrok-free.app';
        this.csrfToken = null;
        this.userKey = 'currentUser'; // session key name
    }

    // --- CSRF Token ---
    async getCsrfToken() {
        try {
            const response = await fetch(`${this.baseUrl}/api/auth/csrf-token`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'ngrok-skip-browser-warning': 'true'
                },
                credentials: 'include'
            });
            if (!response.ok) throw new Error('Failed to get CSRF token');
            const data = await response.json();
            this.csrfToken = data.token;
            return this.csrfToken;
        } catch (error) {
            throw error;
        }
    }

    // --- Register ---
    async register(userData) {
        await this.getCsrfToken();
        const response = await fetch(`${this.baseUrl}/api/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': this.csrfToken
            },
            credentials: 'include',
            body: JSON.stringify(userData)
        });
        const result = await response.json();

        if (!response.ok) throw new Error(result.error || 'Registration failed');

        // Save user to session
        this.setSessionUser(result.user);
        return result;
    }

    // --- Login ---
    async login(credentials) {
        await this.getCsrfToken();
        const response = await fetch(`${this.baseUrl}/api/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': this.csrfToken
            },
            credentials: 'include',
            body: JSON.stringify(credentials)
        });
        const result = await response.json();
        if (!response.ok) throw new Error(result.error || 'Login failed');

        // Save user to session
        this.setSessionUser(result.user);
        return result;
    }

    // --- Get Current User (session + API check) ---
    async getCurrentUser() {
        // 1. Try getting user from sessionStorage
        const sessionUser = this.getSessionUser();
        if (sessionUser) return sessionUser;

        // 2. Fallback to API call
        const response = await fetch(`${this.baseUrl}/api/auth/current-user`, {
            method: 'GET',
            credentials: 'include'
        });
        if (!response.ok) {
            if (response.status === 401) throw new Error('Not authenticated');
            throw new Error('Failed to get user data');
        }
        const user = await response.json();

        // Save to session for faster next time
        this.setSessionUser(user);
        return user;
    }

    // --- Logout ---
    async logout() {
        await this.getCsrfToken();
        const response = await fetch(`${this.baseUrl}/api/auth/logout`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': this.csrfToken
            },
            credentials: 'include'
        });
        if (!response.ok) throw new Error('Logout failed');

        // Clear session
        this.clearSessionUser();
        this.csrfToken = null;
        return await response.json();
    }

    // --- Session Management ---
    setSessionUser(user) {
        if (typeof window !== "undefined") {
            sessionStorage.setItem(this.userKey, JSON.stringify(user));
        }
    }

    getSessionUser() {
        if (typeof window !== "undefined") {
            const data = sessionStorage.getItem(this.userKey);
            return data ? JSON.parse(data) : null;
        }
        return null;
    }

    clearSessionUser() {
        if (typeof window !== "undefined") {
            sessionStorage.removeItem(this.userKey);
        }
    }
}

export default new AuthService();
