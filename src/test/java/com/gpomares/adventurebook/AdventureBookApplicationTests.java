package com.gpomares.adventurebook;

import com.gpomares.adventurebook.domain.AdventureBook;
import com.gpomares.adventurebook.domain.Consequence;
import com.gpomares.adventurebook.domain.ConsequenceType;
import com.gpomares.adventurebook.domain.Difficulty;
import com.gpomares.adventurebook.domain.Option;
import com.gpomares.adventurebook.domain.Section;
import com.gpomares.adventurebook.domain.SectionType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
class AdventureBookApplicationTests {

    @jakarta.annotation.Resource
    private EntityManager entityManager;

	@Test
	void contextLoads() {
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
    }

}
