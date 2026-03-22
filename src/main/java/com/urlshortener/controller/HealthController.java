package com.urlshortener.controller;

import com.urlshortener.model.Url;
import com.urlshortener.service.UrlService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.time.LocalDateTime;

@RestController
public class HealthController {

    @Autowired
    private UrlService urlService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "url-shortener",
                "timestamp", LocalDateTime.now().toString()));
    }

    // @PostMapping("/test-shorten")
    // public ResponseEntity<String> testShorten(@RequestBody String url){
    // Url shortened = urlService.shorten(url);
    // return ResponseEntity.ok("Short code: " + shortened.getShortCode());
    // }
}