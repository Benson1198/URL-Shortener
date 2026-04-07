package com.urlshortener.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.urlshortener.dto.AnalyticsSummaryResponse;
import com.urlshortener.dto.ClicksByDateResponse;
import com.urlshortener.dto.ClicksByDeviceResponse;
import com.urlshortener.service.AnalyticsService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    // GET /api/v1/analytics/{code}
    // Returns total clicks, unique IPs, URL metadata
    @GetMapping("/{shortCode}")
    public ResponseEntity<AnalyticsSummaryResponse> getSummary(@PathVariable String shortCode) {

        return ResponseEntity.ok(analyticsService.getSummary(shortCode));
    }

    // GET /api/v1/analytics/{code}/by-date?days=30
    // Returns clicks per day for last N days (default 30)
    @GetMapping("/{shortCode}/by-date")
    public ResponseEntity<List<ClicksByDateResponse>> getByDate(
            @PathVariable String shortCode, @RequestParam(defaultValue = "30") int days) {

        return ResponseEntity.ok(analyticsService.getClicksByDate(shortCode, days));
    }

    // GET /api/v1/analytics/{code}/by-device
    // Returns breakdown of mobile vs desktop vs tablet
    @GetMapping("/{shortCode}/by-device")
    public ResponseEntity<List<ClicksByDeviceResponse>> getByDevice(
            @PathVariable String shortCode) {
        return ResponseEntity.ok(
                analyticsService.getClicksByDevice(shortCode));
    }
}
