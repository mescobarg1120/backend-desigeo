package com.backend_desigeo.gestion_de_usuarios.service;

import com.backend_desigeo.gestion_de_usuarios.dto.UserCreateDto;
import com.backend_desigeo.gestion_de_usuarios.dto.UserDto;
import com.backend_desigeo.gestion_de_usuarios.dto.UserUpdateDto;
import com.backend_desigeo.gestion_de_usuarios.entity.Role;
import com.backend_desigeo.gestion_de_usuarios.entity.User;
import com.backend_desigeo.gestion_de_usuarios.repository.RoleRepository;
import com.backend_desigeo.gestion_de_usuarios.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Optional<UserDto> getUserById(UUID userId) {
        return userRepository.findById(userId).map(this::mapToDto);
    }

    public Optional<UserDto> getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email).map(this::mapToDto);
    }

    public UserDto createUser(UserCreateDto createDto) {
        if (userRepository.findByEmailIgnoreCase(createDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with email already exists");
        }

        Role role = roleRepository.findByRoleName(createDto.getRoleName())
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail(createDto.getEmail().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(createDto.getPassword()));
        user.setFullName(createDto.getFullName());
        user.setPhone(createDto.getPhone());
        user.setRole(role);
        user.setRoleId(role.getRoleId());
        user.setNotificationPrefs("EMAIL");
        user.setActive(Boolean.TRUE);
        user.setFailedLoginCount(0);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }

    public Optional<UserDto> updateUser(UUID userId, UserUpdateDto updateDto) {
        return userRepository.findById(userId).map(user -> {
            if (updateDto.getEmail() != null) {
                if (!updateDto.getEmail().equalsIgnoreCase(user.getEmail()) &&
                    userRepository.findByEmailIgnoreCase(updateDto.getEmail()).isPresent()) {
                    throw new IllegalArgumentException("User with email already exists");
                }
                user.setEmail(updateDto.getEmail().toLowerCase());
            }
            if (updateDto.getFullName() != null) {
                user.setFullName(updateDto.getFullName());
            }
            if (updateDto.getRoleName() != null) {
                Role role = roleRepository.findByRoleName(updateDto.getRoleName())
                        .orElseThrow(() -> new IllegalArgumentException("Role not found"));
                user.setRole(role);
                user.setRoleId(role.getRoleId());
            }
            if (updateDto.getActive() != null) {
                user.setActive(updateDto.getActive());
            }
            user.setUpdatedAt(Instant.now());

            return mapToDto(userRepository.save(user));
        });
    }

    public boolean deleteUser(UUID userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    private UserDto mapToDto(User user) {
        UserDto dto = new UserDto();
        dto.setUserId(user.getUserId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRoleName(user.getRole() != null ? user.getRole().getRoleName() : null);
        dto.setActive(user.getActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}