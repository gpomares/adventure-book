package com.gpomares.adventurebook.web.json;

import java.util.List;

public record PlayableSection(long id, String text, String type, List<PlayableOption> options) {
}
