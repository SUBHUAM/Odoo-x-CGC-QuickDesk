package com.odoo.quick_desk_backend.utility;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SessionValidator {

    public boolean isUserAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
                authentication.isAuthenticated() &&
                !authentication.getName().equals("anonymousUser");
    }

    /**
     * Get current authenticated user's ID from session
     */
    public Long getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (Long) session.getAttribute("USER_ID");
        }
        return null;
    }

    /**
     * Get current authenticated username from session
     */
    public String getCurrentUsername(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (String) session.getAttribute("USERNAME");
        }
        return null;
    }

    /**
     * Get current user's role from session
     */
    public String getCurrentUserRole(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (String) session.getAttribute("ROLE");
        }
        return null;
    }


    public boolean canAccessResource(HttpServletRequest request, Long resourceOwnerId) {
        if (!isUserAuthenticated()) {
            return false;
        }

        Long currentUserId = getCurrentUserId(request);
        String currentRole = getCurrentUserRole(request);

        if ("ADMIN".equals(currentRole)) {
            return true;
        }

        return currentUserId != null && currentUserId.equals(resourceOwnerId);
    }
}
