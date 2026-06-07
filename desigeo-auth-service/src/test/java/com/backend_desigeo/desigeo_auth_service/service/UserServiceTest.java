package com.backend_desigeo.desigeo_auth_service.service;

import com.backend_desigeo.desigeo_auth_service.dto.CreateUserRequest;
import com.backend_desigeo.desigeo_auth_service.dto.UserDTO;
import com.backend_desigeo.desigeo_auth_service.entity.User;
import com.backend_desigeo.desigeo_auth_service.exception.UserAlreadyExistsException;
import com.backend_desigeo.desigeo_auth_service.exception.WeakPasswordException;
import com.backend_desigeo.desigeo_auth_service.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;

@DisplayName("CP-AUTH-001: Registro exitoso - UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordValidator passwordValidator;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, passwordEncoder, passwordValidator);
    }

    @Test
    @DisplayName("CP-AUTH-001: Registro exitoso con email válido y password segura → 201")
    void testSuccessfulRegistration() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("newuser@test.com");
        request.setPassword("SecurePass123!");
        request.setFullName("Test User");
        request.setRoleId(1);

        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        doNothing().when(passwordValidator).validate(anyString());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");

        User savedUser = new User();
        savedUser.setUserId(UUID.randomUUID());
        savedUser.setEmail("newuser@test.com");
        savedUser.setFullName("Test User");
        savedUser.setRoleId(1);
        savedUser.setActive(true);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserDTO result = userService.createUser(request);

        // Assert
        assertNotNull(result);
        assertEquals("newuser@test.com", result.getEmail());
        assertEquals("Test User", result.getFullName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("CP-AUTH-002: Rechazar contraseña débil ('123') → 429 Bad Request")
    void testWeakPasswordRejection() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("user@test.com");
        request.setPassword("123");
        request.setFullName("Test User");
        request.setRoleId(1);

        doThrow(new WeakPasswordException("Password must be at least 8 characters long"))
                .when(passwordValidator).validate(anyString());

        // Act & Assert
        assertThrows(WeakPasswordException.class, () -> {
            userService.createUser(request);
        });
    }

    @Test
    @DisplayName("CP-AUTH-002: Rechazar contraseña sin mayúsculas → 429 Bad Request")
    void testPasswordWithoutUppercase() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("user@test.com");
        request.setPassword("password123!");
        request.setFullName("Test User");
        request.setRoleId(1);

        doThrow(new WeakPasswordException("Password must contain at least one uppercase letter"))
                .when(passwordValidator).validate(anyString());

        // Act & Assert
        assertThrows(WeakPasswordException.class, () -> {
            userService.createUser(request);
        });
    }

    @Test
    @DisplayName("CP-AUTH-002: Rechazar contraseña sin caracteres especiales → 429 Bad Request")
    void testPasswordWithoutSpecialChar() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("user@test.com");
        request.setPassword("Password123");
        request.setFullName("Test User");
        request.setRoleId(1);

        doThrow(new WeakPasswordException("Password must contain at least one special character"))
                .when(passwordValidator).validate(anyString());

        // Act & Assert
        assertThrows(WeakPasswordException.class, () -> {
            userService.createUser(request);
        });
    }
}
