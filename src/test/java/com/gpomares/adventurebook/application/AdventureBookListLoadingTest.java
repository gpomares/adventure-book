package com.gpomares.adventurebook.application;

import com.gpomares.adventurebook.domain.*;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class AdventureBookListLoadingTest {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private AdventureBookRepository repository;
    @Autowired
    private AdventureBookService service;

    @Test
    @Transactional
    void listSummaryDoesNotFetchSectionsOrOptions() {
        var option = Option.Builder.option().description("Continue").gotoId(2).build();
        var section = Section.Builder.section().id(901).text("Start").type(SectionType.BEGIN)
                .options(List.of(option)).build();
        var book = AdventureBook.Builder.adventureBook().title("Loading test").author("Author")
                .difficulty(Difficulty.EASY).sections(List.of(section)).build();
        entityManager.persist(book);
        entityManager.flush();
        entityManager.clear();

        SessionFactory sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();
        service.search(new com.gpomares.adventurebook.domain.AdventureBookFilter(null, null, null, null),
                PageRequest.of(0, 20));

        AdventureBook managed = entityManager.find(AdventureBook.class, book.getId());
        assertFalse(entityManager.getEntityManagerFactory().getPersistenceUnitUtil()
                .isLoaded(managed, "sections"));
        assertEquals(0, statistics.getCollectionStatistics(
                "com.gpomares.adventurebook.domain.AdventureBook.sections").getFetchCount());
        assertEquals(0, statistics.getCollectionStatistics(
                "com.gpomares.adventurebook.domain.Section.options").getFetchCount());
    }

    @Test
    void returnedSummaryCategoriesAreUsableAfterTheSearchTransactionEnds() {
        repository.save(AdventureBook.Builder.adventureBook().title("Categories").author("Author")
                .difficulty(Difficulty.EASY).categories(Set.of("Fantasy")).build());
        repository.flush();

        var summaries = service.search(new com.gpomares.adventurebook.domain.AdventureBookFilter(null, null, null, null),
                PageRequest.of(0, 20));

        assertEquals(Set.of("Fantasy"), summaries.stream()
                .filter(summary -> summary.title().equals("Categories"))
                .findFirst().orElseThrow().categories());
    }
}
