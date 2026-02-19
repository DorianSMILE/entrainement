package com.ticketing.entrainement.application;

import com.ticketing.entrainement.application.ports.TicketCleanupPort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class CleanupClosedTicketsUseCase {

    private final TicketCleanupPort cleanupPort;
    private final Duration defaultInactivity;

    public CleanupClosedTicketsUseCase(TicketCleanupPort cleanupPort, Duration defaultInactivity) {
        this.cleanupPort = cleanupPort;
        this.defaultInactivity = defaultInactivity;
    }

    public int execute() {
        return execute(defaultInactivity);
    }

    public int execute(Duration inactivityDuration) {
        Instant threshold = Instant.now().minus(inactivityDuration);
        return cleanupPort.deleteClosedNotModifiedSince(threshold);
    }
}