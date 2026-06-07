package com.backend_desigeo.desigeo_auth_service.service;

import com.backend_desigeo.desigeo_auth_service.dto.ForgotPasswordRequest;
import com.backend_desigeo.desigeo_auth_service.dto.VerifyResetCodeRequest;
import com.backend_desigeo.desigeo_auth_service.entity.PasswordReset;
import com.backend_desigeo.desigeo_auth_service.entity.User;
import com.backend_desigeo.desigeo_auth_service.exception.InvalidCredentialsException;
import com.backend_desigeo.desigeo_auth_service.repository.PasswordResetRepository;
import com.backend_desigeo.desigeo_auth_service.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("CP-AUTH-004, CP-AUTH-005, CP-AUTH-006: Password Recovery - PasswordResetService")
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetRepository passwordResetRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordResetService = new PasswordResetService(
                userRepository, passwordResetRepository, passwordEncoder, emailService
        );
    }

    @Test
    @DisplayName("CP-AUTH-004: Iniciar recuperación contraseña con email válido → 200")
    void testForgotPasswordWithValidEmail() {
        // Arrange
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setFullName("Test User");

        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_code");

        // Act
        assertDoesNotThrow(() -> {
            passwordResetService.forgotPassword(request);
        });

        // Assert
        verify(passwordResetRepository).save(any(PasswordReset.class));
        verify(emailService).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("CP-AUTH-004: Iniciar recuperación con email no registrado → 200 (sin revelar)")
    void testForgotPasswordWithNonExistentEmail() {
        // Arrange
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("nonexistent@example.com");

        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        // Act & Assert - No debe lanzar excepción, por seguridad
        assertDoesNotThrow(() -> {
            passwordResetService.forgotPassword(request);
        });
    }

    @Test
    @DisplayName("CP-AUTH-005: Validar código de recuperación correcto → 200 + token")
    void testVerifyResetCodeWithValidCode() {
        // Arrange
        VerifyResetCodeRequest request = new VerifyResetCodeRequest();
        request.setEmail("test@example.com");
        request.setCode("123456");

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("test@example.com");

        PasswordReset passwordReset = new PasswordReset();
        passwordReset.setUserId(user.getUserId());
        passwordReset.setCodeHash("encoded_code");
        passwordReset.setToken("reset_token");
        passwordReset.setExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES));
        passwordReset.setAttempts(0);
        passwordReset.setUsed(false);

        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(user));
        when(passwordResetRepository.findLastActiveByUserId(any(UUID.class), any(Instant.class)))
                .thenReturn(Optional.of(passwordReset));
        when(passwordEncoder.matches("123456", "encoded_code")).thenReturn(true);

        // Act
        Map<String, Object> result = passwordResetService.verifyResetCode(request);

        // Assert
        assertNotNull(result);
        assertTrue((Boolean) result.get("ok"));
        assertEquals("reset_token", result.get("token"));
    }

    @Test
    @DisplayName("CP-AUTH-005: Validar código de recuperación incorrecto → 401 Unauthorized")
    void testVerifyResetCodeWithInvalidCode() {
        // Arrange
        VerifyResetCodeRequest request = new VerifyResetCodeRequest();
        request.setEmail("test@example.com");
        request.setCode("wrong_code");

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("test@example.com");

        PasswordReset passwordReset = new PasswordReset();
        passwordReset.setUserId(user.getUserId());
        passwordReset.setCodeHash("encoded_correct_code");
        passwordReset.setAttempts(0);
        passwordReset.setExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES));
        passwordReset.setUsed(false);

        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(user));
        when(passwordResetRepository.findLastActiveByUserId(any(UUID.class), any(Instant.class)))
                .thenReturn(Optional.of(passwordReset));
        when(passwordEncoder.matches("wrong_code", "encoded_correct_code")).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> {
            passwordResetService.verifyResetCode(request);
        });

        // Verificar que se incrementó el contador de intentos
        verify(passwordResetRepository).save(any(PasswordReset.class));
    }

    @Test
    @DisplayName("CP-AUTH-006: Rechazar código fallido después de 5 intentos → 429 Too Many Requests")
    void testRejectCodeAfterFiveAttempts() {
        // Arrange
        VerifyResetCodeRequest request = new VerifyResetCodeRequest();
        request.setEmail("test@example.com");
        request.setCode("wrong_code");

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("test@example.com");

        PasswordReset passwordReset = new PasswordReset();
        passwordReset.setUserId(user.getUserId());
        passwordReset.setCodeHash("encoded_correct_code");
        passwordReset.setAttempts(5); // Ya alcanzó el máximo
        passwordReset.setExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES));
        passwordReset.setUsed(false);

        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(user));
        when(passwordResetRepository.findLastActiveByUserId(any(UUID.class), any(Instant.class)))
                .thenReturn(Optional.of(passwordReset));

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> {
            passwordResetService.verifyResetCode(request);
        });

        assertTrue(exception.getMessage().contains("Too many attempts"));
    }

    @Test
    @DisplayName("CP-AUTH-006: Incrementar intento fallido y mostrar mensaje después de 5 intentos")
    void testIncrementFailedAttemptsAndRejectAfterMax() {
        // Arrange
        VerifyResetCodeRequest request = new VerifyResetCodeRequest();
        request.setEmail("test@example.com");
        request.setCode("wrong_code");

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("test@example.com");

        PasswordReset passwordReset = new PasswordReset();
        passwordReset.setUserId(user.getUserId());
        passwordReset.setCodeHash("encoded_correct_code");
        passwordReset.setAttempts(4); // Cuarto intento fallido
        passwordReset.setExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES));
        passwordReset.setUsed(false);

        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(user));
        when(passwordResetRepository.findLastActiveByUserId(any(UUID.class), any(Instant.class)))
                .thenReturn(Optional.of(passwordReset));
        when(passwordEncoder.matches("wrong_code", "encoded_correct_code")).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> {
            passwordResetService.verifyResetCode(request);
        });

        // Verificar que se guardó con intento incrementado
        ArgumentCaptor<PasswordReset> captor = ArgumentCaptor.forClass(PasswordReset.class);
        verify(passwordResetRepository).save(captor.capture());
        assertEquals(5, captor.getValue().getAttempts());
    }
}
