package com.backend_desigeo.desigeo_auth_service.exception;

/**
 * Lanzada cuando una cuenta es bloqueada por exceder el número máximo
 * de intentos de login fallidos. Corresponde a 429 Too Many Requests.
 */
public class AccountLockedException extends RuntimeException {
    public AccountLockedException(String message) {
        super(message);
    }
}
