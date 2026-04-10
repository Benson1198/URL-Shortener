package com.urlshortener.config;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.urlshortener.dto.ErrorResponse;
import com.urlshortener.service.RateLimitService;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String ipAddress = getClientIp(request);
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // Pick the right bucket based on endpoint
        Bucket bucket = resolveBucket(ipAddress, uri, method);
        if (bucket == null)
            return true; // No rate limit for this endpoint

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // Add rate limit headers so clients know their quota
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true; // allow request
        }

        // Rate limit exceeded - return 429
        long waitSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitSeconds));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(429)
                .error("RATE_LIMIT_EXCEEDED")
                .message("Too many requests. Try again in " + waitSeconds + " seconds.")
                .timestamp(LocalDateTime.now().toString())
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));

        log.warn("Rate limit exceeded for IP: {} on {}", ipAddress, uri);
        return false; // block request
    }

    private Bucket resolveBucket(String ip, String uri, String method) {

        // Rate limit POST /shorten
        if ("POST".equals(method) && uri.contains("/shorten")) {
            return rateLimitService.getShortenBucket(ip);
        }

        // Rate limit GET redirects (/{code} pattern)
        if ("GET".equals(method) && uri.matches("/api/v1/urls/[^/]+$")) {
            return rateLimitService.getRedirectBucket(ip);
        }

        return null; // no rate limit on analytics, health, etc.
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
