package com.ticketing.entrainement.domain;

import java.util.UUID;

@FunctionalInterface
public interface ChildrenCounter {
    long countNotClosed(UUID parentId);
}