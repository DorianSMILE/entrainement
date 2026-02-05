package com.ticketing.entrainement.application.ports;

import com.ticketing.entrainement.application.DuplicateCandidate;

import java.util.List;
import java.util.UUID;

public interface TicketDuplicateCheckerPort {
    List<UUID> findDuplicateIdsByNormalizedTitle(String normalizedTitle, int limit);

    List<UUID> findDuplicateIdsByNormalizedTitleExcluding(String normalizedTitle, UUID excludeId, int limit);

    List<DuplicateCandidate> findFuzzyDuplicates(String rawTitle, double threshold, int limit);

    List<DuplicateCandidate> findFuzzyDuplicatesExcluding(String rawTitle, UUID excludeId, double threshold, int limit);
}



