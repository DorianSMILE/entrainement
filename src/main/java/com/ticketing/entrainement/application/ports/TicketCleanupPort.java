package com.ticketing.entrainement.application.ports;

import java.time.Instant;

public interface TicketCleanupPort {
    int deleteClosedNotModifiedSince(Instant threshold);
}
