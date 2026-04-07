package com.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClicksByDeviceResponse {

    private String device;
    private Long clicks;
}
