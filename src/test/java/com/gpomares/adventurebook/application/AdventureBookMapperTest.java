package com.gpomares.adventurebook.application;

import com.gpomares.adventurebook.domain.Consequence;
import com.gpomares.adventurebook.domain.ConsequenceType;
import com.gpomares.adventurebook.domain.Difficulty;
import com.gpomares.adventurebook.domain.Option;
import com.gpomares.adventurebook.domain.Section;
import com.gpomares.adventurebook.domain.SectionType;
import com.gpomares.adventurebook.domain.AdventureBook;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AdventureBookMapperTest {

    private final AdventureBookMapper mapper = new AdventureBookMapper();

    @Test
    void mapsTheCompleteBookTree() {
        Option option = Option.Builder.option()
                .description("Take the lantern")
                .gotoId(2)
                .consequence(Consequence.Builder.consequence()
                        .type(ConsequenceType.GAIN_HEALTH).value(3).text("You feel better.").build())
                .build();
        AdventureBook book = AdventureBook.Builder.adventureBook()
                .title("The Cave").author("A. Writer").difficulty(Difficulty.HARD)
                .categories(Set.of("fantasy", "classic"))
                .sections(List.of(Section.Builder.section().id(1).text("Start")
                        .type(SectionType.BEGIN).options(List.of(option)).build()))
                .build();

        var dto = mapper.map(book);

        assertNull(dto.id());
        assertEquals("The Cave", dto.title());
        assertEquals("A. Writer", dto.author());
        assertEquals("HARD", dto.difficulty());
        assertEquals(Set.of("fantasy", "classic"), dto.categories());
        assertEquals(1, dto.sections().size());
        var section = dto.sections().getFirst();
        assertEquals(1L, section.id());
        assertEquals("Start", section.text());
        assertEquals("BEGIN", section.type());
        var mappedOption = section.options().getFirst();
        assertEquals("Take the lantern", mappedOption.description());
        assertEquals(2L, mappedOption.gotoId());
        assertEquals("GAIN_HEALTH", mappedOption.consequence().type());
        assertEquals(3, mappedOption.consequence().value());
        assertEquals("You feel better.", mappedOption.consequence().text());
    }

    @Test
    void mapsAnOptionWithoutAConsequenceToNull() {
        var book = AdventureBook.Builder.adventureBook()
                .title("Book").author("Author").difficulty(Difficulty.EASY)
                .sections(List.of(Section.Builder.section().id(1).text("Text").type(SectionType.END)
                        .options(List.of(Option.Builder.option().description("Finish").gotoId(1).build())).build()))
                .build();

        assertNull(mapper.map(book).sections().getFirst().options().getFirst().consequence());
    }
}
