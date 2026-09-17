package com.gpomares.adventurebook.application;

import com.gpomares.adventurebook.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadingSessionMapperTest {

    private final ReadingSessionMapper mapper = new ReadingSessionMapper();

    @Test
    void hidesOptionsWhenTheReadingSessionHasReachedATerminalState() {
        ReadingSession session = ReadingSession.start(1, 1);
        session.progressTo(2, 0, ReadingSessionStatus.DEAD);
        Section destination = Section.Builder.section().sectionNumber(2).text("A dangerous room")
                .type(SectionType.NODE)
                .options(List.of(Option.Builder.option().description("Continue").gotoId(3).build()))
                .build();

        var dto = mapper.map(new ReadingSessionState(session, destination, null));

        assertEquals("DEAD", dto.status());
        assertTrue(dto.section().options().isEmpty());
    }
}
