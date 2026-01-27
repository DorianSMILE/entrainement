package com.ticketing.entrainement.api;

import com.ticketing.entrainement.application.TicketService;
import com.ticketing.entrainement.domain.Ticket;
import com.ticketing.entrainement.domain.TicketPriority;
import com.ticketing.entrainement.domain.TicketStatus;
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
import java.util.UUID;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> create(@RequestBody @Valid CreateTicketRequest req) {
        Ticket t = service.create(req.title(), req.description(), req.priority());
        TicketResponse body = toResponse(t);

        URI location = URI.create("/tickets/" + t.id());
        return ResponseEntity.created(location).body(body); // => 201 + Location
    }

    @GetMapping("/{id}")
    public TicketResponse getById(@PathVariable UUID id) {
        Ticket t = service.getById(id);
        return toResponse(t);
    }

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

    @PatchMapping("/{id}")
    public TicketResponse update(@PathVariable UUID id, @RequestBody @Valid PatchTicketRequest req) {
        Ticket t = service.update(id, req.title(), req.description(), req.status(), req.priority());
        return toResponse(t);
    }

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
}
