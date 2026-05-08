package com.backend_desigeo.desigeo_auth_service.service;

import com.backend_desigeo.desigeo_auth_service.dto.LoginRequest;
import com.backend_desigeo.desigeo_auth_service.dto.LoginResponse;
import com.backend_desigeo.desigeo_auth_service.entity.User;
import com.backend_desigeo.desigeo_auth_service.exception.InvalidCredentialsException;
import com.backend_desigeo.desigeo_auth_service.exception.UserNotFoundException;
import com.backend_desigeo.desigeo_auth_service.mapper.UserMapper;
import com.backend_desigeo.desigeo_auth_service.repository.UserRepository;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + request.getEmail()));

        if (!user.isActive()) {
            throw new InvalidCredentialsException("User account is not active");
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            throw new InvalidCredentialsException("User account is temporarily locked");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String token = jwtService.generateToken(user);
        return new LoginResponse(token, UserMapper.toDTO(user));
    }
}
