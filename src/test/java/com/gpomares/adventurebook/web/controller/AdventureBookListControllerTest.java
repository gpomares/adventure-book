package com.gpomares.adventurebook.web.controller;

import com.gpomares.adventurebook.application.AdventureBookService;
import com.gpomares.adventurebook.domain.AdventureBookFilter;
import com.gpomares.adventurebook.dto.AdventureBookSummaryDto;
import com.gpomares.adventurebook.web.exception.GlobalControllerExceptionHandler;
import com.gpomares.adventurebook.web.mapper.AdventureBookJsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
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
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalControllerExceptionHandler()).build();
    }

    @Test
    void returnsSummariesAndPassesAllFilters() throws Exception {
        service.page = new PageImpl<>(List.of(
                new AdventureBookSummaryDto(1L, "Dragon", "Alice", "EASY", Set.of("fantasy"))),
                PageRequest.of(1, 2), 3);

        mockMvc.perform(get("/api/adventure-books").param("title", " dragon ")
                        .param("author", "ALICE").param("category", "FANTASY")
                        .param("difficulty", "EASY").param("page", "1").param("size", "2")
                        .param("sort", "author,desc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.number").value(1)).andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3)).andExpect(jsonPath("$.totalPages").value(2));

        assertEquals(" dragon ", service.filter.title());
        assertEquals("ALICE", service.filter.author());
        assertEquals("FANTASY", service.filter.category());
        assertEquals("EASY", service.filter.difficulty().name());
        assertEquals(1, service.pageable.getPageNumber());
        assertEquals(2, service.pageable.getPageSize());
        assertEquals("author", service.pageable.getSort().getOrderFor("author").getProperty());
        assertEquals(org.springframework.data.domain.Sort.Direction.DESC,
                service.pageable.getSort().getOrderFor("author").getDirection());
    }

    @Test
    void returnsAnEmptyListAndRejectsInvalidDifficulty() throws Exception {
        service.page = Page.empty(PageRequest.of(0, 20));
        mockMvc.perform(get("/api/adventure-books")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty()).andExpect(jsonPath("$.number").value(0));
        mockMvc.perform(get("/api/adventure-books").param("difficulty", "impossible"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void appliesDefaultPaginationAndIgnoresABlankDifficulty() throws Exception {
        var books = java.util.stream.IntStream.rangeClosed(1, 20)
                .mapToObj(id -> new AdventureBookSummaryDto((long) id, "Book " + id, "Author", "EASY", Set.of()))
                .toList();
        service.page = new PageImpl<>(books, PageRequest.of(0, 20), 21);

        mockMvc.perform(get("/api/adventure-books").param("difficulty", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(20))
                .andExpect(jsonPath("$.totalElements").value(21))
                .andExpect(jsonPath("$.totalPages").value(2));

        assertEquals(null, service.filter.difficulty());
        assertEquals("title", service.pageable.getSort().getOrderFor("title").getProperty());
    }

    private static class CapturingService implements AdventureBookService {
        private AdventureBookFilter filter;
        private Page<AdventureBookSummaryDto> page = Page.empty();
        private Pageable pageable;

        @Override
        public AdventureBookSummaryDto get(Long id) {
            return null;
        }

        @Override
        public Page<AdventureBookSummaryDto> search(AdventureBookFilter filter, Pageable pageable) {
            this.filter = filter;
            this.pageable = pageable;
            return page;
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
