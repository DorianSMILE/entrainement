package com.ticketing.entrainement.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TicketTest {

    @Test
    void closed_can_only_reopen() {
        Ticket closed = baseTicket(TicketStatus.CLOSED);

        Ticket reopened = closed.changeStatus(TicketStatus.REOPENED);
        assertEquals(TicketStatus.REOPENED, reopened.status());

        assertThrows(InvalidTicketStatusTransition.class,
                () -> closed.changeStatus(TicketStatus.IN_PROGRESS));
    }

    @Test
    void closed_cannot_rename_or_change_description_or_priority() {
        Ticket closed = baseTicket(TicketStatus.CLOSED);

        assertThrows(TicketClosedException.class, () -> closed.rename("new title"));
        assertThrows(TicketClosedException.class, () -> closed.changeDescription("new descr"));
        assertThrows(TicketClosedException.class, () -> closed.changePriority(TicketPriority.HIGH));
    }

    @Test
    void rename_trims_and_updates_updatedAt() {
        Ticket t = baseTicket(TicketStatus.OPEN);
        Instant before = t.updatedAt();

        Ticket renamed = t.rename("  hello  ");
        assertEquals("hello", renamed.title());
        assertTrue(renamed.updatedAt().isAfter(before) || !renamed.updatedAt().equals(before));
    }

    @Test
    void rename_blank_should_throw() {
        Ticket t = baseTicket(TicketStatus.OPEN);
        assertThrows(InvalidTicketTitleException.class, () -> t.rename("   "));
    }

    @Test
    void changeDescription_too_long_should_throw() {
        Ticket t = baseTicket(TicketStatus.OPEN);

        String tooLong = "a".repeat(10_001);
        assertThrows(InvalidTicketDescriptionException.class, () -> t.changeDescription(tooLong));
    }

    private Ticket baseTicket(TicketStatus status) {
        return new Ticket(
                UUID.randomUUID(),
                "title",
                "description",
                status,
                TicketPriority.MEDIUM,
                Instant.now().minusSeconds(60),
                Instant.now().minusSeconds(30)
        );
    }
}
