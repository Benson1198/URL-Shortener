package com.urlshortener.service;

import com.urlshortener.exception.GlobalExceptionHandler;
import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.urlshortener.model.ClickEvent;
import com.urlshortener.repository.ClickEventRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final ClickEventRepository clickEventRepository;

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
