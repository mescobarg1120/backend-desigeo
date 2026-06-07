package com.backend_desigeo.gestion_de_usuarios.controller;

import com.backend_desigeo.gestion_de_usuarios.dto.UserDto;
import com.backend_desigeo.gestion_de_usuarios.dto.UserUpdateDto;
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
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CP-USER-003 — Actualizar perfil de usuario
 * PUT /api/users/{userId} + nombre + teléfono → 200 OK
 *
 * Nota: el plan especifica PATCH /api/usuarios/{id}, pero el controller real expone
 * PUT /api/users/{userId}. El test sigue el contrato del código fuente.
 * Si en el futuro se agrega PATCH, este test deberá actualizarse.
 *
 * Tipo: JUnit5 / @WebMvcTest (capa controller aislada con MockBean del servicio)
 */
@WebMvcTest(UserController.class)
@DisplayName("CP-USER-003: Actualizar perfil de usuario")
class CP_USER_003_UpdateUserTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    // UUID fijo para todos los tests de esta clase
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000123");

    // ──────────────────────────────────────────────────────────
    // Tests
    // ──────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("PUT /api/users/{id} con nombre y teléfono actualizados debe retornar 200 OK")
    void actualizarNombreYTelefono_retorna200() throws Exception {
        // Arrange — request body
        UserUpdateDto request = new UserUpdateDto();
        request.setFullName("Juan Actualizado");
        request.setPhone("+56987654321");

        // Arrange — respuesta simulada del servicio
        UserDto responseDto = new UserDto();
        responseDto.setUserId(USER_ID);
        responseDto.setEmail("juan.perez@ejemplo.cl");
        responseDto.setFullName("Juan Actualizado");
        responseDto.setPhone("+56987654321");
        responseDto.setRoleName(RoleName.CITIZEN);
        responseDto.setActive(Boolean.TRUE);
        responseDto.setUpdatedAt(Instant.now());

        when(userService.updateUser(eq(USER_ID), any(UserUpdateDto.class)))
                .thenReturn(Optional.of(responseDto));

        // Act & Assert
        mockMvc.perform(put("/api/users/{id}", USER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID.toString()))
                .andExpect(jsonPath("$.fullName").value("Juan Actualizado"))
                .andExpect(jsonPath("$.phone").value("+56987654321"));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/users/{id} para un usuario inexistente debe retornar 404 Not Found")
    void actualizarUsuarioInexistente_retorna404() throws Exception {
        // Arrange
        UUID idInexistente = UUID.randomUUID();

        UserUpdateDto request = new UserUpdateDto();
        request.setFullName("Nadie");

        when(userService.updateUser(eq(idInexistente), any(UserUpdateDto.class)))
                .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/api/users/{id}", idInexistente)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/users/{id} con solo nombre actualizado debe retornar 200 OK")
    void actualizarSoloNombre_retorna200() throws Exception {
        // Arrange — solo actualiza el fullName (los demás campos son null → no se modifican)
        UserUpdateDto request = new UserUpdateDto();
        request.setFullName("Solo Nombre Nuevo");

        UserDto responseDto = new UserDto();
        responseDto.setUserId(USER_ID);
        responseDto.setEmail("juan.perez@ejemplo.cl");
        responseDto.setFullName("Solo Nombre Nuevo");
        responseDto.setPhone("+56912345678");           // phone no cambió
        responseDto.setRoleName(RoleName.CITIZEN);
        responseDto.setActive(Boolean.TRUE);

        when(userService.updateUser(eq(USER_ID), any(UserUpdateDto.class)))
                .thenReturn(Optional.of(responseDto));

        // Act & Assert
        mockMvc.perform(put("/api/users/{id}", USER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Solo Nombre Nuevo"))
                .andExpect(jsonPath("$.phone").value("+56912345678"));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/users/{id} con email inválido en body debe retornar 400 Bad Request")
    void actualizarConEmailInvalido_retorna400() throws Exception {
        // Arrange — @Email validation en UserUpdateDto debe rechazar esto
        UserUpdateDto request = new UserUpdateDto();
        request.setEmail("no-es-un-email");
        request.setFullName("Nombre Ok");

        // Act & Assert — la validación @Valid en el controller rechaza antes de llegar al servicio
        mockMvc.perform(put("/api/users/{id}", USER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
