package com.gpomares.adventurebook.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AdventureBookRepositoryTest {

    @Autowired
    private AdventureBookRepository repository;

    @Autowired
    private AdventureBookDomainService domainService;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        repository.saveAll(java.util.List.of(
                book("Dragon Quest", "Alice", Difficulty.EASY, "Fantasy", "Classic"),
                book("dragon tales", "Bob", Difficulty.HARD, "fantasy"),
                book("Space Walk", "ALICE", Difficulty.MEDIUM, "Science"),
                book("1000 Adventure", "Carol", Difficulty.EASY, "Literal")));
        repository.flush();
    }

    @Test
    void matchesTitleAndAuthorCaseInsensitively() {
        assertEquals(2, repository.findAll(AdventureBookSpecifications.matching(
                new AdventureBookFilter(" DRAGON ", null, null, null))).size());
        assertEquals(2, repository.findAll(AdventureBookSpecifications.matching(
                new AdventureBookFilter(null, "alice", null, null))).size());
    }

    @Test
    void matchesCategoryWithoutDuplicatesAndDifficultyExactly() {
        repository.save(book("Two fantasy names", "Dana", Difficulty.EASY, "Fantasy", "fantasy"));
        repository.flush();

        var matches = repository.findAll(AdventureBookSpecifications.matching(
                new AdventureBookFilter(null, null, " fantasy ", Difficulty.EASY)));

        assertEquals(2, matches.size());
        assertEquals(Set.of("Dragon Quest", "Two fantasy names"),
                matches.stream().map(AdventureBook::getTitle).collect(java.util.stream.Collectors.toSet()));
        assertEquals(3, repository.findAll(AdventureBookSpecifications.matching(
                new AdventureBookFilter(null, null, "fantasy", null))).size());
        assertEquals(3, repository.findAll(AdventureBookSpecifications.matching(
                new AdventureBookFilter(null, null, null, Difficulty.EASY))).size());
    }

    @Test
    void combinesFiltersAndReturnsNoMatchesWhenNothingMatches() {
        var matches = repository.findAll(AdventureBookSpecifications.matching(
                new AdventureBookFilter("dragon", "alice", "fantasy", Difficulty.EASY)));

        assertEquals(1, matches.size());
        assertEquals("Dragon Quest", matches.getFirst().getTitle());
        assertTrue(repository.findAll(AdventureBookSpecifications.matching(
                new AdventureBookFilter("missing", null, null, null))).isEmpty());
    }

    @Test
    void treatsLikeWildcardsAsLiteralText() {
        repository.save(book("100% Adventure", "Carol", Difficulty.EASY, "Literal"));
        repository.save(book("100_ Adventure", "Carol", Difficulty.EASY, "Literal"));
        repository.flush();
        var page = repository.findAll(AdventureBookSpecifications.matching(
                new AdventureBookFilter("100% Adventure", null, null, null)));
        assertEquals(1, page.size());
        assertEquals("100% Adventure", page.getFirst().getTitle());
        assertEquals(1, repository.findAll(AdventureBookSpecifications.matching(
                new AdventureBookFilter("100_ Adventure", null, null, null))).size());
    }

    @Test
    void ignoresBlankFiltersAndListsMoreThanTwentyBooksInTitleThenIdOrder() {
        for (int index = 0; index < 21; index++) {
            repository.save(book("Extra %02d".formatted(index), "Author", Difficulty.EASY, "Extra"));
        }
        repository.flush();

        var books = domainService.search(new AdventureBookFilter(" ", "\t", " ", null));

        assertEquals(25, books.size());
        assertEquals("1000 Adventure", books.getFirst().getTitle());
        assertEquals("dragon tales", books.getLast().getTitle());
    }

    private AdventureBook book(String title, String author, Difficulty difficulty, String... categories) {
        return AdventureBook.Builder.adventureBook().title(title).author(author).difficulty(difficulty)
                .categories(Set.of(categories)).build();
    }
}
