package com.ticketing.entrainement.application;

import com.ticketing.entrainement.application.ports.TicketRepositoryPort;
import com.ticketing.entrainement.domain.Ticket;
import com.ticketing.entrainement.domain.TicketHierarchyService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AttachTicketToParentUseCase {

    private final TicketRepositoryPort repository;
    private final TicketHierarchyService ticketHierarchyService;

    public AttachTicketToParentUseCase(
            TicketRepositoryPort repository,
            TicketHierarchyService ticketHierarchyService
    ) {
        this.repository = repository;
        this.ticketHierarchyService = ticketHierarchyService;
    }

    public void execute(UUID ticketId, UUID parentId) {
        Ticket ticket = repository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        ticketHierarchyService.validateNoCycle(
                ticket.id(),
                parentId,
                repository::findParentId
        );

        Ticket updated = ticket.attachToParent(parentId);
        repository.save(updated);
    }
}