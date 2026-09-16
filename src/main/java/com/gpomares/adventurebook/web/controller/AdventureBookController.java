package com.gpomares.adventurebook.web.controller;

import com.gpomares.adventurebook.application.AdventureBookService;
import com.gpomares.adventurebook.web.json.AdventureBookSummary;
import com.gpomares.adventurebook.web.mapper.AdventureBookJsonMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Adventure books", description = "Adventure book lookup operations")
public class AdventureBookController {

    private final AdventureBookService service;
    private final AdventureBookJsonMapper mapper;

    @Autowired
    public AdventureBookController(AdventureBookService service, AdventureBookJsonMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping("/api/adventure-books/{id}")
    @Operation(summary = "Get an adventure book summary", description = "Returns an adventure book summary by its identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Adventure book found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AdventureBookSummary.class))),
            @ApiResponse(responseCode = "400", description = "The identifier is not a valid number or the adventure book is invalid", content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Adventure book not found", content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server failure", content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<AdventureBookSummary> get(@Parameter(
            in = ParameterIn.PATH,
            description = "The adventure book identifier",
            required = true,
            example = "7") @PathVariable Long id) {
        return ResponseEntity.ok(mapper.mapSummary(service.get(id)));
    }
}
