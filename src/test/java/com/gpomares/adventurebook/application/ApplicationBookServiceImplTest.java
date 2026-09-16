package com.gpomares.adventurebook.application;

import com.gpomares.adventurebook.domain.AdventureBook;
import com.gpomares.adventurebook.domain.AdventureBookDomainService;
import com.gpomares.adventurebook.domain.Difficulty;
import com.gpomares.adventurebook.dto.AdventureBookSummaryDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class ApplicationBookServiceImplTest {

    @Test
    void delegatesLookupAndReturnsTheMappedDto() {
        var book = AdventureBook.Builder.adventureBook().title("Book").author("Author")
                .difficulty(Difficulty.EASY).build();
        var dto = new AdventureBookSummaryDto(1L, "Book", "Author", "EASY", java.util.Set.of());
        var domainService = new AdventureBookDomainService() {
            Long lookedUpId;
            @Override public AdventureBook findById(Long id) { lookedUpId = id; return book; }
        };
        var mapper = new AdventureBookMapper() {
            AdventureBook mappedBook;
            @Override public AdventureBookSummaryDto mapSummary(AdventureBook value) { mappedBook = value; return dto; }
        };

        assertSame(dto, new ApplicationBookServiceImpl(domainService, mapper).get(1L));
        org.junit.jupiter.api.Assertions.assertEquals(1L, domainService.lookedUpId);
        assertSame(book, mapper.mappedBook);
    }
}
