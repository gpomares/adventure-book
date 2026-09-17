package com.gpomares.adventurebook.web.controller;

import com.gpomares.adventurebook.application.AdventureBookService;
import com.gpomares.adventurebook.domain.AdventureBookFilter;
import com.gpomares.adventurebook.dto.AdventureBookSummaryDto;
import com.gpomares.adventurebook.web.exception.GlobalControllerExceptionHandler;
import com.gpomares.adventurebook.web.mapper.AdventureBookJsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdventureBookListControllerTest {

    private MockMvc mockMvc;
    private CapturingService service;

    @BeforeEach
    void setUp() {
        service = new CapturingService();
        mockMvc = MockMvcBuilders.standaloneSetup(new AdventureBookController(service, new AdventureBookJsonMapper()))
                .setControllerAdvice(new GlobalControllerExceptionHandler()).build();
    }

    @Test
    void returnsSummariesAndPassesAllFilters() throws Exception {
        service.books = List.of(new AdventureBookSummaryDto(1L, "Dragon", "Alice", "EASY", Set.of("fantasy")));

        mockMvc.perform(get("/api/adventure-books").param("title", " dragon ")
                        .param("author", "ALICE").param("category", "FANTASY")
                        .param("difficulty", "EASY"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(1));

        assertEquals(" dragon ", service.filter.title());
        assertEquals("ALICE", service.filter.author());
        assertEquals("FANTASY", service.filter.category());
        assertEquals("EASY", service.filter.difficulty().name());
    }

    @Test
    void returnsAnEmptyListAndRejectsInvalidDifficulty() throws Exception {
        service.books = List.of();
        mockMvc.perform(get("/api/adventure-books")).andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/api/adventure-books").param("difficulty", "impossible"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsEverySummaryWithoutPaginationAndIgnoresABlankDifficulty() throws Exception {
        service.books = java.util.stream.IntStream.rangeClosed(1, 21)
                .mapToObj(id -> new AdventureBookSummaryDto((long) id, "Book " + id, "Author", "EASY", Set.of()))
                .toList();

        mockMvc.perform(get("/api/adventure-books").param("difficulty", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(21));

        assertEquals(null, service.filter.difficulty());
    }

    private static class CapturingService implements AdventureBookService {
        private AdventureBookFilter filter;
        private List<AdventureBookSummaryDto> books = List.of();

        @Override
        public AdventureBookSummaryDto get(Long id) {
            return null;
        }

        @Override
        public List<AdventureBookSummaryDto> search(AdventureBookFilter filter) {
            this.filter = filter;
            return books;
        }

        @Override
        public boolean addCategory(Long id, String category) {
            return false;
        }

        @Override
        public void replaceCategories(Long id, List<String> categories) {
        }

        @Override
        public void removeCategory(Long id, String category) {
        }
    }
}
