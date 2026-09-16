package com.gpomares.adventurebook.dto;

public record OptionDto(Long id,
                        String description,
                        Long gotoId,
                        ConsequenceDto consequence) {
}
