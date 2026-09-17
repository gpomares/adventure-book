package com.gpomares.adventurebook.application;

import com.gpomares.adventurebook.domain.Consequence;
import com.gpomares.adventurebook.domain.ReadingSessionState;
import com.gpomares.adventurebook.domain.ReadingSessionStatus;
import com.gpomares.adventurebook.domain.Section;
import com.gpomares.adventurebook.dto.ConsequenceDto;
import com.gpomares.adventurebook.dto.PlayableOptionDto;
import com.gpomares.adventurebook.dto.PlayableSectionDto;
import com.gpomares.adventurebook.dto.ReadingSessionDto;
import org.springframework.stereotype.Component;

@Component
public class ReadingSessionMapper {

    public ReadingSessionDto map(ReadingSessionState state) {
        return new ReadingSessionDto(
                state.session().getId(),
                state.session().getHealth(),
                state.session().getStatus().name(),
                mapConsequence(state.consequence()),
                mapSection(state.section(), state.session().getStatus()));
    }

    private PlayableSectionDto mapSection(Section section, ReadingSessionStatus status) {
        var options = status == ReadingSessionStatus.IN_PROGRESS
                ? section.getOptions().stream()
                .map(option -> new PlayableOptionDto(option.getId(), option.getDescription()))
                .toList()
                : java.util.List.<PlayableOptionDto>of();
        return new PlayableSectionDto(section.getSectionNumber(), section.getText(), section.getType().name(), options);
    }

    private ConsequenceDto mapConsequence(Consequence consequence) {
        if (consequence == null) {
            return null;
        }
        return new ConsequenceDto(
                consequence.getType() == null ? null : consequence.getType().name(),
                consequence.getValue(),
                consequence.getText());
    }
}
