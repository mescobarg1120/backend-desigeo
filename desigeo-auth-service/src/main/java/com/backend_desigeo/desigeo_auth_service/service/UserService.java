package com.backend_desigeo.desigeo_auth_service.service;

import com.backend_desigeo.desigeo_auth_service.dto.CreateUserRequest;
import com.backend_desigeo.desigeo_auth_service.dto.UserDTO;
import com.backend_desigeo.desigeo_auth_service.entity.User;
import com.backend_desigeo.desigeo_auth_service.exception.UserAlreadyExistsException;
import com.backend_desigeo.desigeo_auth_service.repository.UserRepository;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, PasswordValidator passwordValidator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordValidator = passwordValidator;
    }

    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        // Validar contraseña segura
        passwordValidator.validate(request.getPassword());
        
        if (userRepository.findByEmailIgnoreCase(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        User user = new User();
        user.setUserId(java.util.UUID.randomUUID());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRoleId(request.getRoleId());
        user.setActive(Boolean.TRUE.equals(request.getActive()));
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(Instant.now());

        return mapToDTO(userRepository.save(user));
    }

    private UserDTO mapToDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRoleId(user.getRoleId());
        dto.setRoleName(user.getRole() != null ? user.getRole().getRoleName() : null);
        dto.setActive(user.isActive());
        return dto;
    }
}
