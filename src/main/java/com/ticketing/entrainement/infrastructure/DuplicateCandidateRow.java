package com.ticketing.entrainement.infrastructure;

import java.util.UUID;

public interface DuplicateCandidateRow {
    UUID getId();
    String getTitle();
    double getScore();
}
