package com.backend_desigeo.gestion_de_usuarios.service;

import com.backend_desigeo.gestion_de_usuarios.exception.EmailAlreadyExistsException;
import com.backend_desigeo.gestion_de_usuarios.dto.UserCreateDto;
import com.backend_desigeo.gestion_de_usuarios.dto.UserDto;
import com.backend_desigeo.gestion_de_usuarios.dto.UserUpdateDto;
import com.backend_desigeo.gestion_de_usuarios.entity.Role;
import com.backend_desigeo.gestion_de_usuarios.entity.RoleName;
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
            throw new EmailAlreadyExistsException(createDto.getEmail());
        }

        if (!isValidRut(createDto.getRut().toUpperCase())) {
        throw new IllegalArgumentException("Invalid RUT");
        }

        Role role = roleRepository.findByRoleName(createDto.getRoleName())
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail(createDto.getEmail().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(createDto.getPassword()));
        user.setRut(createDto.getRut().toUpperCase());
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
                    throw new EmailAlreadyExistsException(updateDto.getEmail());
                }
                user.setEmail(updateDto.getEmail().toLowerCase());
            }
            if (updateDto.getFullName() != null) {
                user.setFullName(updateDto.getFullName());
            }
            if (updateDto.getPhone() != null) {
                user.setPhone(updateDto.getPhone());
            }
            if (updateDto.getRoleName() != null) {
            Role role = roleRepository.findByRoleName(updateDto.getRoleName())
                    .orElseThrow(() -> new IllegalArgumentException("Role not found"));
            
            // Validar que no haya otro ADMIN_MUNICIPAL en la misma comuna
            if (RoleName.ADMIN_MUNICIPAL.equals(updateDto.getRoleName())) {
                Integer comunaId = user.getComunaId();
                if (comunaId != null) {
                    boolean yaExiste = userRepository.findByComunaId(comunaId).stream()
                        .anyMatch(u -> !u.getUserId().equals(userId) &&
                                u.getRoleId() != null && u.getRoleId() == 3);
                    if (yaExiste) {
                        throw new IllegalArgumentException(
                            "Ya existe un administrador municipal asignado a esta comuna"
                        );
                    }
                }
            }
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
        dto.setRut(user.getRut());
        dto.setPhone(user.getPhone());
        dto.setRoleName(user.getRole() != null ? user.getRole().getRoleName() : null);
        dto.setActive(user.getActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setComunaId(user.getComunaId());
        return dto;
    }

    private boolean isValidRut(String rut) {
    try {
        String body = rut.substring(0, rut.length() - 1);
        char dv = Character.toUpperCase(rut.charAt(rut.length() - 1));
        int sum = 0;
        int multiplier = 2;
        for (int i = body.length() - 1; i >= 0; i--) {
            sum += Character.getNumericValue(body.charAt(i)) * multiplier;
            multiplier = multiplier == 7 ? 2 : multiplier + 1;
        }
        int remainder = 11 - (sum % 11);
        char expected = remainder == 11 ? '0' : remainder == 10 ? 'K' : (char) ('0' + remainder);
        return dv == expected;
    } catch (Exception e) {
        return false;
    }
    }

    public List<UserDto> getUsersByComunaId(Integer comunaId) {
    return userRepository.findByComunaId(comunaId)
            .stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }
}