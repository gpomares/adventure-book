package com.gpomares.adventurebook.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class SecurityIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private FilterChainProxy securityFilterChain;

    @Autowired
    private JwtService jwtService;

    @Test
    void requiresABearerTokenForApiEndpoints() throws Exception {
        mockMvc().perform(get("/api/adventure-books"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsMalformedBearerTokens() throws Exception {
        mockMvc().perform(get("/api/adventure-books")
                        .header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void acceptsAValidTokenAndExposesItsApplicationIdentity() throws Exception {
        String token = jwtService.createAccessToken(42L, "reader@example.com");

        assertEquals(new AuthenticatedUser(42L, "reader@example.com"), jwtService.validate(token));
        mockMvc().perform(get("/api/adventure-books")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private MockMvc mockMvc() {
        return webAppContextSetup(webApplicationContext)
                .addFilters(securityFilterChain)
                .build();
    }
}
