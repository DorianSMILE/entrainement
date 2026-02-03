package com.ticketing.entrainement.infrastructure.adapter;

import com.ticketing.entrainement.application.DuplicateCandidate;
import com.ticketing.entrainement.application.ports.TicketDuplicateCheckerPort;
import com.ticketing.entrainement.infrastructure.TicketEntity;
import com.ticketing.entrainement.infrastructure.TicketJpaRepository;
import org.springframework.data.domain.PageRequest;
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

    @Override
    public List<DuplicateCandidate> findFuzzyDuplicates(String rawTitle, double threshold, int limit) {
        var rows = repo.findSimilarTitles(rawTitle, threshold, PageRequest.of(0, limit));
        return rows.stream()
                .map(r -> new DuplicateCandidate(r.getId(), r.getTitle(), r.getScore()))
                .toList();
    }
}
