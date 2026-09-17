package com.gpomares.adventurebook.web.mapper;

import com.gpomares.adventurebook.dto.PlayableOptionDto;
import com.gpomares.adventurebook.dto.PlayableSectionDto;
import com.gpomares.adventurebook.dto.ReadingSessionDto;
import com.gpomares.adventurebook.web.json.PlayableOption;
import com.gpomares.adventurebook.web.json.PlayableSection;
import com.gpomares.adventurebook.web.json.ReadingSession;
import org.springframework.stereotype.Component;

@Component
public class ReadingSessionJsonMapper {

    public ReadingSession map(ReadingSessionDto dto) {
        return new ReadingSession(dto.sessionId(), dto.health(), dto.status(), dto.consequence(),
                mapSection(dto.section()));
    }

    private PlayableSection mapSection(PlayableSectionDto dto) {
        return new PlayableSection(dto.id(), dto.text(), dto.type(), dto.options().stream()
                .map(this::mapOption)
                .toList());
    }

    private PlayableOption mapOption(PlayableOptionDto dto) {
        return new PlayableOption(dto.id(), dto.description());
    }
}
