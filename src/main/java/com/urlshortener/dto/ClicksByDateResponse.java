package com.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClicksByDateResponse {

    private String date;
    private Long clicks;
}
