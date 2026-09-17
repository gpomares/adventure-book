package com.gpomares.adventurebook.application;

import com.gpomares.adventurebook.domain.User;
import com.gpomares.adventurebook.domain.UserRepository;
import com.gpomares.adventurebook.dto.UserDto;
import com.gpomares.adventurebook.exception.InvalidUserRegistrationException;
import com.gpomares.adventurebook.exception.UserAlreadyExistsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class UserRegistrationServiceImpl implements UserRegistrationService {

    private static final int MINIMUM_PASSWORD_LENGTH = 12;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserDto register(String email, String password) {
        String normalizedEmail = normalizeAndValidateEmail(email);
        validatePassword(password);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException();
        }

        User user = new User(normalizedEmail, passwordEncoder.encode(password), Instant.now());
        try {
            User persisted = userRepository.saveAndFlush(user);
            return new UserDto(persisted.getId(), persisted.getEmail(), persisted.getCreatedAt());
        } catch (DataIntegrityViolationException exception) {
            throw new UserAlreadyExistsException();
        }
    }

    private String normalizeAndValidateEmail(String email) {
        if (email == null) {
            throw new InvalidUserRegistrationException("Email is required");
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > 320 || !EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new InvalidUserRegistrationException("Email must be a valid email address");
        }
        return normalized;
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < MINIMUM_PASSWORD_LENGTH) {
            throw new InvalidUserRegistrationException("Password must be at least 12 characters long");
        }
    }
}
