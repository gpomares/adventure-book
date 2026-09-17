package com.gpomares.adventurebook.web.controller;

import com.gpomares.adventurebook.domain.*;
import com.gpomares.adventurebook.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class ReadingSessionApiIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AdventureBookRepository adventureBookRepository;

    @Autowired
    private ReadingSessionRepository readingSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private FilterChainProxy securityFilterChain;

    private MockMvc mockMvc;
    private AdventureBook book;
    private String bearerToken;
    private Long userId;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext).addFilters(securityFilterChain).build();
        readingSessionRepository.deleteAll();
        adventureBookRepository.deleteAll();
        userRepository.deleteByEmailNot("legacy-reading-sessions@system.invalid");
        User user = userRepository.saveAndFlush(new User("reader@example.com", "!", Instant.now()));
        userId = user.getId();
        bearerToken = jwtService.createAccessToken(user.getId(), user.getEmail());

        Option enterCrevice = Option.Builder.option().description("Enter the crevice")
                .gotoId(2)
                .consequence(Consequence.Builder.consequence().type(ConsequenceType.LOSE_HEALTH)
                        .value(4).text("You scrape your shoulder.").build())
                .build();
        Option takeDeadEnd = Option.Builder.option().description("Take the dead end").gotoId(4).build();
        Option rest = Option.Builder.option().description("Rest").gotoId(3).build();

        book = adventureBookRepository.saveAndFlush(AdventureBook.Builder.adventureBook()
                .title("The Crystal Caverns")
                .author("Evelyn Stormrider")
                .difficulty(Difficulty.EASY)
                .sections(List.of(
                        Section.Builder.section().sectionNumber(1).text("Entrance").type(SectionType.BEGIN)
                                .options(List.of(enterCrevice, takeDeadEnd)).build(),
                        Section.Builder.section().sectionNumber(2).text("Crevice").type(SectionType.NODE)
                                .options(List.of(rest)).build(),
                        Section.Builder.section().sectionNumber(3).text("Escape").type(SectionType.END).build(),
                        Section.Builder.section().sectionNumber(4).text("Dead end").type(SectionType.NODE).build()))
                .build());
    }

    @Test
    void playsACompletePathThroughTheRealHttpApi() throws Exception {
        mockMvc.perform(authenticatedPost("/api/adventure-books/{bookId}/reading-sessions", book.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.health").value(10))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.section.id").value(1));
        long sessionId = readingSessionRepository.findAll().getFirst().getId();

        mockMvc.perform(authenticatedPost("/api/adventure-books/{bookId}/reading-sessions/{sessionId}/options/{optionId}",
                        book.getId(), sessionId, optionId(1, "Enter the crevice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.health").value(6))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.consequence.type").value("LOSE_HEALTH"))
                .andExpect(jsonPath("$.section.id").value(2));

        mockMvc.perform(authenticatedPost("/api/adventure-books/{bookId}/reading-sessions/{sessionId}/options/{optionId}",
                        book.getId(), sessionId, optionId(2, "Rest")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.health").value(6))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.section.id").value(3))
                .andExpect(jsonPath("$.section.options").isEmpty());
    }

    @Test
    void returnsStuckForAnAliveReaderWhoReachesTheDeadEnd() throws Exception {
        mockMvc.perform(authenticatedPost("/api/adventure-books/{bookId}/reading-sessions", book.getId()))
                .andExpect(status().isCreated());
        long sessionId = readingSessionRepository.findAll().getFirst().getId();

        mockMvc.perform(authenticatedPost("/api/adventure-books/{bookId}/reading-sessions/{sessionId}/options/{optionId}",
                        book.getId(), sessionId, optionId(1, "Take the dead end")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.health").value(10))
                .andExpect(jsonPath("$.status").value("STUCK"))
                .andExpect(jsonPath("$.section.id").value(4))
                .andExpect(jsonPath("$.section.options").isEmpty());
    }

    @Test
    void rejectsAnOptionFromAnotherSection() throws Exception {
        mockMvc.perform(authenticatedPost("/api/adventure-books/{bookId}/reading-sessions", book.getId()))
                .andExpect(status().isCreated());
        long sessionId = readingSessionRepository.findAll().getFirst().getId();

        mockMvc.perform(authenticatedPost("/api/adventure-books/{bookId}/reading-sessions/{sessionId}/options/{optionId}",
                        book.getId(), sessionId, optionId(2, "Rest")))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.detail").value("Option is not available from the current section"));
    }

    @Test
    void publishesBothReadingSessionOperationsInOpenApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/adventure-books/{bookId}/reading-sessions'].post").exists())
                .andExpect(jsonPath("$.paths['/api/adventure-books/{bookId}/reading-sessions/{sessionId}/options/{optionId}'].post").exists());
    }

    @Test
    void recordsAuthenticatedOwnershipAndAuditValuesServerSide() throws Exception {
        mockMvc.perform(authenticatedPost("/api/adventure-books/{bookId}/reading-sessions", book.getId()))
                .andExpect(status().isCreated());
        ReadingSession session = readingSessionRepository.findAll().getFirst();
        Instant createdAt = session.getCreatedAt();

        assertEquals(userId, session.getOwnerId());
        assertEquals(userId, session.getCreatedByUserId());
        assertEquals(userId, session.getUpdatedByUserId());
        assertNotNull(createdAt);
        assertNotNull(session.getUpdatedAt());

        mockMvc.perform(authenticatedPost("/api/adventure-books/{bookId}/reading-sessions/{sessionId}/options/{optionId}",
                        book.getId(), session.getId(), optionId(1, "Enter the crevice")))
                .andExpect(status().isOk());
        ReadingSession advanced = readingSessionRepository.findById(session.getId()).orElseThrow();

        assertEquals(userId, advanced.getOwnerId());
        assertEquals(userId, advanced.getCreatedByUserId());
        assertEquals(userId, advanced.getUpdatedByUserId());
        assertTrue(!advanced.getUpdatedAt().isBefore(createdAt));
    }

    @Test
    void doesNotDiscloseOrAdvanceAnotherUsersSession() throws Exception {
        mockMvc.perform(authenticatedPost("/api/adventure-books/{bookId}/reading-sessions", book.getId()))
                .andExpect(status().isCreated());
        ReadingSession session = readingSessionRepository.findAll().getFirst();
        User otherUser = userRepository.saveAndFlush(new User("other@example.com", "!", Instant.now()));
        String otherToken = jwtService.createAccessToken(otherUser.getId(), otherUser.getEmail());

        mockMvc.perform(post("/api/adventure-books/{bookId}/reading-sessions/{sessionId}/options/{optionId}",
                        book.getId(), session.getId(), optionId(1, "Enter the crevice"))
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());

        assertEquals(1, readingSessionRepository.findById(session.getId()).orElseThrow().getCurrentSectionNumber());
    }

    @Test
    void recoversOnlyTheAuthenticatedUsersInProgressSessions() throws Exception {
        mockMvc.perform(authenticatedPost("/api/adventure-books/{bookId}/reading-sessions", book.getId()))
                .andExpect(status().isCreated());
        Long sessionId = readingSessionRepository.findAll().getFirst().getId();
        User otherUser = userRepository.saveAndFlush(new User("other@example.com", "!", Instant.now()));
        String otherToken = jwtService.createAccessToken(otherUser.getId(), otherUser.getEmail());

        mockMvc.perform(get("/api/reading-sessions/{sessionId}", sessionId)
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.section.id").value(1));
        mockMvc.perform(get("/api/reading-sessions").param("status", "IN_PROGRESS")
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].sessionId").value(sessionId));

        mockMvc.perform(get("/api/reading-sessions/{sessionId}", sessionId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/reading-sessions").param("status", "IN_PROGRESS")
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private long optionId(long sectionNumber, String description) {
        return book.getSections().stream()
                .filter(section -> section.getSectionNumber() == sectionNumber)
                .flatMap(section -> section.getOptions().stream())
                .filter(option -> option.getDescription().equals(description))
                .findFirst()
                .orElseThrow()
                .getId();
    }

    private MockHttpServletRequestBuilder authenticatedPost(String path, Object... pathVariables) {
        return post(path, pathVariables).header("Authorization", "Bearer " + bearerToken);
    }
}
