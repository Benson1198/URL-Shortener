package com.urlshortener.service;

import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.UrlResponse;
import com.urlshortener.exception.AliasAlreadyExistsException;
import com.urlshortener.exception.UrlExpiredException;
import com.urlshortener.exception.UrlNotFoundException;
import com.urlshortener.model.Url;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import javax.management.RuntimeErrorException;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {

    private final UrlRepository urlRepository;
    private final Base62Encoder base62Encoder;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String CACHE_PREFIX = "url:";
    private static final long DEFAULT_TTL_HOURS = 1;

    public UrlResponse shorten(ShortenRequest request) {
        // Check if custom alias is already taken
        if (request.getCustomAlias() != null && !request.getCustomAlias().isBlank()) {
            if (urlRepository.existsByCustomAlias(request.getCustomAlias())) {
                throw new AliasAlreadyExistsException(request.getCustomAlias());
            }
        }

        // Parse expiresAt if provided
        LocalDateTime expiresAt = null;
        if (request.getExpiresAt() != null && !request.getExpiresAt().isBlank()) {
            expiresAt = LocalDateTime.parse(request.getExpiresAt()); // expects "2026-04-01T00:00:00"
        }

        // Save with temp code to get the DB-generated Id
        Url url = Url.builder()
                .originalUrl(request.getOriginalUrl())
                .shortCode("temp")
                .customAlias(request.getCustomAlias())
                .expiresAt(expiresAt)
                .build();
        url = urlRepository.save(url);

        // Generate short code from DB Id (or use custom alias)
        String shortCode = (request.getCustomAlias() != null &&
                !request.getCustomAlias().isBlank())
                        ? request.getCustomAlias()
                        : base62Encoder.encode(url.getId());

        // Update with real shortCode
        url.setShortCode(shortCode);
        url = urlRepository.save(url);

        log.info("Shortened URL: {} -> {}", request.getOriginalUrl(), shortCode);
        return toResponse(url);

    }

    public String resolve(String shortCode) {
        // Check Redis cache first
        String cacheKey = CACHE_PREFIX + shortCode;
        String cachedUrl = redisTemplate.opsForValue().get(cacheKey);

        if (cachedUrl != null) {

            log.info("Cache HIT for: {}", shortCode);
            return cachedUrl;
        }

        // Cache miss - hit the database
        log.info("Cache MISS for: {}", shortCode);
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        // Check if expired
        if (url.getExpiresAt() != null &&
                url.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UrlExpiredException(shortCode);
        }

        long ttlSeconds = calculateTtl(url);
        redisTemplate.opsForValue().set(
                cacheKey,
                url.getOriginalUrl(),
                ttlSeconds,
                TimeUnit.SECONDS);

        log.info("Cached {} with TTL {} seconds", shortCode, ttlSeconds);

        // Increment click count
        url.setClickCount(url.getClickCount() + 1);
        urlRepository.save(url);

        return url.getOriginalUrl();
    }

    public void deleteUrl(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        // Evict from cache
        String cacheKey = CACHE_PREFIX + shortCode;
        redisTemplate.delete(cacheKey);
        log.info("Evicted cache for: {}", shortCode);

        urlRepository.delete(url);
        log.info("Deleted URL: {}", shortCode);
    }

    public UrlResponse getInfo(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        return toResponse(url);
    }

    // Calculate TTL - aligns with URL expiry or defaults to 1 hour
    private long calculateTtl(Url url) {
        if (url.getExpiresAt() == null) {
            return TimeUnit.HOURS.toSeconds(DEFAULT_TTL_HOURS); // 3600 seconds
        }
        long secondsUntilExpiry = Duration.between(LocalDateTime.now(), url.getExpiresAt()).getSeconds();

        // If expiry is in the past, use very short TTL
        return Math.max(secondsUntilExpiry, 1);
    }

    private UrlResponse toResponse(Url url) {
        return UrlResponse.builder()
                .shortCode(url.getShortCode())
                .originalUrl(url.getOriginalUrl())
                .shortUrl("http://localhost:8080/api/v1/urls/" + url.getShortCode())
                .clickCount(url.getClickCount())
                .createdAt(url.getCreatedAt() != null
                        ? url.getCreatedAt().toString()
                        : null)
                .build();
    }
}
