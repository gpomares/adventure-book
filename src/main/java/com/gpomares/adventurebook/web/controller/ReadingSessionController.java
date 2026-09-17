package com.gpomares.adventurebook.web.controller;

import com.gpomares.adventurebook.application.ReadingSessionService;
import com.gpomares.adventurebook.dto.ReadingSessionDto;
import com.gpomares.adventurebook.security.AuthenticatedUserProvider;
import com.gpomares.adventurebook.web.json.ReadingSession;
import com.gpomares.adventurebook.web.mapper.ReadingSessionJsonMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@Tag(name = "Reading sessions", description = "Adventure book reading operations")
public class ReadingSessionController {

    private final ReadingSessionService service;
    private final ReadingSessionJsonMapper mapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public ReadingSessionController(ReadingSessionService service, ReadingSessionJsonMapper mapper,
                                    AuthenticatedUserProvider authenticatedUserProvider) {
        this.service = service;
        this.mapper = mapper;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @PostMapping("/api/adventure-books/{bookId}/reading-sessions")
    @Operation(summary = "Start a reading session", description = "Starts at the book's BEGIN section with health 10.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reading session created"),
            @ApiResponse(responseCode = "404", description = "Adventure book not found"),
            @ApiResponse(responseCode = "400", description = "Invalid adventure book")
    })
    public ResponseEntity<ReadingSession> start(@PathVariable Long bookId) {
        ReadingSessionDto state = service.start(bookId, authenticatedUserProvider.requireUserId());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .pathSegment(state.sessionId().toString())
                .build()
                .toUri();
        return ResponseEntity.created(location).body(mapper.map(state));
    }

    @PostMapping("/api/adventure-books/{bookId}/reading-sessions/{sessionId}/options/{optionId}")
    @Operation(summary = "Choose an option", description = "Applies its consequence and moves the reading session to the next section.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reading session advanced"),
            @ApiResponse(responseCode = "400", description = "Invalid path identifier"),
            @ApiResponse(responseCode = "404", description = "Adventure book or reading session not found"),
            @ApiResponse(responseCode = "409", description = "The session ended or the option is unavailable")
    })
    public ResponseEntity<ReadingSession> chooseOption(@PathVariable Long bookId,
                                                       @PathVariable Long sessionId,
                                                       @PathVariable Long optionId) {
        return ResponseEntity.ok(mapper.map(service.chooseOption(bookId, sessionId, optionId,
                authenticatedUserProvider.requireUserId())));
    }

    @GetMapping("/api/reading-sessions/{sessionId}")
    public ResponseEntity<ReadingSession> get(@PathVariable Long sessionId) {
        return ResponseEntity.ok(mapper.map(service.get(sessionId, authenticatedUserProvider.requireUserId())));
    }

    @GetMapping("/api/reading-sessions")
    public ResponseEntity<java.util.List<ReadingSession>> list(
            @RequestParam com.gpomares.adventurebook.domain.ReadingSessionStatus status) {
        return ResponseEntity.ok(service.list(authenticatedUserProvider.requireUserId(), status).stream()
                .map(mapper::map).toList());
    }
}
