package com.urlshortener.service;

import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // Shorten: stricter — 10 requests per minute
    private Bucket createShortenBucket() {
        return Bucket.builder()
                .addLimit(limit -> limit
                        .capacity(10)
                        .refillGreedy(10, Duration.ofMinutes(1)))
                .build();
    }

    // Redirect: lenient — 100 requests per minute
    private Bucket createRedirectBucket() {
        return Bucket.builder()
                .addLimit(limit -> limit
                        .capacity(100)
                        .refillGreedy(100, Duration.ofMinutes(1)))
                .build();
    }

    public Bucket getShortenBucket(String ipAddress) {
        return buckets.computeIfAbsent(
                "shorten:" + ipAddress,
                k -> createShortenBucket());
    }

    public Bucket getRedirectBucket(String ipAddress) {
        return buckets.computeIfAbsent(
                "redirect:" + ipAddress,
                k -> createRedirectBucket());
    }
}