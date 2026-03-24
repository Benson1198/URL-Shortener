package com.urlshortener.service;

import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.UrlResponse;
import com.urlshortener.model.Url;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import javax.management.RuntimeErrorException;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {

    private final UrlRepository urlRepository;
    private final Base62Encoder base62Encoder;

    public UrlResponse shorten(ShortenRequest request) {
        // Check if custom alias is already taken
        if (request.getCustomAlias() != null && !request.getCustomAlias().isBlank()) {
            if (urlRepository.existsByCustomAlias(request.getCustomAlias())) {
                throw new RuntimeException("Alias already exists: " + request.getCustomAlias());
            }
        }

        // Save with temp code to get the DB-generated Id
        Url url = Url.builder()
                .originalUrl(request.getOriginalUrl())
                .shortCode("temp")
                .customAlias(request.getCustomAlias())
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

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("Url not found: " + shortCode));

        // Check if expired
        if (url.getExpiresAt() != null &&
                url.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("URL has expired: " + shortCode);
        }

        // Increment click count
        url.setClickCount(url.getClickCount() + 1);
        urlRepository.save(url);

        return url.getOriginalUrl();
    }

    public UrlResponse getInfo(String shortCode){
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> 
                new RuntimeException("URL not found: " + shortCode));

                return toResponse(url);
    }

    private UrlResponse toResponse(Url url){
        return UrlResponse.builder()
                .shortCode(url.getShortCode())
                .originalUrl(url.getOriginalUrl())
                .shortUrl("http://localhost/api/v1/urls/" + url.getShortCode())
                .clickCount(url.getClickCount())
                .createdAt(url.getCreatedAt() != null 
            ? url.getCreatedAt().toString() : null)
            .build();
    }
}
