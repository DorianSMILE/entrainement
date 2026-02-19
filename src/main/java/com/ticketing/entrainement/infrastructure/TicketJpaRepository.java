package com.ticketing.entrainement.infrastructure;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TicketJpaRepository extends JpaRepository<TicketEntity, UUID>, JpaSpecificationExecutor<TicketEntity> {
    List<TicketEntity> findTop5ByNormalizedTitle(String normalizedTitle);

    @Query(value = """
    SELECT id, title, similarity(title, :title) AS score
    FROM tickets
    WHERE similarity(title, :title) >= :threshold
    ORDER BY score DESC
    """, nativeQuery = true)
    List<DuplicateCandidateRow> findSimilarTitles(
            @Param("title") String title,
            @Param("threshold") double threshold,
            Pageable pageable
    );

    List<TicketEntity> findTop10ByNormalizedTitleAndIdNot(String normalizedTitle, UUID id);

    @Query(value = """
    SELECT id, title, similarity(title, :title) AS score
    FROM tickets
    WHERE id <> :excludeId
      AND similarity(title, :title) >= :threshold
    ORDER BY score DESC
    """, nativeQuery = true)
    List<DuplicateCandidateRow> findSimilarTitlesExcluding(
            @Param("title") String title,
            @Param("excludeId") UUID excludeId,
            @Param("threshold") double threshold,
            Pageable pageable
    );

    @Modifying
    @Query("""
        delete from TicketEntity t
        where t.status = 'CLOSED'
          and t.updatedAt < :threshold
    """)
    int deleteClosedNotModifiedSince(@Param("threshold") Instant threshold);

}
