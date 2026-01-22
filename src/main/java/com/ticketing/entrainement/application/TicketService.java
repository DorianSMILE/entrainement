package com.ticketing.entrainement.application;

import com.ticketing.entrainement.application.ports.TicketRepositoryPort;
import com.ticketing.entrainement.commun.NotFoundException;
import com.ticketing.entrainement.domain.Ticket;
import com.ticketing.entrainement.domain.TicketPriority;
import com.ticketing.entrainement.domain.TicketStatus;
import com.ticketing.entrainement.infrastructure.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class TicketService {
    private final TicketRepositoryPort port;

    public TicketService(TicketRepositoryPort port) {
        this.port = port;
    }

    @Transactional
    public Ticket create(String title, String description, TicketPriority priority) {
        Instant now = Instant.now();
        Ticket ticket = new Ticket(UUID.randomUUID(), title, description,
                TicketStatus.OPEN, priority, now, now);

        return port.save(ticket);
    }

    @Transactional(readOnly = true)
    public Ticket getById(UUID id) {
        return port.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Ticket> list(TicketStatus status, TicketPriority priority, String q, Pageable pageable) {
        return port.search(status, priority, q, pageable);
    }

    @Transactional
    public Ticket update(UUID id, String title, String description, TicketStatus status, TicketPriority priority) {
        Ticket ticket = port.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + id));

        Ticket updated = ticket;

        if (title != null) {
            String t = title.trim();
            if (!t.isEmpty() && !t.equals(updated.title())) {
                updated = updated.rename(t); // ou rename(t)
            }
        }

        if (description != null && !description.equals(updated.description())) {
            updated = updated.changeDescription(description); // ou changeDescription(...)
        }

        if (status != null && status != updated.status()) {
            updated = updated.changeStatus(status); // règle métier + copie
        }

        if (priority != null && priority != updated.priority()) {
            updated = updated.changePriority(priority); // ou changePriority(...)
        }

        if (updated.equals(ticket)) return ticket;
        return port.save(updated);
    }

    @Transactional
    public void delete(UUID id) {
        if (!port.existsById(id)) {
            throw new NotFoundException("Ticket not found: " + id);
        }
        port.deleteById(id);
    }

}
