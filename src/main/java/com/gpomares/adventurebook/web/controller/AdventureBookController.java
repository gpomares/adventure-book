package com.gpomares.adventurebook.web.controller;

import com.gpomares.adventurebook.application.AdventureBookService;
import com.gpomares.adventurebook.domain.AdventureBookFilter;
import com.gpomares.adventurebook.domain.Difficulty;
import com.gpomares.adventurebook.exception.InvalidAdventureBookException;
import com.gpomares.adventurebook.web.json.AdventureBookSummary;
import com.gpomares.adventurebook.web.json.CategoryRequest;
import com.gpomares.adventurebook.web.mapper.AdventureBookJsonMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

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

    @GetMapping("/api/adventure-books")
    @Operation(summary = "List adventure books", description = "Lists paginated adventure book summaries with optional filters.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated adventure book summaries"),
            @ApiResponse(responseCode = "400", description = "Invalid difficulty", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Page<AdventureBookSummary>> list(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Difficulty difficulty,
            @ParameterObject @PageableDefault(page = 0, size = 20, sort = "title", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return ResponseEntity.ok(service.search(new AdventureBookFilter(title, author, category, difficulty), pageable)
                .map(mapper::mapSummary));
    }

    @PostMapping("/api/adventure-books/{id}/categories")
    @Operation(summary = "Add a category", description = "Adds a trimmed, case-sensitive category. An existing category is a no-op.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category added"),
            @ApiResponse(responseCode = "204", description = "Category already exists"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or category", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Adventure book not found", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> addCategory(
            @PathVariable Long id,
            @RequestBody(required = true) @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true, description = "The category name to add",
                    content = @Content(schema = @Schema(implementation = CategoryRequest.class))) CategoryRequest request) {
        if (request == null || request.name() == null) {
            throw new InvalidAdventureBookException("Category must not be blank");
        }
        boolean added = service.addCategory(id, request.name());
        if (!added) {
            return ResponseEntity.noContent().build();
        }
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .pathSegment(request.name().trim()).build().encode().toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/api/adventure-books/{id}/categories")
    @Operation(summary = "Replace categories", description = "Replaces the complete category set after trimming names, collapsing normalized duplicates, and preserving case sensitivity. An empty array clears all categories.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categories replaced"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or category", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Adventure book not found", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> replaceCategories(@PathVariable Long id,
                                                  @RequestBody(required = true) @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                          required = true, description = "The complete replacement category array; use [] to clear all categories",
                                                          content = @Content(schema = @Schema(type = "array", implementation = String.class))) List<String> categories) {
        service.replaceCategories(id, categories);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/adventure-books/{id}/categories/{category}")
    @Operation(summary = "Remove a category", description = "Removes a trimmed, case-sensitive category. Removing an absent category is a no-op. Category values containing a slash cannot be routed by this path template.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category removed or already absent"),
            @ApiResponse(responseCode = "400", description = "Invalid category path value", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Adventure book not found", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> removeCategory(@PathVariable Long id, @PathVariable String category) {
        service.removeCategory(id, category);
        return ResponseEntity.noContent().build();
    }
}
