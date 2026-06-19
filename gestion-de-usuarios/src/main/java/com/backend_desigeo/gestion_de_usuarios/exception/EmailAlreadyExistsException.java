package com.backend_desigeo.gestion_de_usuarios.exception;

/**
 * Lanzada cuando se intenta crear o actualizar un usuario con un email
 * que ya está registrado. Corresponde a 409 Conflict.
 */
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("Ya existe un usuario registrado con el email: " + email);
    }
}
