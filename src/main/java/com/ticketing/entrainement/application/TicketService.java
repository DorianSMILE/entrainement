package com.ticketing.entrainement.application;

import com.ticketing.entrainement.api.TicketDuplicateResult;
import com.ticketing.entrainement.application.ports.TicketDuplicateCheckerPort;
import com.ticketing.entrainement.application.ports.TicketRepositoryPort;
import com.ticketing.entrainement.commun.exception.NotFoundException;
import com.ticketing.entrainement.domain.Ticket;
import com.ticketing.entrainement.domain.TicketPriority;
import com.ticketing.entrainement.domain.TicketStatus;
import com.ticketing.entrainement.domain.exception.DuplicateTicketException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TicketService {
    private final TicketRepositoryPort port;
    private final TicketDuplicateCheckerPort duplicateChecker;

    public TicketService(TicketRepositoryPort port, TicketDuplicateCheckerPort duplicateChecker) {
        this.port = port;
        this.duplicateChecker = duplicateChecker;
    }

    @Transactional
    public Ticket create(String title, String description, TicketPriority priority) {

        String normalizedTitle = com.ticketing.entrainement.domain.TicketText.normalize(title);

        var duplicates = duplicateChecker.findDuplicateIdsByNormalizedTitle(normalizedTitle, 5);
        if (!duplicates.isEmpty()) {
            throw new DuplicateTicketException(
                    "Duplicate ticket detected (same normalized title).",
                    duplicates,
                    null);
        }

        double threshold = 0.55;
        var fuzzy = duplicateChecker.findFuzzyDuplicates(title, threshold, 5);
        if (!fuzzy.isEmpty()) {
            throw new DuplicateTicketException("Duplicate ticket (fuzzy match).", List.of(), fuzzy);
        }


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
                updated = updated.rename(t);
            }
        }

        if (description != null && !description.equals(updated.description())) {
            updated = updated.changeDescription(description);
        }

        if (status != null && status != updated.status()) {
            updated = updated.changeStatus(status);
        }

        if (priority != null && priority != updated.priority()) {
            updated = updated.changePriority(priority);
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

    @Transactional(readOnly = true)
    public List<TicketDuplicateResult> findDuplicates(UUID id, double threshold, int limit) {
        Ticket base = port.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + id));

        String normalized = com.ticketing.entrainement.domain.TicketText.normalize(base.title());

        // EXACT (normalized)
        List<UUID> exactIds = duplicateChecker
                .findDuplicateIdsByNormalizedTitleExcluding(normalized, id, limit);

        // FUZZY (pg_trgm)
        List<DuplicateCandidate> fuzzy = duplicateChecker
                .findFuzzyDuplicatesExcluding(base.title(), id, threshold, limit);

        // index fuzzy score by id
        Map<UUID, Double> fuzzyScoreById = fuzzy.stream()
                .collect(java.util.stream.Collectors.toMap(
                        DuplicateCandidate::id,
                        DuplicateCandidate::score,
                        Math::max
                ));

        // merge ids unique (preserve order: exact first, then fuzzy)
        java.util.LinkedHashSet<UUID> ids = new java.util.LinkedHashSet<>();
        ids.addAll(exactIds);
        ids.addAll(fuzzyScoreById.keySet());

        if (ids.isEmpty()) return List.of();

        List<Ticket> tickets = port.findAllByIds(ids);

        // build results
        return tickets.stream()
                .map(t -> {
                    if (exactIds.contains(t.id())) {
                        return new TicketDuplicateResult(t, TicketDuplicateResult.MatchType.EXACT, null);
                    }
                    return new TicketDuplicateResult(t, TicketDuplicateResult.MatchType.FUZZY, fuzzyScoreById.get(t.id()));
                })
                .toList();
    }

}
