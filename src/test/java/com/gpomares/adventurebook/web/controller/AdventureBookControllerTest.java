package com.gpomares.adventurebook.web.controller;

import com.gpomares.adventurebook.application.AdventureBookService;
import com.gpomares.adventurebook.dto.AdventureBookSummaryDto;
import com.gpomares.adventurebook.exception.AdventureBookNotFoundException;
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
            return dto;
        };
        mapper = new AdventureBookJsonMapper() {
            @Override
            public AdventureBookSummary mapSummary(AdventureBookSummaryDto value) {
                return new AdventureBookSummary(value.id(), value.title(), value.author(), value.difficulty(), value.categories());
            }
        };
        mockMvc = MockMvcBuilders.standaloneSetup(new AdventureBookController(service, mapper)).build();
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
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundWhenTheServiceCannotFindTheBook() throws Exception {
        mockMvc.perform(get("/api/adventure-books/99"))
                .andExpect(status().isNotFound());
    }
}
