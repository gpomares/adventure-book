package com.gpomares.adventurebook.web.controller;

import com.gpomares.adventurebook.application.ReadingSessionService;
import com.gpomares.adventurebook.domain.ReadingSessionConflictException;
import com.gpomares.adventurebook.domain.ReadingSessionNotFoundException;
import com.gpomares.adventurebook.dto.ConsequenceDto;
import com.gpomares.adventurebook.dto.PlayableOptionDto;
import com.gpomares.adventurebook.dto.PlayableSectionDto;
import com.gpomares.adventurebook.dto.ReadingSessionDto;
import com.gpomares.adventurebook.exception.AdventureBookNotFoundException;
import com.gpomares.adventurebook.security.AuthenticatedUser;
import com.gpomares.adventurebook.security.AuthenticatedUserProvider;
import com.gpomares.adventurebook.web.exception.GlobalControllerExceptionHandler;
import com.gpomares.adventurebook.web.mapper.ReadingSessionJsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReadingSessionControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ReadingSessionService service = new ReadingSessionService() {
            @Override
            public ReadingSessionDto start(Long bookId, Long userId) {
                if (bookId == 99L) {
                    throw new AdventureBookNotFoundException(bookId);
                }
                return initialState();
            }

            @Override
            public ReadingSessionDto chooseOption(Long bookId, Long sessionId, Long optionId, Long userId) {
                if (bookId == 99L) {
                    throw new AdventureBookNotFoundException(bookId);
                }
                if (sessionId == 98L) {
                    throw new ReadingSessionNotFoundException(sessionId);
                }
                if (sessionId == 97L) {
                    throw new ReadingSessionConflictException("Reading session is no longer in progress");
                }
                if (sessionId == 96L) {
                    throw new ObjectOptimisticLockingFailureException(Object.class, sessionId);
                }
                return progressedState();
            }
        };
        AuthenticatedUserProvider currentUser = () -> Optional.of(new AuthenticatedUser(1L, "reader@example.com"));
        mockMvc = MockMvcBuilders.standaloneSetup(new ReadingSessionController(service, new ReadingSessionJsonMapper(), currentUser))
                .setControllerAdvice(new GlobalControllerExceptionHandler())
                .build();
    }

    @Test
    void startsAReadingSessionAndReturnsItsPlayableBeginSection() throws Exception {
        mockMvc.perform(post("/api/adventure-books/7/reading-sessions"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/adventure-books/7/reading-sessions/42"))
                .andExpect(content().json("""
                        {"sessionId":42,"health":10,"status":"IN_PROGRESS","consequence":null,
                         "section":{"id":1,"text":"Entrance","type":"BEGIN",
                         "options":[{"id":12,"description":"Enter the cavern"}]}}
                        """));
    }

    @Test
    void advancesAReadingSessionWithoutExposingDestinationMetadataInOptions() throws Exception {
        mockMvc.perform(post("/api/adventure-books/7/reading-sessions/42/options/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.health").value(6))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.consequence.type").value("LOSE_HEALTH"))
                .andExpect(jsonPath("$.section.id").value(2))
                .andExpect(jsonPath("$.section.options[0].id").value(13))
                .andExpect(jsonPath("$.section.options[0].gotoId").doesNotExist());
    }

    @Test
    void returnsNotFoundForMissingBooksOrSessions() throws Exception {
        mockMvc.perform(post("/api/adventure-books/99/reading-sessions"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        mockMvc.perform(post("/api/adventure-books/7/reading-sessions/98/options/12"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void returnsConflictForAChoiceAfterTheSessionEnds() throws Exception {
        mockMvc.perform(post("/api/adventure-books/7/reading-sessions/97/options/12"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.detail").value("Reading session is no longer in progress"));
    }

    @Test
    void returnsConflictWhenAConcurrentRequestHasAlreadyAdvancedTheSession() throws Exception {
        mockMvc.perform(post("/api/adventure-books/7/reading-sessions/96/options/12"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.detail").value(
                        "Reading session was updated by another request; refresh its state and try again"));
    }

    @Test
    void rejectsMalformedPathIdentifiers() throws Exception {
        mockMvc.perform(post("/api/adventure-books/invalid/reading-sessions"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
    }

    private ReadingSessionDto initialState() {
        return new ReadingSessionDto(42L, 10, "IN_PROGRESS", null,
                new PlayableSectionDto(1, "Entrance", "BEGIN",
                        List.of(new PlayableOptionDto(12L, "Enter the cavern"))));
    }

    private ReadingSessionDto progressedState() {
        return new ReadingSessionDto(42L, 6, "IN_PROGRESS",
                new ConsequenceDto("LOSE_HEALTH", 4, "You scrape your shoulder."),
                new PlayableSectionDto(2, "Crevice", "NODE",
                        List.of(new PlayableOptionDto(13L, "Rest"))));
    }
}
