package com.urlshortener.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.urlshortener.model.ClickEvent;

@Repository
public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {
    // Total clicks for a short code
    Long countByShortCode(String shortCode);

    // All clicks for a short code ordered by time
    List<ClickEvent> findByShortCodeOrderByClickedAtDesc(String shortCode);

    // Clicks after a certain date(for last 30 days queries)
    @Query("SELECT c FROM ClickEvent c WHERE c.shortCode = :code " +
            "AND c.clickedAt >= :since ORDER BY c.clickedAt DESC")
    List<ClickEvent> findByShortCodeSince(@Param("code") String shortCode, @Param("since") LocalDateTime since);

    // Count clicks grouped by device
    @Query("SELECT c.device, COUNT(c) FROM ClickEvent c WHERE c.shortCode = :code GROUP BY c.device")
    List<Object[]> countByDevice(@Param("code") String shortCode);
}
