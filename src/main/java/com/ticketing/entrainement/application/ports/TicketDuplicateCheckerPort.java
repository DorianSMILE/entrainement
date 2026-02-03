package com.ticketing.entrainement.application.ports;

import java.util.List;
import java.util.UUID;

public interface TicketDuplicateCheckerPort {
    List<UUID> findDuplicateIdsByNormalizedTitle(String normalizedTitle, int limit);
}



