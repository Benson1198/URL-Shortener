package com.urlshortener.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

@Service
public class RateLimitService {

    // One bucket per IP address stored in memory
    // private final Map<String , Bucket> buckets = new ConcurrentHashMap<>();

    //Shorten endpoint: stricter - 10 requests per minute
    // private Bucket createShortenBucket(){
    //     Bandwidth limit = Bandwidth.classic(0, null)
    // }
}
