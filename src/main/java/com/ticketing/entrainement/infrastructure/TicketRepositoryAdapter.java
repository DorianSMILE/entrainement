package com.ticketing.entrainement.infrastructure;

import com.ticketing.entrainement.application.ports.TicketRepositoryPort;
import com.ticketing.entrainement.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class TicketRepositoryAdapter implements TicketRepositoryPort {

    private final TicketJpaRepository repo;
    private final TicketEntityMapper mapper;

    public TicketRepositoryAdapter(TicketJpaRepository repo, TicketEntityMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public Optional<Ticket> findById(UUID id) {
        return repo.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Ticket> search(TicketStatus status, TicketPriority priority, String q, Pageable pageable) {
        Specification<TicketEntity> spec = TicketSpecifications.hasStatus(status)
                .and(TicketSpecifications.hasPriority(priority))
                .and(TicketSpecifications.textSearch(q));
        return repo.findAll(spec, pageable).map(mapper::toDomain);
    }

    @Override
    public Ticket save(Ticket ticket) {
        TicketEntity saved = repo.save(mapper.toEntity(ticket));
        return mapper.toDomain(saved);
    }

    @Override
    public boolean existsById(UUID id) {
        return repo.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        repo.deleteById(id);
    }
}
