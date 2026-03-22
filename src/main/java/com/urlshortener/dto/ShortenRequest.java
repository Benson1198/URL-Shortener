package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ShortenRequest {
    @NotBlank(message="URL cannot be blank")
    @Size(max=2048,message="URL too long")
    private String originalUrl;

    private String customAlias;
    private String expiresAt;

}
