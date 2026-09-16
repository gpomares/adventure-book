package com.gpomares.adventurebook.web.json;

import java.util.Set;

public record AdventureBookSummary(
        Long id,
        String title,
        String author,
        String difficulty,
        Set<String> categories
) {
}
