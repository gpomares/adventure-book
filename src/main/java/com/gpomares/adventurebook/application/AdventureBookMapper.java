package com.gpomares.adventurebook.application;

import com.gpomares.adventurebook.domain.AdventureBook;
import com.gpomares.adventurebook.domain.Consequence;
import com.gpomares.adventurebook.domain.Option;
import com.gpomares.adventurebook.domain.Section;
import com.gpomares.adventurebook.dto.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AdventureBookMapper {

    public AdventureBookSummaryDto mapSummary(AdventureBook adventureBook) {
        Objects.requireNonNull(adventureBook, "adventureBook must not be null");

        return new AdventureBookSummaryDto(
                adventureBook.getId(),
                adventureBook.getTitle(),
                adventureBook.getAuthor(),
                nameOf(adventureBook.getDifficulty()),
                adventureBook.getCategories());
    }

    public AdventureBookDto map(AdventureBook adventureBook) {
        Objects.requireNonNull(adventureBook, "adventureBook must not be null");

        return new AdventureBookDto(
                adventureBook.getId(),
                adventureBook.getTitle(),
                adventureBook.getAuthor(),
                nameOf(adventureBook.getDifficulty()),
                adventureBook.getCategories(),
                adventureBook.getSections().stream()
                        .map(this::mapSection)
                        .toList());
    }

    private SectionDto mapSection(Section section) {
        return new SectionDto(
                section.getSectionNumber(),
                section.getText(),
                nameOf(section.getType()),
                section.getOptions().stream()
                        .map(this::mapOption)
                        .toList());
    }

    private OptionDto mapOption(Option option) {
        return new OptionDto(
                option.getId(),
                option.getDescription(),
                option.getGotoId(),
                mapConsequence(option.getConsequence()));
    }

    private ConsequenceDto mapConsequence(Consequence consequence) {
        if (consequence == null) {
            return null;
        }

        return new ConsequenceDto(
                nameOf(consequence.getType()),
                consequence.getValue(),
                consequence.getText());
    }

    private String nameOf(Enum<?> value) {
        return value == null ? null : value.name();
    }
}
