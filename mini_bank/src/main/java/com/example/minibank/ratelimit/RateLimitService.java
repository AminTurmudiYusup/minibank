package com.example.minibank.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private final List<RateLimitPolicy> policies = List.of(

            new RateLimitPolicy(
                    "login", "POST", "/api/auth/login",
                    5, Duration.ofMinutes(1)
            ),

            new RateLimitPolicy(
                    "register", "POST", "/api/auth/register",
                    3, Duration.ofMinutes(1)
            ),

            new RateLimitPolicy(
                    "transfer", "POST", "/api/transfers",
                    10, Duration.ofMinutes(1)
            )
    );

    public Optional<RateLimitPolicy> findPolicy(
            String method,
            String path
    ) {
        return policies.stream()
                .filter(policy ->
                        policy.httpMethod().equalsIgnoreCase(method)
                                && policy.path().equals(path))
                .findFirst();
    }

    public ConsumptionProbe consume(
            RateLimitPolicy policy,
            String clientId
    ) {
        String bucketKey = policy.name() + ":" + clientId;

        Bucket bucket = buckets.computeIfAbsent(
                bucketKey,
                key -> createBucket(policy)
        );

        return bucket.tryConsumeAndReturnRemaining(1);
    }

    private Bucket createBucket(RateLimitPolicy policy) {
        return Bucket.builder()
                .addLimit(limit -> limit
                        .capacity(policy.capacity())
                        .refillGreedy(
                                policy.capacity(),
                                policy.refillDuration()
                        )
                )
                .build();
    }
}