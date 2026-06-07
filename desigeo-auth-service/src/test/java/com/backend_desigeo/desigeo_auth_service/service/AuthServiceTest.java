package com.backend_desigeo.desigeo_auth_service.service;

import com.backend_desigeo.desigeo_auth_service.dto.LoginRequest;
import com.backend_desigeo.desigeo_auth_service.dto.LoginResponse;
import com.backend_desigeo.desigeo_auth_service.entity.User;
import com.backend_desigeo.desigeo_auth_service.exception.InvalidCredentialsException;
import com.backend_desigeo.desigeo_auth_service.exception.UserNotFoundException;
import com.backend_desigeo.desigeo_auth_service.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@DisplayName("CP-AUTH-003: Login y CP-AUTH-006: Rechazar código fallido - AuthService")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    @DisplayName("CP-AUTH-003: Login con credenciales válidas → 200 + JWT + Refresh")
    void testSuccessfulLogin() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("SecurePass123!");

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed_password");
        user.setFullName("Test User");
        user.setActive(true);
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);

        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt_token_here");

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("jwt_token_here", response.getToken());
        assertNotNull(response.getUser());
        assertEquals("test@example.com", response.getUser().getEmail());
    }

    @Test
    @DisplayName("CP-AUTH-003: Login con credenciales inválidas → 401 Unauthorized")
    void testLoginWithInvalidPassword() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("WrongPassword123!");

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed_password");
        user.setActive(true);
        user.setLockedUntil(null);

        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(request);
        });
    }

    @Test
    @DisplayName("CP-AUTH-003: Login con usuario no activo → 401 Unauthorized")
    void testLoginWithInactiveUser() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("SecurePass123!");

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed_password");
        user.setActive(false);
        user.setLockedUntil(null);

        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(request);
        });
    }

    @Test
    @DisplayName("CP-AUTH-003: Login con usuario bloqueado → 401 Unauthorized")
    void testLoginWithLockedUser() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("SecurePass123!");

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed_password");
        user.setActive(true);
        user.setLockedUntil(Instant.now().plusSeconds(3600)); // Bloqueado 1 hora

        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(request);
        });
    }
}
