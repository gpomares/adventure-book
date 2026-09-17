package com.gpomares.adventurebook.web.controller;

import com.gpomares.adventurebook.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

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

    private MockMvc mockMvc;
    private AdventureBook book;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext).build();
        readingSessionRepository.deleteAll();
        adventureBookRepository.deleteAll();

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
        mockMvc.perform(post("/api/adventure-books/{bookId}/reading-sessions", book.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.health").value(10))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.section.id").value(1));
        long sessionId = readingSessionRepository.findAll().getFirst().getId();

        mockMvc.perform(post("/api/adventure-books/{bookId}/reading-sessions/{sessionId}/options/{optionId}",
                        book.getId(), sessionId, optionId(1, "Enter the crevice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.health").value(6))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.consequence.type").value("LOSE_HEALTH"))
                .andExpect(jsonPath("$.section.id").value(2));

        mockMvc.perform(post("/api/adventure-books/{bookId}/reading-sessions/{sessionId}/options/{optionId}",
                        book.getId(), sessionId, optionId(2, "Rest")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.health").value(6))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.section.id").value(3))
                .andExpect(jsonPath("$.section.options").isEmpty());
    }

    @Test
    void returnsStuckForAnAliveReaderWhoReachesTheDeadEnd() throws Exception {
        mockMvc.perform(post("/api/adventure-books/{bookId}/reading-sessions", book.getId()))
                .andExpect(status().isCreated());
        long sessionId = readingSessionRepository.findAll().getFirst().getId();

        mockMvc.perform(post("/api/adventure-books/{bookId}/reading-sessions/{sessionId}/options/{optionId}",
                        book.getId(), sessionId, optionId(1, "Take the dead end")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.health").value(10))
                .andExpect(jsonPath("$.status").value("STUCK"))
                .andExpect(jsonPath("$.section.id").value(4))
                .andExpect(jsonPath("$.section.options").isEmpty());
    }

    @Test
    void rejectsAnOptionFromAnotherSection() throws Exception {
        mockMvc.perform(post("/api/adventure-books/{bookId}/reading-sessions", book.getId()))
                .andExpect(status().isCreated());
        long sessionId = readingSessionRepository.findAll().getFirst().getId();

        mockMvc.perform(post("/api/adventure-books/{bookId}/reading-sessions/{sessionId}/options/{optionId}",
                        book.getId(), sessionId, optionId(2, "Rest")))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.detail").value("Option is not available from the current section"));
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
}
