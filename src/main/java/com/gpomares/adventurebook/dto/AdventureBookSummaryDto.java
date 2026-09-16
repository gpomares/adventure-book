package com.gpomares.adventurebook.dto;

import java.util.List;
import java.util.Set;

public record AdventureBookSummaryDto(Long id,
                                      String title,
                                      String author,
                                      String difficulty,
                                      Set<String> categories) {
}
