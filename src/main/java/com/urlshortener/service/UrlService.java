package com.urlshortener.service;

import com.urlshortener.model.Url;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {

    private final UrlRepository urlRepository;
    private final Base62Encoder base62Encoder;

    public Url shorten(String originalUrl) {
        Url url = Url.builder() // Save with empty short code first to get DB-generated ID
                .originalUrl(originalUrl)
                .shortCode("temp")
                .build();

        url = urlRepository.save(url);

        String shortCode = base62Encoder.encode(url.getId()); // Use the DB ID to generate short code

        url.setShortCode(shortCode); // Update the record with the real short code
        return urlRepository.save(url);
    }

    public Url resolve(String shortCode) {
        return urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("URL not found for shortCode: " + shortCode));
    }
}
