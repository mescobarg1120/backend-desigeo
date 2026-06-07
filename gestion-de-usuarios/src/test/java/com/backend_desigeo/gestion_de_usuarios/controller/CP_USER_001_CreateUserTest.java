package com.backend_desigeo.gestion_de_usuarios.controller;

import com.backend_desigeo.gestion_de_usuarios.dto.UserCreateDto;
import com.backend_desigeo.gestion_de_usuarios.dto.UserDto;
import com.backend_desigeo.gestion_de_usuarios.entity.RoleName;
import com.backend_desigeo.gestion_de_usuarios.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CP-USER-001 — Crear usuario con rol válido
 * POST /api/users + email + nombre + rol → 201 Created
 * Tipo: JUnit5 / @WebMvcTest (capa controller aislada con MockBean del servicio)
 */
@WebMvcTest(UserController.class)
@DisplayName("CP-USER-001: Crear usuario con rol válido")
class CP_USER_001_CreateUserTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser
    @DisplayName("POST /api/users con email, nombre y rol válido debe retornar 201 Created")
    void crearUsuarioConRolValido_retorna201() throws Exception {
        // Arrange — request body
        UserCreateDto request = new UserCreateDto();
        request.setEmail("juan.perez@ejemplo.cl");
        request.setPassword("Seguro123!");
        request.setFullName("Juan Pérez");
        request.setRut("123456785");   // RUT con dígito verificador válido
        request.setRoleName(RoleName.CITIZEN);
        request.setPhone("+56912345678");

        // Arrange — respuesta simulada del servicio
        UserDto responseDto = new UserDto();
        responseDto.setUserId(UUID.randomUUID());
        responseDto.setEmail("juan.perez@ejemplo.cl");
        responseDto.setFullName("Juan Pérez");
        responseDto.setRut("123456785");
        responseDto.setRoleName(RoleName.CITIZEN);
        responseDto.setActive(Boolean.TRUE);
        responseDto.setCreatedAt(Instant.now());

        when(userService.createUser(any(UserCreateDto.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("juan.perez@ejemplo.cl"))
                .andExpect(jsonPath("$.fullName").value("Juan Pérez"))
                .andExpect(jsonPath("$.roleName").value("CITIZEN"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.userId").isNotEmpty());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/users con rol ADMIN_MUNICIPAL debe retornar 201 Created")
    void crearUsuarioConRolAdminMunicipal_retorna201() throws Exception {
        // Arrange
        UserCreateDto request = new UserCreateDto();
        request.setEmail("admin@municipio.cl");
        request.setPassword("AdminPass1!");
        request.setFullName("María Administradora");
        request.setRut("123456785");
        request.setRoleName(RoleName.ADMIN_MUNICIPAL);

        UserDto responseDto = new UserDto();
        responseDto.setUserId(UUID.randomUUID());
        responseDto.setEmail("admin@municipio.cl");
        responseDto.setFullName("María Administradora");
        responseDto.setRoleName(RoleName.ADMIN_MUNICIPAL);
        responseDto.setActive(Boolean.TRUE);
        responseDto.setCreatedAt(Instant.now());

        when(userService.createUser(any(UserCreateDto.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roleName").value("ADMIN_MUNICIPAL"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/users sin email debe retornar 400 Bad Request (validación Bean)")
    void crearUsuarioSinEmail_retorna400() throws Exception {
        // Arrange — email ausente, debe fallar la validación @NotBlank
        UserCreateDto request = new UserCreateDto();
        request.setPassword("Seguro123!");
        request.setFullName("Sin Email");
        request.setRut("123456785");
        request.setRoleName(RoleName.CITIZEN);

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
