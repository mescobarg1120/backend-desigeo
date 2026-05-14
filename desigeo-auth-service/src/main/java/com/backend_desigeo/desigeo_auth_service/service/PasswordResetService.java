package com.backend_desigeo.desigeo_auth_service.service;

import com.backend_desigeo.desigeo_auth_service.dto.ForgotPasswordRequest;
import com.backend_desigeo.desigeo_auth_service.dto.ResetPasswordRequest;
import com.backend_desigeo.desigeo_auth_service.dto.VerifyResetCodeRequest;
import com.backend_desigeo.desigeo_auth_service.entity.PasswordReset;
import com.backend_desigeo.desigeo_auth_service.entity.User;
import com.backend_desigeo.desigeo_auth_service.exception.InvalidCredentialsException;
import com.backend_desigeo.desigeo_auth_service.repository.PasswordResetRepository;
import com.backend_desigeo.desigeo_auth_service.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_MINUTES = 15;
    private static final int MAX_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetRepository passwordResetRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(request.getEmail());

        // No revelar si el email existe o no (seguridad)
        if (userOpt.isEmpty()) {
            log.debug("Forgot password requested for non-existent email: {}", request.getEmail());
            return;
        }

        User user = userOpt.get();
        String code = generateCode();
        String token = UUID.randomUUID().toString();

        PasswordReset passwordReset = new PasswordReset();
        passwordReset.setUserId(user.getUserId());
        passwordReset.setCodeHash(passwordEncoder.encode(code));
        passwordReset.setToken(token);
        passwordReset.setExpiresAt(Instant.now().plus(EXPIRATION_MINUTES, ChronoUnit.MINUTES));
        passwordReset.setUsed(false);
        passwordReset.setAttempts(0);
        passwordReset.setCreatedAt(Instant.now());

        passwordResetRepository.save(passwordReset);

        emailService.sendPasswordResetEmail(user.getEmail(), code, token);
        log.info("Password reset initiated for user: {}", user.getUserId());
    }

    @Transactional
    public Map<String, Object> verifyResetCode(VerifyResetCodeRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid code or email"));

        PasswordReset passwordReset = passwordResetRepository
                .findLastActiveByUserId(user.getUserId(), Instant.now())
                .orElseThrow(() -> new InvalidCredentialsException("No active reset request found"));

        if (passwordReset.getAttempts() >= MAX_ATTEMPTS) {
            throw new InvalidCredentialsException("Too many attempts. Please request a new code.");
        }

        if (!passwordEncoder.matches(request.getCode(), passwordReset.getCodeHash())) {
            passwordReset.setAttempts(passwordReset.getAttempts() + 1);
            passwordResetRepository.save(passwordReset);
            throw new InvalidCredentialsException("Invalid code");
        }

        // Código válido: devolver el token para usarlo en reset-password
        return Map.of("ok", true, "token", passwordReset.getToken());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordReset passwordReset = passwordResetRepository
                .findByTokenAndUsedFalse(request.getToken())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid or expired token"));

        if (passwordReset.isExpired()) {
            throw new InvalidCredentialsException("Token has expired");
        }

        User user = userRepository.findById(passwordReset.getUserId())
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        // Actualizar contraseña
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        // Marcar como usado
        passwordReset.setUsed(true);
        passwordResetRepository.save(passwordReset);

        log.info("Password reset completed for user: {}", user.getUserId());
    }

    private String generateCode() {
        int code = secureRandom.nextInt(900000) + 100000; // 6 dígitos: 100000-999999
        return String.valueOf(code);
    }
}
