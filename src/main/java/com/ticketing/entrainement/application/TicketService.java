package com.ticketing.entrainement.application;

import com.ticketing.entrainement.commun.NotFoundException;
import com.ticketing.entrainement.domain.Ticket;
import com.ticketing.entrainement.domain.TicketPriority;
import com.ticketing.entrainement.domain.TicketStatus;
import com.ticketing.entrainement.infrastructure.TicketEntity;
import com.ticketing.entrainement.infrastructure.TicketEntityMapper;
import com.ticketing.entrainement.infrastructure.TicketJpaRepository;
import com.ticketing.entrainement.infrastructure.TicketSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class TicketService {
    private final TicketJpaRepository repo;
    private final TicketEntityMapper mapper;

    public TicketService(TicketJpaRepository repo, TicketEntityMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Transactional
    public Ticket create(String title, String description, TicketPriority priority) {
        Instant now = Instant.now();
        Ticket ticket = new Ticket(UUID.randomUUID(), title, description,
                TicketStatus.OPEN, priority, now, now);

        TicketEntity saved = repo.save(mapper.toEntity(ticket));
        return mapper.toDomain(saved);
    }

    @Transactional(readOnly = true)
    public Ticket getById(UUID id) {
        TicketEntity entity = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + id));
        return mapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public Page<Ticket> list(TicketStatus status, TicketPriority priority, String q, Pageable pageable) {

        Specification<TicketEntity> spec = TicketSpecifications.hasStatus(status)
                .and(TicketSpecifications.hasPriority(priority))
                .and(TicketSpecifications.textSearch(q));

        return repo.findAll(spec, pageable).map(mapper::toDomain);
    }

    @Transactional
    public Ticket update(UUID id, String title, String description, TicketStatus status, TicketPriority priority) {
        TicketEntity entity = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + id));

        boolean changed = false;

        if (title != null) {
            String t = title.trim();
            if (!t.isEmpty() && !t.equals(entity.getTitle())) {
                entity.setTitle(t);
                changed = true;
            }
        }

        if (description != null && !description.equals(entity.getDescription())) {
            entity.setDescription(description);
            changed = true;
        }

        if (status != null && status != entity.getStatus()) {
            entity.setStatus(status);
            changed = true;
        }

        if (priority != null && priority != entity.getPriority()) {
            entity.setPriority(priority);
            changed = true;
        }

        if (changed) {
            entity.setUpdatedAt(Instant.now());
        }

        TicketEntity saved = repo.save(entity);
        return mapper.toDomain(saved);
    }

    @Transactional
    public void delete(UUID id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException("Ticket not found: " + id);
        }
        repo.deleteById(id);
    }


}
