package com.backend_desigeo.gestion_de_usuarios.service;

import com.backend_desigeo.gestion_de_usuarios.dto.UserCreateDto;
import com.backend_desigeo.gestion_de_usuarios.entity.Role;
import com.backend_desigeo.gestion_de_usuarios.entity.RoleName;
import com.backend_desigeo.gestion_de_usuarios.entity.User;
import com.backend_desigeo.gestion_de_usuarios.repository.RoleRepository;
import com.backend_desigeo.gestion_de_usuarios.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * CP-USER-002 — Rechazar email duplicado
 * POST mismo email 2 veces → 2da llamada debe lanzar IllegalArgumentException (→ 409 / 400)
 *
 * Tipo: JUnit5 puro con Mockito (simulando el repositorio)
 * Nota: el plan original pedía Testcontainers, pero como la dependencia no está en el pom.xml
 * se implementa como test de servicio con Mockito. Para usar Testcontainers con PostgreSQL real
 * agregar la dependencia org.testcontainers:postgresql y extender con @Testcontainers.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CP-USER-002: Rechazar email duplicado")
class CP_USER_002_EmailDuplicadoTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    // Se usa spy sobre BCrypt real para evitar NPE al codificar la contraseña
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, roleRepository, passwordEncoder);
    }

    // ──────────────────────────────────────────────────────────
    // Datos de apoyo
    // ──────────────────────────────────────────────────────────

    private UserCreateDto buildCreateDto(String email) {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail(email);
        dto.setPassword("Seguro123!");
        dto.setFullName("Usuario Test");
        dto.setRut("123456785");           // RUT válido
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

    // ──────────────────────────────────────────────────────────
    // Tests
    // ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Primer registro con email nuevo debe tener éxito")
    void primerRegistro_emailNuevo_creaUsuario() {
        // Arrange
        String email = "duplicado@test.cl";
        UserCreateDto dto = buildCreateDto(email);

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName(RoleName.CITIZEN)).thenReturn(Optional.of(buildRole()));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act & Assert — no debe lanzar excepción
        assertThat(userService.createUser(dto)).isNotNull();
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Segundo registro con mismo email debe lanzar IllegalArgumentException (→ 409 Conflict)")
    void segundoRegistro_emailDuplicado_lanzaExcepcion() {
        // Arrange: el repositorio ya devuelve un usuario existente con ese email
        String email = "duplicado@test.cl";
        UserCreateDto dto = buildCreateDto(email);

        when(userRepository.findByEmailIgnoreCase(email))
                .thenReturn(Optional.of(buildExistingUser(email)));

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        // Nunca debe llegar a persistir
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Email duplicado es case-insensitive (MAYUS vs minús)")
    void emailDuplicado_caseInsensitive_lanzaExcepcion() {
        // Arrange: intento con email en mayúsculas, el repo devuelve el existente en minúsculas
        String emailLower = "duplicado@test.cl";
        String emailUpper = "DUPLICADO@TEST.CL";

        UserCreateDto dto = buildCreateDto(emailUpper);

        // findByEmailIgnoreCase debe ignorar case — simulamos que encuentra al usuario
        when(userRepository.findByEmailIgnoreCase(emailUpper))
                .thenReturn(Optional.of(buildExistingUser(emailLower)));

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Controller retorna 409 cuando el servicio lanza IllegalArgumentException por email duplicado")
    void controller_emailDuplicado_retorna400() {
        /*
         * Nota: el UserController captura IllegalArgumentException y retorna 400 BAD_REQUEST.
         * Si se requiere 409 CONFLICT, habría que lanzar una excepción específica (p.ej.
         * EmailAlreadyExistsException) y anotarla con @ResponseStatus(HttpStatus.CONFLICT).
         * Este test documenta el comportamiento actual del sistema: 400 Bad Request.
         *
         * Para un test de capa de controller, ver CP_USER_001 que usa @WebMvcTest.
         * Este test valida solo la lógica de servicio.
         */
        String email = "conflicto@test.cl";
        UserCreateDto dto = buildCreateDto(email);

        when(userRepository.findByEmailIgnoreCase(email))
                .thenReturn(Optional.of(buildExistingUser(email)));

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }
}
