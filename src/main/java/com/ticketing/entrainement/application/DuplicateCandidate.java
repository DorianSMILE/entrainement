package com.ticketing.entrainement.application;

import java.util.UUID;

public record DuplicateCandidate(UUID id, String title, double score) {}
