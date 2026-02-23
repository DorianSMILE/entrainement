package com.ticketing.entrainement.application;

import com.ticketing.entrainement.application.ports.TicketRepositoryPort;
import com.ticketing.entrainement.domain.Ticket;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DetachTicketFromParentUseCase {

    private final TicketRepositoryPort repository;

    public DetachTicketFromParentUseCase(TicketRepositoryPort repository) {
        this.repository = repository;
    }

    public void execute(UUID ticketId) {
        Ticket ticket = repository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        Ticket updated = ticket.detachFromParent();
        repository.save(updated);
    }
}