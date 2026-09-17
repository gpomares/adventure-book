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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdventureBookControllerTest {

    private AdventureBookSummaryDto dto;
    private AdventureBookService service;
    private AdventureBookJsonMapper mapper;
    private MockMvc mockMvc;
    private Set<String> categoryState;

    @BeforeEach
    void setUp() {
        dto = new AdventureBookSummaryDto(7L, "Book", "Author", "EASY", Set.of("fantasy"));
        service = new AdventureBookService() {
            @Override
            public AdventureBookSummaryDto get(Long id) {
                if (id == 99L) throw new AdventureBookNotFoundException(99L);
                if (id == 400L) throw new InvalidAdventureBookException("Category must not be blank");
                if (id == 500L) throw new IllegalStateException("sensitive internal failure");
                return dto;
            }

            @Override
            public List<AdventureBookSummaryDto> search(com.gpomares.adventurebook.domain.AdventureBookFilter filter) {
                return List.of();
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

    @Test
    void returnsCreatedAndALocationWhenAddingANewCategory() throws Exception {
        useCategoryService(Set.of("existing"));

        mockMvc.perform(post("/api/adventure-books/7/categories")
                        .contentType("application/json").content("{\"name\":\" fantasy \"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/adventure-books/7/categories/fantasy"))
                .andExpect(content().string(""));
    }

    @Test
    void returnsNoContentWhenAddingADuplicateCategory() throws Exception {
        useCategoryService(Set.of("fantasy"));

        mockMvc.perform(post("/api/adventure-books/7/categories")
                        .contentType("application/json").content("{\"name\":\" fantasy \"}"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void replacesAndRemovesCategoriesWithNoResponseBody() throws Exception {
        useCategoryService(Set.of("fantasy"));

        mockMvc.perform(put("/api/adventure-books/7/categories")
                        .contentType("application/json").content("[\" fantasy \",\"fantasy\",\"Mystery\"]"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        org.junit.jupiter.api.Assertions.assertEquals(Set.of("fantasy", "Mystery"), categoryState);

        mockMvc.perform(delete("/api/adventure-books/7/categories/fantasy"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        mockMvc.perform(delete("/api/adventure-books/7/categories/absent"))
                .andExpect(status().isNoContent());
        org.junit.jupiter.api.Assertions.assertEquals(Set.of("Mystery"), categoryState);
    }

    @Test
    void returnsNotFoundForCategoryMutationOnMissingBook() throws Exception {
        useCategoryService(Set.of("fantasy"));

        mockMvc.perform(put("/api/adventure-books/99/categories")
                        .contentType("application/json").content("[]"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(404));
    }

    private void useCategoryService(Set<String> initialCategories) {
        categoryState = new LinkedHashSet<>(initialCategories);
        service = new AdventureBookService() {
            @Override
            public AdventureBookSummaryDto get(Long id) {
                return dto;
            }

            @Override
            public List<AdventureBookSummaryDto> search(com.gpomares.adventurebook.domain.AdventureBookFilter filter) {
                return List.of();
            }

            @Override
            public boolean addCategory(Long id, String category) {
                if (id == 99L) throw new AdventureBookNotFoundException(id);
                String normalized = category.trim();
                return categoryState.add(normalized);
            }

            @Override
            public void replaceCategories(Long id, List<String> categories) {
                if (id == 99L) throw new AdventureBookNotFoundException(id);
                categoryState.clear();
                categories.forEach(category -> categoryState.add(category.trim()));
            }

            @Override
            public void removeCategory(Long id, String category) {
                if (id == 99L) throw new AdventureBookNotFoundException(id);
                categoryState.remove(category.trim());
            }
        };
        mockMvc = MockMvcBuilders.standaloneSetup(new AdventureBookController(service, mapper))
                .setControllerAdvice(new GlobalControllerExceptionHandler())
                .build();
    }
}
