class AuthService {
  constructor() {
    this.baseUrl = "https://0413f8c3b52a.ngrok-free.app";
    this.csrfKey = "csrfToken"; // session key for CSRF token
    this.userKey = "currentUser"; // session key for user
    this.csrfToken = this.getSessionCsrfToken(); // Initialize from session if available
  }

  // --- CSRF Token ---
  async getCsrfToken() {
    // If already stored in session, return it
    if (this.csrfToken) return this.csrfToken;
    try {
      const response = await fetch(`${this.baseUrl}/api/auth/csrf-token`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          "ngrok-skip-browser-warning": "true",
        },
        credentials: "include",
      });
      if (!response.ok) throw new Error("Failed to get CSRF token");
      const data = await response.json();

      this.csrfToken = data.token;
      this.setSessionCsrfToken(this.csrfToken); // Save to session
      return this.csrfToken;
    } catch (error) {
      throw error;
    }
  }

  // --- Register ---
  async register(userData) {
    await this.getCsrfToken();
    const response = await fetch(`${this.baseUrl}/api/auth/register`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "ngrok-skip-browser-warning": "true",
        "X-CSRF-TOKEN": this.csrfToken,
      },
      credentials: "include",
      body: JSON.stringify(userData),
    });
    const result = await response.json();

    if (!response.ok) throw new Error(result.error || "Registration failed");

    // Save user to session
    this.setSessionUser(result.user);
    return result;
  }

  // --- Login ---
  async login(credentials) {
    await this.getCsrfToken();
    const response = await fetch(`${this.baseUrl}/api/auth/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "ngrok-skip-browser-warning": "true",
        "X-CSRF-TOKEN": this.csrfToken,
      },
      credentials: "include",
      body: JSON.stringify(credentials),
    });
    const result = await response.json();
    if (!response.ok) throw new Error(result.error || "Login failed");

    // Save user to session
    this.setSessionUser(result.user);
    return result;
  }

  // --- Get Current User (session + API check) ---
  async getCurrentUser() {
    // Only fetch from sessionStorage
    const sessionUser = this.getSessionUser();
    if (!sessionUser) {
      throw new Error("User not found in session");
    }

    return sessionUser;
  }

  // --- Logout ---
  async logout() {
    // Clear session
    this.clearSessionUser();
    this.clearSessionCsrfToken();
    this.csrfToken = null;
    return ;
  }

  // --- Get Tickets ---
  async getTickets() {
    await this.getCsrfToken(); // Ensure token exists
    const response = await fetch(`${this.baseUrl}/api/tickets/my`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "ngrok-skip-browser-warning": "true",
        "X-CSRF-TOKEN": this.csrfToken,
      },
      credentials: "omit",
    });
    const result = await response.json();

    if (!response.ok) throw new Error(result.error || "No Tickets Found");

    return result;
  }
  // --- Add Tickets ---
  async addTicket(ticket) {
    await this.getCsrfToken(); // Ensure token exists
    const response = await fetch(`${this.baseUrl}/api/tickets/save`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "ngrok-skip-browser-warning": "true",
        "X-CSRF-TOKEN": this.csrfToken,
      },
      credentials: "omit",
      body: JSON.stringify(ticket),
    });
    const result = await response.json();

    if (!response.ok) throw new Error(result.error || "Error");

    return result;
  }
 // --- Update Ticket Status ---
  async updateTicketStatus(ticket) {
    await this.getCsrfToken(); // Ensure token exists
    const response = await fetch(`${this.baseUrl}/api/tickets/${ticket.ticketId}/status`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        "ngrok-skip-browser-warning": "true",
        "X-CSRF-TOKEN": this.csrfToken,
      },
      credentials: "omit",
      body: JSON.stringify(ticket),
    });
    const result = await response.json();

    if (!response.ok) throw new Error(result.error || "Error");

    return result;
  }
  // --- Session Management for User ---
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

  // --- Session Management for CSRF Token ---
  setSessionCsrfToken(token) {
    if (typeof window !== "undefined") {
      sessionStorage.setItem(this.csrfKey, token);
    }
  }

  getSessionCsrfToken() {
    if (typeof window !== "undefined") {
      return sessionStorage.getItem(this.csrfKey);
    }
    return null;
  }

  clearSessionCsrfToken() {
    if (typeof window !== "undefined") {
      sessionStorage.removeItem(this.csrfKey);
    }
  }
}

export default new AuthService();
