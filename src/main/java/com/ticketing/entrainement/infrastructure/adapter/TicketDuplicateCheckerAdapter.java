package com.ticketing.entrainement.infrastructure.adapter;

import com.ticketing.entrainement.application.ports.TicketDuplicateCheckerPort;
import com.ticketing.entrainement.infrastructure.TicketEntity;
import com.ticketing.entrainement.infrastructure.TicketJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class TicketDuplicateCheckerAdapter implements TicketDuplicateCheckerPort {

    private final TicketJpaRepository repo;

    public TicketDuplicateCheckerAdapter(TicketJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<UUID> findDuplicateIdsByNormalizedTitle(String normalizedTitle, int limit) {
        return repo.findTop5ByNormalizedTitle(normalizedTitle)
                .stream()
                .map(TicketEntity::getId)
                .toList();
    }
}
