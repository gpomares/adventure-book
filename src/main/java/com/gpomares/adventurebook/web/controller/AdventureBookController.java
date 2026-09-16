package com.gpomares.adventurebook.web.controller;

import com.gpomares.adventurebook.application.AdventureBookService;
import com.gpomares.adventurebook.web.json.AdventureBook;
import com.gpomares.adventurebook.web.mapper.AdventureBookJsonMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdventureBookController {

    private final AdventureBookService service;
    private final AdventureBookJsonMapper mapper;

    @Autowired
    public AdventureBookController(AdventureBookService service, AdventureBookJsonMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping("/api/adventure-books/{id}")
    public AdventureBook get(@PathVariable Long id) {
        return mapper.map(service.get(id));
    }
}
