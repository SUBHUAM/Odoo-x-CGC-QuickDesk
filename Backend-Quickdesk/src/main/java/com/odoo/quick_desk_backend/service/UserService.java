package com.odoo.quick_desk_backend.service;

import com.odoo.quick_desk_backend.dto.request.LoginDto;
import com.odoo.quick_desk_backend.dto.request.RegistrationDto;
import com.odoo.quick_desk_backend.dto.response.UserResponseDto;
import com.odoo.quick_desk_backend.entity.User;
import com.odoo.quick_desk_backend.repository.UserRepository;
import com.odoo.quick_desk_backend.utility.InputSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final InputSanitizer inputSanitizer;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, InputSanitizer inputSanitizer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.inputSanitizer = inputSanitizer;
    }

    public UserResponseDto registerUser(RegistrationDto registrationDto) {
        // Basic sanitization
        String name = inputSanitizer.sanitize(registrationDto.getName());
        String email = registrationDto.getEmail();
        String username = inputSanitizer.sanitize(registrationDto.getUsername());

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

        User savedUser = userRepository.save(user);
        logger.info("User registered: {}", username);

        return convertToUserResponseDto(savedUser);
    }

    public UserResponseDto loginUser(LoginDto loginDto) {
        String username = inputSanitizer.sanitize(loginDto.getUsername());

        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            throw new RuntimeException("Invalid credentials");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        logger.info("User logged in: {}", username);
        return convertToUserResponseDto(user);
    }

    public UserResponseDto getUserByUsername(String username) {
        String sanitizedUsername = inputSanitizer.sanitize(username);
        Optional<User> userOptional = userRepository.findByUsername(sanitizedUsername);

        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        return convertToUserResponseDto(userOptional.get());
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
