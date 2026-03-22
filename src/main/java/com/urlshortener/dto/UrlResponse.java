package com.urlshortener.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UrlResponse {

    private String shortCode;
    private String originalUrl;
    private String shortUrl;
    private Long clickCount;
    private String createdAt;

}
