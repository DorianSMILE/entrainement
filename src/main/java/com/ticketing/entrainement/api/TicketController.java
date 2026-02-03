package com.ticketing.entrainement.api;

import com.ticketing.entrainement.application.TicketService;
import com.ticketing.entrainement.domain.Ticket;
import com.ticketing.entrainement.domain.TicketPriority;
import com.ticketing.entrainement.domain.TicketStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @Operation(summary = "Create a ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ticket created"),
            @ApiResponse(responseCode = "400", description = "Invalid payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<TicketResponse> create(@RequestBody @Valid CreateTicketRequest req) {
        Ticket t = service.create(req.title(), req.description(), req.priority());
        TicketResponse body = toResponse(t);

        URI location = URI.create("/tickets/" + t.id());
        return ResponseEntity.created(location).body(body); // => 201 + Location
    }

    @Operation(summary = "Get a ticket by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket found"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @GetMapping("/{id}")
    public TicketResponse getById(@PathVariable UUID id) {
        Ticket t = service.getById(id);
        return toResponse(t);
    }

    @Operation(summary = "List tickets (filter + pagination)")
    @Parameter(in = ParameterIn.QUERY, name = "q", description = "Full-text search on title/description")
    @Parameter(in = ParameterIn.QUERY, name = "sort", description = "Example: sort=createdAt,desc or sort=priority,asc")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of tickets")
    })
    @GetMapping
    public Page<TicketResponse> list(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) String q,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return service.list(status, priority, q, pageable).map(this::toResponse);
    }

    @Operation(summary = "Update a ticket (partial)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket updated"),
            @ApiResponse(responseCode = "400", description = "Invalid payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Ticket not found"),
            @ApiResponse(responseCode = "409", description = "Business rule violation")
    })
    @PatchMapping("/{id}")
    public TicketResponse update(@PathVariable UUID id, @RequestBody @Valid PatchTicketRequest req) {
        Ticket t = service.update(id, req.title(), req.description(), req.status(), req.priority());
        return toResponse(t);
    }

    @Operation(summary = "Delete a ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ticket deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    private TicketResponse toResponse(Ticket t) {
        return new TicketResponse(
                t.id(),
                t.title(),
                t.description(),
                t.status(),
                t.priority(),
                t.createdAt(),
                t.updatedAt()
        );
    }

    @GetMapping("/{id}/duplicates")
    public List<TicketDuplicateResponse> duplicates(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0.55") double threshold,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return service.findDuplicates(id, threshold, limit).stream()
                .map(d -> new TicketDuplicateResponse(
                        toResponse(d.ticket()),
                        d.matchType(),
                        d.score()
                ))
                .toList();
    }

}
