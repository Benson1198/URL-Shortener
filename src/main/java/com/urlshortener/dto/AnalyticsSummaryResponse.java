package com.urlshortener.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnalyticsSummaryResponse {

    private String shortCode;
    private String originalUrl;
    private Long totalClicks;
    private Long uniqueIps;
    private String createdAt;
    private String expiresAt;
}
