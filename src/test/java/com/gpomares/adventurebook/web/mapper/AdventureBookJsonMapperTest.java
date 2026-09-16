package com.gpomares.adventurebook.web.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpomares.adventurebook.dto.AdventureBookSummaryDto;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdventureBookJsonMapperTest {

    @Test
    void exposesOnlyThePublicBookFields() throws Exception {
        var dto = new AdventureBookSummaryDto(7L, "Book", "Author", "EASY", Set.of("fantasy"));
        var json = new ObjectMapper().writeValueAsString(new AdventureBookJsonMapper().mapSummary(dto));

        var node = new ObjectMapper().readTree(json);
        var fields = new HashSet<String>();
        node.fieldNames().forEachRemaining(fields::add);
        assertEquals(Set.of("id", "title", "author", "difficulty", "categories"), fields);
        assertEquals(7, node.get("id").asInt());
        assertEquals("Book", node.get("title").asText());
        assertEquals("Author", node.get("author").asText());
        assertEquals("EASY", node.get("difficulty").asText());
        assertEquals("fantasy", node.get("categories").get(0).asText());
    }
}
