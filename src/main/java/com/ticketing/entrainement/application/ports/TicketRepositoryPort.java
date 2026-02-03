package com.ticketing.entrainement.application.ports;

import com.ticketing.entrainement.domain.Ticket;
import com.ticketing.entrainement.domain.TicketPriority;
import com.ticketing.entrainement.domain.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepositoryPort {
    Optional<Ticket> findById(UUID id);
    Page<Ticket> search(TicketStatus status, TicketPriority priority, String q, Pageable pageable);
    Ticket save(Ticket ticket);
    boolean existsById(UUID id);
    void deleteById(UUID id);
    List<Ticket> findAllByIds(Iterable<UUID> ids);
}
