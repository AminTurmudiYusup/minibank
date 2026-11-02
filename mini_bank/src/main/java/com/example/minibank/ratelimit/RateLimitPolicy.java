package com.example.minibank.ratelimit;

import java.time.Duration;

public record RateLimitPolicy(
        String name,
        String httpMethod,
        String path,
        long capacity,
        Duration refillDuration
) {}
