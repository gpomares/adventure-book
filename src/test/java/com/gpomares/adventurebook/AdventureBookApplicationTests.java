package com.gpomares.adventurebook;

import com.gpomares.adventurebook.application.AdventureBookService;
import com.gpomares.adventurebook.domain.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
class AdventureBookApplicationTests {

    @jakarta.annotation.Resource
    private EntityManager entityManager;

    @jakarta.annotation.Resource
    private JdbcTemplate jdbcTemplate;

    @jakarta.annotation.Resource
    private AdventureBookService adventureBookService;

    @jakarta.annotation.Resource
    private AdventureBookRepository adventureBookRepository;

	@Test
    void contextLoads() {
	}

    @Test
    void readsCategoriesThroughTheApplicationServiceWithOpenSessionInViewDisabled() {
        AdventureBook book = AdventureBook.Builder.adventureBook()
                .title("Lazy categories")
                .author("Test author")
                .difficulty(Difficulty.EASY)
                .categories(java.util.Set.of("fantasy"))
                .build();
        AdventureBook saved = adventureBookRepository.save(book);

        var summary = adventureBookService.get(saved.getId());

        Assertions.assertEquals(java.util.Set.of("fantasy"), summary.categories());
    }

    @Test
    void appliesFlywayMigrationAndValidatesJpaSchemaOnStartup() {
        Assertions.assertEquals(1, jdbcTemplate.queryForObject(
                "select count(*) from \"flyway_schema_history\" where \"version\" = '1'", Integer.class));
        Assertions.assertEquals(1, jdbcTemplate.queryForObject(
                "select count(*) from \"flyway_schema_history\" where \"version\" = '5'", Integer.class));
        Assertions.assertEquals(1, jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_name = 'ADVENTURE_BOOKS'", Integer.class));
        Assertions.assertEquals(1, jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_name = 'APP_USERS'", Integer.class));
    }

    @Test
    @Transactional
    void persistsTheBookContainmentTree() {
        Consequence consequence = Consequence.Builder.consequence()
                .type(ConsequenceType.GAIN_HEALTH)
                .value(1)
                .text("You regain your footing.")
                .build();
        Option option = Option.Builder.option()
                .description("Continue")
                .gotoId(2)
                .consequence(consequence)
                .build();
        Section firstSection = Section.Builder.section()
                .id(1)
                .text("The beginning")
                .type(SectionType.BEGIN)
                .options(List.of(option))
                .build();
        Section secondSection = Section.Builder.section()
                .id(2)
                .text("The end")
                .type(SectionType.END)
                .build();
        AdventureBook book = AdventureBook.Builder.adventureBook()
                .title("Test book")
                .author("Test author")
                .difficulty(Difficulty.EASY)
                .categories(java.util.Set.of("fantasy", "classic"))
                .sections(List.of(firstSection, secondSection))
                .build();

        entityManager.persist(book);
        entityManager.flush();
        entityManager.clear();

        AdventureBook persisted = entityManager.find(AdventureBook.class, book.getId());
        Assertions.assertEquals(2, persisted.getSections().size());
        Assertions.assertEquals(2, persisted.getSections().getFirst().getOptions().getFirst().getGotoId());
        Assertions.assertEquals(ConsequenceType.GAIN_HEALTH,
                persisted.getSections().getFirst().getOptions().getFirst().getConsequence().getType());
        Assertions.assertEquals(java.util.Set.of("fantasy", "classic"), persisted.getCategories());
    }

    @Test
    @Transactional
    void persistsAnOptionWithANullConsequence() {
        Option option = Option.Builder.option()
                .description("Finish")
                .gotoId(1)
                .build();
        AdventureBook book = AdventureBook.Builder.adventureBook()
                .title("Terminal book")
                .author("Test author")
                .difficulty(Difficulty.EASY)
                .sections(List.of(Section.Builder.section().id(1).text("End")
                        .type(SectionType.END).options(List.of(option)).build()))
                .build();

        entityManager.persist(book);
        entityManager.flush();
        entityManager.clear();

        AdventureBook persisted = entityManager.find(AdventureBook.class, book.getId());
        Assertions.assertNull(persisted.getSections().getFirst().getOptions().getFirst().getConsequence());
    }

    @Test
    @Transactional
    void deletesBooksWithCyclicOptions() {
        Option toTwo = Option.Builder.option().description("Two").gotoId(2).build();
        Option toOne = Option.Builder.option().description("One").gotoId(1).build();
        AdventureBook book = AdventureBook.Builder.adventureBook()
                .title("Cycle").author("Author").difficulty(Difficulty.EASY)
                .sections(List.of(
                        Section.Builder.section().id(1).text("One").type(SectionType.NODE)
                                .options(List.of(toTwo)).build(),
                        Section.Builder.section().id(2).text("Two").type(SectionType.NODE)
                                .options(List.of(toOne)).build()))
                .build();

        entityManager.persist(book);
        entityManager.flush();
        entityManager.clear();
        entityManager.remove(entityManager.find(AdventureBook.class, book.getId()));
        entityManager.flush();

        Assertions.assertEquals(0, jdbcTemplate.queryForObject(
                "select count(*) from options where id in (?, ?)", Integer.class, toTwo.getId(), toOne.getId()));
        Assertions.assertEquals(0, jdbcTemplate.queryForObject(
                "select count(*) from sections where book_id = ?", Integer.class, book.getId()));
    }

    @Test
    @Transactional
    void differentBooksCanReuseSectionNumbers() {
        for (int i = 0; i < 2; i++) {
            entityManager.persist(AdventureBook.Builder.adventureBook()
                    .title("Book " + i).author("Author").difficulty(Difficulty.EASY)
                    .sections(List.of(Section.Builder.section().sectionNumber(100)
                            .text("End").type(SectionType.END).build()))
                    .build());
        }
        entityManager.flush();
        Assertions.assertEquals(2, jdbcTemplate.queryForObject(
                "select count(*) from sections where section_number = 100", Integer.class));
    }

    @Test
    @Transactional
    void persistsCategoryMutationsThroughTheApplicationService() {
        AdventureBook book = AdventureBook.Builder.adventureBook()
                .title("Category book")
                .author("Test author")
                .difficulty(Difficulty.EASY)
                .categories(java.util.Set.of("old"))
                .build();
        entityManager.persist(book);
        entityManager.flush();

        org.junit.jupiter.api.Assertions.assertTrue(adventureBookService.addCategory(book.getId(), " fantasy "));
        adventureBookService.replaceCategories(book.getId(), java.util.List.of(" mystery ", "mystery", "Mystery"));
        adventureBookService.removeCategory(book.getId(), "missing");
        entityManager.flush();
        entityManager.clear();

        AdventureBook persisted = entityManager.find(AdventureBook.class, book.getId());
        org.junit.jupiter.api.Assertions.assertEquals(
                java.util.Set.of("mystery", "Mystery"), persisted.getCategories());
    }

}
