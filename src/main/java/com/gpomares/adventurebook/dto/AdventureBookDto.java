package com.gpomares.adventurebook.dto;

import java.util.Set;
import java.util.List;

public record AdventureBookDto(Long id,
                               String title,
                               String author,
                               String difficulty,
                               Set<String> categories,
                               List<SectionDto> sections) {
}
