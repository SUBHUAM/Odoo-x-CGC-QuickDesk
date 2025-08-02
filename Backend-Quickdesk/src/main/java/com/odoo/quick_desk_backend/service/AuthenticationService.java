package com.odoo.quick_desk_backend.service;

import com.odoo.quick_desk_backend.dto.request.LoginDto;
import com.odoo.quick_desk_backend.dto.response.UserResponseDto;
import com.odoo.quick_desk_backend.entity.User;
import com.odoo.quick_desk_backend.repository.UserRepository;
import com.odoo.quick_desk_backend.utility.InputSanitizer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final InputSanitizer inputSanitizer;

    @Autowired
    public AuthenticationService(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 InputSanitizer inputSanitizer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.inputSanitizer = inputSanitizer;
    }

    public UserResponseDto authenticateUser(LoginDto loginDto, HttpServletRequest request) {
        String username = inputSanitizer.sanitize(loginDto.getUsername());

        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            throw new RuntimeException("Invalid credentials");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Create authentication token
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );

        // Set security context
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Store in session
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
        session.setAttribute("USER_ID", user.getUserId());
        session.setAttribute("USERNAME", user.getUsername());
        session.setAttribute("ROLE", user.getRole().name());

        logger.info("User authenticated and session created: {}", username);

        return convertToUserResponseDto(user);
    }

    public void logoutUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String username = (String) session.getAttribute("USERNAME");
            session.invalidate();
            SecurityContextHolder.clearContext();
            logger.info("User logged out: {}", username);
        }
    }

    private UserResponseDto convertToUserResponseDto(User user) {
        return new UserResponseDto(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getUsername(),
                user.getCreateTime(),
                user.getUpdateTime(),
                user.getRole()
        );

    }
}
