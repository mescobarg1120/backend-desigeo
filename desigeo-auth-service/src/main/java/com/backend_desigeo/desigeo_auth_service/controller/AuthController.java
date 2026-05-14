package com.backend_desigeo.desigeo_auth_service.controller;

import com.backend_desigeo.desigeo_auth_service.dto.CreateUserRequest;
import com.backend_desigeo.desigeo_auth_service.dto.ForgotPasswordRequest;
import com.backend_desigeo.desigeo_auth_service.dto.LoginRequest;
import com.backend_desigeo.desigeo_auth_service.dto.LoginResponse;
import com.backend_desigeo.desigeo_auth_service.dto.ResetPasswordRequest;
import com.backend_desigeo.desigeo_auth_service.dto.UserDTO;
import com.backend_desigeo.desigeo_auth_service.dto.VerifyResetCodeRequest;
import com.backend_desigeo.desigeo_auth_service.service.AuthService;
import com.backend_desigeo.desigeo_auth_service.service.PasswordResetService;
import com.backend_desigeo.desigeo_auth_service.service.UserService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, UserService userService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.userService = userService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Boolean>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<Map<String, Object>> verifyResetCode(@Valid @RequestBody VerifyResetCodeRequest request) {
        Map<String, Object> result = passwordResetService.verifyResetCode(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Boolean>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
