package com.backend_desigeo.desigeo_auth_service.service;

import com.backend_desigeo.desigeo_auth_service.exception.WeakPasswordException;
import org.springframework.stereotype.Service;

@Service
public class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final String UPPERCASE_PATTERN = ".*[A-Z].*";
    private static final String LOWERCASE_PATTERN = ".*[a-z].*";
    private static final String DIGIT_PATTERN = ".*[0-9].*";
    private static final String SPECIAL_CHAR_PATTERN = ".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>?/~`].*";

    public void validate(String password) {
        if (password == null || password.isEmpty()) {
            throw new WeakPasswordException("Password cannot be empty");
        }

        if (password.length() < MIN_LENGTH) {
            throw new WeakPasswordException(
                    "Password must be at least " + MIN_LENGTH + " characters long"
            );
        }

        if (!password.matches(UPPERCASE_PATTERN)) {
            throw new WeakPasswordException("Password must contain at least one uppercase letter");
        }

        if (!password.matches(LOWERCASE_PATTERN)) {
            throw new WeakPasswordException("Password must contain at least one lowercase letter");
        }

        if (!password.matches(DIGIT_PATTERN)) {
            throw new WeakPasswordException("Password must contain at least one digit");
        }

        if (!password.matches(SPECIAL_CHAR_PATTERN)) {
            throw new WeakPasswordException("Password must contain at least one special character");
        }
    }
}
