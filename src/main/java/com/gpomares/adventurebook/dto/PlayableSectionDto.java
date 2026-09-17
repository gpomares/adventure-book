package com.gpomares.adventurebook.dto;

import java.util.List;

public record PlayableSectionDto(long id, String text, String type, List<PlayableOptionDto> options) {
}
