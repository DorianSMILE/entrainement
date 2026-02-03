package com.ticketing.entrainement.application.ports;

import com.ticketing.entrainement.application.DuplicateCandidate;

import java.util.List;
import java.util.UUID;

public interface TicketDuplicateCheckerPort {
    List<UUID> findDuplicateIdsByNormalizedTitle(String normalizedTitle, int limit);
    List<DuplicateCandidate> findFuzzyDuplicates(String rawTitle, double threshold, int limit);
}



