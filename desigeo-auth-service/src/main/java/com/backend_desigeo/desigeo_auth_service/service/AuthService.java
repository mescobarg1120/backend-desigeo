package com.backend_desigeo.desigeo_auth_service.service;

import com.backend_desigeo.desigeo_auth_service.dto.LoginRequest;
import com.backend_desigeo.desigeo_auth_service.dto.LoginResponse;
import com.backend_desigeo.desigeo_auth_service.entity.User;
import com.backend_desigeo.desigeo_auth_service.exception.AccountLockedException;
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

    static final int MAX_FAILED_ATTEMPTS = 5;
    static final int LOCK_DURATION_MINUTES = 15;

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

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + request.getEmail()));

        if (!user.isActive()) {
            throw new InvalidCredentialsException("User account is not active");
        }

        // Verificar si la cuenta está bloqueada
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            throw new InvalidCredentialsException("User account is temporarily locked");
        }

        // Verificar contraseña — si falla, incrementar contador y posiblemente bloquear
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            int attempts = (user.getFailedLoginCount() == null ? 0 : user.getFailedLoginCount()) + 1;
            user.setFailedLoginCount(attempts);

            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setLockedUntil(Instant.now().plusSeconds(LOCK_DURATION_MINUTES * 60L));
                userRepository.save(user);
                throw new AccountLockedException(
                        "Account locked after " + MAX_FAILED_ATTEMPTS +
                        " failed attempts. Try again in " + LOCK_DURATION_MINUTES + " minutes.");
            }

            userRepository.save(user);
            throw new InvalidCredentialsException("Invalid credentials");
        }

        // Login exitoso — resetear contador y actualizar lastLoginAt
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new LoginResponse(token, UserMapper.toDTO(user));
    }
}
