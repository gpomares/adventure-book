package com.gpomares.adventurebook.web.controller;

import com.gpomares.adventurebook.domain.User;
import com.gpomares.adventurebook.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class UserRegistrationApiIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private FilterChainProxy securityFilterChain;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.gpomares.adventurebook.domain.ReadingSessionRepository readingSessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        readingSessionRepository.deleteAll();
        userRepository.deleteByEmailNot("legacy-reading-sessions@system.invalid");
        mockMvc = webAppContextSetup(webApplicationContext).addFilters(securityFilterChain).build();
    }

    @Test
    void registersANormalizedUserWithOnlySafeFieldsInTheResponse() throws Exception {
        String password = "correct horse battery staple";

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content("{\"email\":\" Reader@Example.COM \",\"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("http://localhost/api/users/\\d+")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("reader@example.com"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(password))));

        User user = userRepository.findByEmail("reader@example.com").orElseThrow();
        assertEquals("reader@example.com", user.getEmail());
        assertTrue(user.getPasswordHash().startsWith("$2"));
        assertTrue(passwordEncoder.matches(password, user.getPasswordHash()));
    }

    @Test
    void rejectsInvalidRegistrationInput() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content("{\"email\":\"not-an-email\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Email must be a valid email address"));

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content("{\"email\":\"reader@example.com\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Password must be at least 12 characters long"));
    }

    @Test
    void rejectsAnAlreadyRegisteredNormalizedEmail() throws Exception {
        String password = "correct horse battery staple";
        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content("{\"email\":\"reader@example.com\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content("{\"email\":\" READER@example.com \",\"password\":\"another secure password\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("An account with that email already exists"));

        assertEquals(2, userRepository.count()); // the migration's legacy user is retained
    }
}
