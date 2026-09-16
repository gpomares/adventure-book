package com.gpomares.adventurebook.web.controller;

import com.gpomares.adventurebook.application.AdventureBookService;
import com.gpomares.adventurebook.dto.AdventureBookSummaryDto;
import com.gpomares.adventurebook.exception.AdventureBookNotFoundException;
import com.gpomares.adventurebook.exception.InvalidAdventureBookException;
import com.gpomares.adventurebook.web.exception.GlobalControllerExceptionHandler;
import com.gpomares.adventurebook.web.json.AdventureBookSummary;
import com.gpomares.adventurebook.web.mapper.AdventureBookJsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdventureBookControllerTest {

    private AdventureBookSummaryDto dto;
    private AdventureBookService service;
    private AdventureBookJsonMapper mapper;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        dto = new AdventureBookSummaryDto(7L, "Book", "Author", "EASY", Set.of("fantasy"));
        service = id -> {
            if (id == 99L) throw new AdventureBookNotFoundException(99L);
            if (id == 400L) throw new InvalidAdventureBookException("Category must not be blank");
            if (id == 500L) throw new IllegalStateException("sensitive internal failure");
            return dto;
        };
        mapper = new AdventureBookJsonMapper() {
            @Override
            public AdventureBookSummary mapSummary(AdventureBookSummaryDto value) {
                return new AdventureBookSummary(value.id(), value.title(), value.author(), value.difficulty(), value.categories());
            }
        };
        mockMvc = MockMvcBuilders.standaloneSetup(new AdventureBookController(service, mapper))
                .setControllerAdvice(new GlobalControllerExceptionHandler())
                .build();
    }

    @Test
    void returnsTheExpectedJsonForAFoundBook() throws Exception {
        mockMvc.perform(get("/api/adventure-books/7"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":7,\"title\":\"Book\",\"author\":\"Author\",\"difficulty\":\"EASY\",\"categories\":[\"fantasy\"]}"));
    }

    @Test
    void rejectsMalformedIds() throws Exception {
        mockMvc.perform(get("/api/adventure-books/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.instance").value("/api/adventure-books/not-a-number"));

    }

    @Test
    void returnsBadRequestForInvalidAdventureBook() throws Exception {
        mockMvc.perform(get("/api/adventure-books/400"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Category must not be blank"))
                .andExpect(jsonPath("$.instance").value("/api/adventure-books/400"));
    }

    @Test
    void returnsNotFoundWhenTheServiceCannotFindTheBook() throws Exception {
        mockMvc.perform(get("/api/adventure-books/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("AdventureBook with id 99 not found"));
    }

    @Test
    void hidesUnexpectedServiceFailureDetails() throws Exception {
        mockMvc.perform(get("/api/adventure-books/500"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("sensitive internal failure"))));
    }
}
