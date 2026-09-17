package com.gpomares.adventurebook.application;

import com.gpomares.adventurebook.domain.AdventureBook;
import com.gpomares.adventurebook.domain.AdventureBookDomainService;
import com.gpomares.adventurebook.domain.AdventureBookFilter;
import com.gpomares.adventurebook.domain.Difficulty;
import com.gpomares.adventurebook.dto.AdventureBookSummaryDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class AdventureBookServiceImplTest {

    @Test
    void delegatesLookupAndReturnsTheMappedDto() {
        var book = AdventureBook.Builder.adventureBook().title("Book").author("Author")
                .difficulty(Difficulty.EASY).build();
        var dto = new AdventureBookSummaryDto(1L, "Book", "Author", "EASY", java.util.Set.of());
        var domainService = new AdventureBookDomainService() {
            Long lookedUpId;
            @Override public AdventureBook findById(Long id) { lookedUpId = id; return book; }

            @Override
            public java.util.List<AdventureBook> search(AdventureBookFilter filter) {
                return java.util.List.of();
            }
            @Override public boolean addCategory(Long id, String category) { return false; }
            @Override public void replaceCategories(Long id, java.util.List<String> categories) { }
            @Override public void removeCategory(Long id, String category) { }
        };
        var mapper = new AdventureBookMapper() {
            AdventureBook mappedBook;
            @Override public AdventureBookSummaryDto mapSummary(AdventureBook value) { mappedBook = value; return dto; }
        };

        assertSame(dto, new AdventureBookServiceImpl(domainService, mapper).get(1L));
        org.junit.jupiter.api.Assertions.assertEquals(1L, domainService.lookedUpId);
        assertSame(book, mapper.mappedBook);
    }

    @Test
    void searchesWithTheSuppliedFilterAndUsesSummaryMapping() {
        var fullBook = AdventureBook.Builder.adventureBook().title("Book").author("Author")
                .difficulty(Difficulty.EASY)
                .sections(java.util.List.of(com.gpomares.adventurebook.domain.Section.Builder.section()
                        .id(1).text("section").type(com.gpomares.adventurebook.domain.SectionType.END).build()))
                .build();
        var domainService = new AdventureBookDomainService() {
            @Override
            public AdventureBook findById(Long id) {
                throw new UnsupportedOperationException();
            }

            AdventureBookFilter receivedFilter;

            @Override
            public java.util.List<AdventureBook> search(AdventureBookFilter filter) {
                receivedFilter = filter;
                return java.util.List.of(fullBook);
            }

            @Override
            public boolean addCategory(Long id, String category) {
                return false;
            }

            @Override
            public void replaceCategories(Long id, java.util.List<String> categories) {
            }

            @Override
            public void removeCategory(Long id, String category) {
            }
        };
        var summary = new AdventureBookSummaryDto(null, "Book", "Author", "EASY", java.util.Set.of());
        var filter = new AdventureBookFilter(" Dragon ", " AUTHOR ", " Fantasy ", Difficulty.EASY);
        var result = new AdventureBookServiceImpl(domainService, new AdventureBookMapper()).search(filter);

        assertEquals(summary, result.getFirst());
        assertSame(filter, domainService.receivedFilter);
    }
}
