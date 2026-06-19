package com.backend_desigeo.gestion_de_usuarios.service;

import com.backend_desigeo.gestion_de_usuarios.dto.UserCreateDto;
import com.backend_desigeo.gestion_de_usuarios.entity.Role;
import com.backend_desigeo.gestion_de_usuarios.entity.RoleName;
import com.backend_desigeo.gestion_de_usuarios.entity.User;
import com.backend_desigeo.gestion_de_usuarios.exception.EmailAlreadyExistsException;
import com.backend_desigeo.gestion_de_usuarios.repository.RoleRepository;
import com.backend_desigeo.gestion_de_usuarios.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CP-USER-002 — Rechazar email duplicado
 * POST mismo email 2 veces → 2da llamada lanza EmailAlreadyExistsException → 409 Conflict
 *
 * BUG-02 corregido: el servicio ahora lanza EmailAlreadyExistsException (tipada)
 * en lugar de IllegalArgumentException genérica, y el GlobalExceptionHandler
 * la mapea a 409 CONFLICT en vez de 400.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CP-USER-002: Rechazar email duplicado → 409 Conflict (BUG-02 corregido)")
class CP_USER_002_EmailDuplicadoTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, roleRepository, passwordEncoder);
    }

    // ── helpers ──────────────────────────────────────────────

    private UserCreateDto buildCreateDto(String email) {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail(email);
        dto.setPassword("Seguro123!");
        dto.setFullName("Usuario Test");
        dto.setRut("123456785");
        dto.setRoleName(RoleName.CITIZEN);
        return dto;
    }

    private Role buildRole() {
        Role role = new Role();
        role.setRoleId(1);
        role.setRoleName(RoleName.CITIZEN);
        role.setActive(Boolean.TRUE);
        return role;
    }

    private User buildExistingUser(String email) {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail(email.toLowerCase());
        user.setPasswordHash("$2a$hashed");
        user.setRut("123456785");
        user.setFullName("Usuario Existente");
        user.setRoleId(1);
        user.setRole(buildRole());
        user.setNotificationPrefs("EMAIL");
        user.setActive(Boolean.TRUE);
        user.setFailedLoginCount(0);
        return user;
    }

    // ── tests ─────────────────────────────────────────────────

    @Test
    @DisplayName("Primer registro con email nuevo debe tener éxito")
    void primerRegistro_emailNuevo_creaUsuario() {
        String email = "nuevo@test.cl";
        UserCreateDto dto = buildCreateDto(email);

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName(RoleName.CITIZEN)).thenReturn(Optional.of(buildRole()));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(userService.createUser(dto)).isNotNull();
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Segundo registro con mismo email lanza EmailAlreadyExistsException → 409 Conflict")
    void segundoRegistro_emailDuplicado_lanzaEmailAlreadyExistsException() {
        String email = "duplicado@test.cl";
        UserCreateDto dto = buildCreateDto(email);

        when(userRepository.findByEmailIgnoreCase(email))
                .thenReturn(Optional.of(buildExistingUser(email)));

        // BUG-02 fix: ahora lanza EmailAlreadyExistsException (no IllegalArgumentException)
        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("duplicado@test.cl");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Email duplicado es case-insensitive (MAYUS vs minús)")
    void emailDuplicado_caseInsensitive_lanzaExcepcion() {
        String emailUpper = "DUPLICADO@TEST.CL";
        String emailLower = "duplicado@test.cl";
        UserCreateDto dto = buildCreateDto(emailUpper);

        when(userRepository.findByEmailIgnoreCase(emailUpper))
                .thenReturn(Optional.of(buildExistingUser(emailLower)));

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("EmailAlreadyExistsException debe ser subclase de RuntimeException")
    void emailAlreadyExistsException_esRuntimeException() {
        EmailAlreadyExistsException ex = new EmailAlreadyExistsException("test@test.cl");
        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).contains("test@test.cl");
    }
}
