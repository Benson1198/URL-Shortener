package com.urlshortener.service;

import com.urlshortener.dto.AnalyticsSummaryResponse;
import com.urlshortener.dto.ClicksByDateResponse;
import com.urlshortener.dto.ClicksByDeviceResponse;
import com.urlshortener.exception.GlobalExceptionHandler;
import com.urlshortener.exception.UrlNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.urlshortener.model.ClickEvent;
import com.urlshortener.model.Url;
import com.urlshortener.repository.ClickEventRepository;
import com.urlshortener.repository.UrlRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final ClickEventRepository clickEventRepository;
    private final UrlRepository urlRepository;

    @Async("analyticsExecutor")
    public void recordClick(String shortCode, HttpServletRequest request) {

        try {
            String ipAddress = getClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            String referer = request.getHeader("Referer");
            String device = detectDevice(userAgent);

            ClickEvent event = ClickEvent.builder()
                    .shortCode(shortCode)
                    .clickedAt(LocalDateTime.now())
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .referer(referer)
                    .device(device)
                    .country("Unknown")
                    .build();

            clickEventRepository.save(event);
            log.debug("Recorded click for {} from {} ({})", shortCode, ipAddress, device);
        } catch (Exception e) {

            // Analytics failure should never affect the redirect
            log.error("Failed to record click for {}: {}", shortCode, e.getMessage());
        }
    }

    // Analytics query methods

    public AnalyticsSummaryResponse getSummary(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode).orElseThrow(() -> new UrlNotFoundException(shortCode));

        Long totalClicks = clickEventRepository.countByShortCode(shortCode);
        Long uniqueIps = clickEventRepository.countUniqueIps(shortCode);

        return AnalyticsSummaryResponse.builder()
                .shortCode(shortCode)
                .originalUrl(url.getOriginalUrl())
                .totalClicks(totalClicks)
                .uniqueIps(uniqueIps)
                .createdAt(url.getCreatedAt() != null ? url.getCreatedAt().toString() : null)
                .expiresAt(url.getExpiresAt() != null ? url.getExpiresAt().toString() : null)
                .build();
    }

    public List<ClicksByDateResponse> getClicksByDate(String shortCode, int days) {

        // Verify URL exists
        if (!urlRepository.existsByShortCode(shortCode)) {
            throw new UrlNotFoundException(shortCode);
        }

        LocalDateTime since = LocalDateTime.now().minusDays(days);

        List<Object[]> results = clickEventRepository.countClicksByDate(shortCode, since);

        return results.stream().map(row -> new ClicksByDateResponse(
                row[0].toString(), (Long) row[1])).collect(Collectors.toList());

    }

    public List<ClicksByDeviceResponse> getClicksByDevice(String shortCode) {
        // Verify URL exists
        if (!urlRepository.existsByShortCode(shortCode)) {
            throw new UrlNotFoundException(shortCode);
        }

        List<Object[]> results = clickEventRepository.countClicksByDevice(shortCode);

        return results.stream().map(row -> new ClicksByDeviceResponse(
                row[0] != null ? row[0].toString() : "unknown",
                (Long) row[1]))
                .collect(Collectors.toList());
    }

    // Extract real IP (handles proxies and load balancers)
    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim(); // first IP in chain
        }

        return request.getRemoteAddr();
    }

    // Simple device detection from User-Agent string
    private String detectDevice(String userAgent) {

        if (userAgent == null)
            return "unknown";
        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("mobile") || userAgent.contains("android")) {
            return "mobile";
        }

        if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
            return "tablet";
        }

        return "desktop";
    }
}
