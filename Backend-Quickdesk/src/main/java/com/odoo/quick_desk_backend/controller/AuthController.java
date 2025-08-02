package com.odoo.quick_desk_backend.controller;

import com.odoo.quick_desk_backend.dto.request.LoginDto;
import com.odoo.quick_desk_backend.dto.request.RegistrationDto;
import com.odoo.quick_desk_backend.dto.response.UserResponseDto;
import com.odoo.quick_desk_backend.service.AuthenticationService;
import com.odoo.quick_desk_backend.service.UserService;
import com.odoo.quick_desk_backend.utility.SessionValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final SessionValidator sessionValidator;

    @Autowired
    public AuthController(UserService userService,
                          AuthenticationService authenticationService,
                          SessionValidator sessionValidator) {
        this.userService = userService;
        this.authenticationService = authenticationService;
        this.sessionValidator = sessionValidator;
    }

    @GetMapping("/csrf-token")
    public ResponseEntity<?> getCsrfToken(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        Map<String, String> response = new HashMap<>();
        if (csrfToken != null) {
            response.put("token", csrfToken.getToken());
            response.put("headerName", csrfToken.getHeaderName());
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationDto registrationDto) {
        try {
            UserResponseDto userResponse = userService.registerUser(registrationDto);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("user", userResponse);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginDto loginDto,
                                       HttpServletRequest request) {
        try {
            UserResponseDto userResponse = authenticationService.authenticateUser(loginDto, request);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("user", userResponse);
            response.put("sessionId", request.getSession().getId());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        try {
            authenticationService.logoutUser(request);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Logout successful");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Logout failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        if (!sessionValidator.isUserAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

        try {
            String username = sessionValidator.getCurrentUsername(request);
            UserResponseDto userResponse = userService.getUserByUsername(username);
            return ResponseEntity.ok(userResponse);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}
