package com.gpomares.adventurebook.dto;

import java.util.List;

public record SectionDto(Long id,
                         String text,
                         String type,
                         List<OptionDto> options) {
}
