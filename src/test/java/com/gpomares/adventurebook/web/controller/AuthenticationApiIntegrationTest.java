package com.gpomares.adventurebook.web.controller;

import com.gpomares.adventurebook.domain.User;
import com.gpomares.adventurebook.domain.UserRepository;
import com.gpomares.adventurebook.security.AuthenticatedUser;
import com.gpomares.adventurebook.security.JwtService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class AuthenticationApiIntegrationTest {

    private static final String EMAIL = "reader@example.com";
    private static final String PASSWORD = "correct horse battery staple";

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
    private JwtService jwtService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        readingSessionRepository.deleteAll();
        userRepository.deleteByEmailNot("legacy-reading-sessions@system.invalid");
        mockMvc = webAppContextSetup(webApplicationContext).addFilters(securityFilterChain).build();
    }

    @Test
    void registeredUserCanLoginAndUseTheIssuedToken() throws Exception {
        register(EMAIL, PASSWORD);
        User user = userRepository.findByEmail(EMAIL).orElseThrow();

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\" READER@example.com \",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String responseBody = loginResult.getResponse().getContentAsString();
        String token = JsonPath.read(responseBody, "$.accessToken");

        assertEquals("Bearer", JsonPath.read(responseBody, "$.tokenType"));
        assertEquals(3600, ((Number) JsonPath.read(responseBody, "$.expiresIn")).longValue());
        assertFalse(token.contains(PASSWORD));
        assertFalse(token.contains(user.getPasswordHash()));
        assertEquals(new AuthenticatedUser(user.getId(), EMAIL), jwtService.validate(token));

        mockMvc.perform(get("/api/adventure-books").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void unknownEmailAndInvalidPasswordReturnTheSameUnauthorizedResponse() throws Exception {
        register(EMAIL, PASSWORD);

        MvcResult unknownEmail = login("unknown@example.com", PASSWORD);
        MvcResult invalidPassword = login(EMAIL, "this password is incorrect");

        assertEquals(401, unknownEmail.getResponse().getStatus());
        assertEquals(unknownEmail.getResponse().getContentAsString(), invalidPassword.getResponse().getContentAsString());
    }

    private void register(String email, String password) throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated());
    }

    private MvcResult login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }
}
