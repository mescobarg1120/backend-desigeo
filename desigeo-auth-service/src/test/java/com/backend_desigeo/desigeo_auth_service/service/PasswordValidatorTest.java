package com.backend_desigeo.desigeo_auth_service.service;

import com.backend_desigeo.desigeo_auth_service.exception.WeakPasswordException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordValidator - Validación de contraseñas seguras")
class PasswordValidatorTest {

    private final PasswordValidator passwordValidator = new PasswordValidator();

    @Test
    @DisplayName("Contraseña válida: P@ssw0rd → Aceptada")
    void testValidPassword() {
        // Act & Assert - No debe lanzar excepción
        assertDoesNotThrow(() -> {
            passwordValidator.validate("P@ssw0rd");
        });
    }

    @Test
    @DisplayName("Contraseña válida: SecurePass123! → Aceptada")
    void testValidPasswordWithSpecialChars() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            passwordValidator.validate("SecurePass123!");
        });
    }

    @Test
    @DisplayName("Contraseña débil: '123' (muy corta) → Rechazada")
    void testShortPassword() {
        // Act & Assert
        assertThrows(WeakPasswordException.class, () -> {
            passwordValidator.validate("123");
        });
    }

    @Test
    @DisplayName("Contraseña débil: 'password' (sin mayúscula, número, especial) → Rechazada")
    void testPasswordWithoutUppercaseAndNumbers() {
        // Act & Assert
        WeakPasswordException exception = assertThrows(WeakPasswordException.class, () -> {
            passwordValidator.validate("password");
        });
        assertTrue(exception.getMessage().contains("uppercase") || 
                   exception.getMessage().contains("digit") ||
                   exception.getMessage().contains("special"));
    }

    @Test
    @DisplayName("Contraseña débil: 'PASSWORD' (solo mayúsculas) → Rechazada")
    void testPasswordWithoutLowercaseAndNumbers() {
        // Act & Assert
        assertThrows(WeakPasswordException.class, () -> {
            passwordValidator.validate("PASSWORD");
        });
    }

    @Test
    @DisplayName("Contraseña débil: 'password123' (sin mayúscula, sin especial) → Rechazada")
    void testPasswordWithoutUppercaseAndSpecial() {
        // Act & Assert
        assertThrows(WeakPasswordException.class, () -> {
            passwordValidator.validate("password123");
        });
    }

    @Test
    @DisplayName("Contraseña débil: 'Password!' (sin número) → Rechazada")
    void testPasswordWithoutNumbers() {
        // Act & Assert
        assertThrows(WeakPasswordException.class, () -> {
            passwordValidator.validate("Password!");
        });
    }

    @Test
    @DisplayName("Contraseña débil: 'Password123' (sin carácter especial) → Rechazada")
    void testPasswordWithoutSpecialChar() {
        // Act & Assert
        assertThrows(WeakPasswordException.class, () -> {
            passwordValidator.validate("Password123");
        });
    }

    @Test
    @DisplayName("Contraseña vacía → Rechazada")
    void testEmptyPassword() {
        // Act & Assert
        assertThrows(WeakPasswordException.class, () -> {
            passwordValidator.validate("");
        });
    }

    @Test
    @DisplayName("Contraseña null → Rechazada")
    void testNullPassword() {
        // Act & Assert
        assertThrows(WeakPasswordException.class, () -> {
            passwordValidator.validate(null);
        });
    }
}
