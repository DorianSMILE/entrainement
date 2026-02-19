package com.ticketing.entrainement.infrastructure;

import com.ticketing.entrainement.application.ports.TicketCleanupPort;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public class JpaTicketCleanupAdapter implements TicketCleanupPort {

    private final TicketJpaRepository repo;

    public JpaTicketCleanupAdapter(TicketJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public int deleteClosedNotModifiedSince(Instant threshold) {
        return repo.deleteClosedNotModifiedSince(threshold);
    }
}