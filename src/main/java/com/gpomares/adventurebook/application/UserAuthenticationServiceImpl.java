package com.gpomares.adventurebook.application;

import com.gpomares.adventurebook.domain.User;
import com.gpomares.adventurebook.domain.UserRepository;
import com.gpomares.adventurebook.dto.AccessTokenDto;
import com.gpomares.adventurebook.exception.InvalidCredentialsException;
import com.gpomares.adventurebook.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class UserAuthenticationServiceImpl implements UserAuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserAuthenticationServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                         JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public AccessTokenDto login(String email, String password) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .filter(candidate -> password != null && passwordEncoder.matches(password, candidate.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        return new AccessTokenDto(jwtService.createAccessToken(user.getId(), user.getEmail()),
                "Bearer", jwtService.accessTokenExpiresInSeconds());
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
