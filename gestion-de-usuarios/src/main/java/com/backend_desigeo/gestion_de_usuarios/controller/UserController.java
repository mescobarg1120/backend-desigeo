package com.backend_desigeo.gestion_de_usuarios.controller;

import com.backend_desigeo.gestion_de_usuarios.dto.UserCreateDto;
import com.backend_desigeo.gestion_de_usuarios.dto.UserDto;
import com.backend_desigeo.gestion_de_usuarios.dto.UserUpdateDto;
import com.backend_desigeo.gestion_de_usuarios.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserDto>> getAllUsers(
            @RequestParam(required = false) Integer comunaId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        if ("ADMIN_MUNICIPAL".equals(userRole) && comunaId != null) {
            return ResponseEntity.ok(userService.getUsersByComunaId(comunaId));
        }
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID userId) {
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreateDto createDto) {
        // EmailAlreadyExistsException → 409 lo maneja GlobalExceptionHandler
        // IllegalArgumentException (RUT inválido, rol no encontrado) → 400
        UserDto user = userService.createUser(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user")
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID userId,
        @Valid @RequestBody UserUpdateDto updateDto) {
        // EmailAlreadyExistsException → 409 lo maneja GlobalExceptionHandler
        // IllegalArgumentException (rol no encontrado, admin municipal duplicado) → 400
        return userService.updateUser(userId, updateDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        boolean deleted = userService.deleteUser(userId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}