package com.example.expense_tracker.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory rate limiting using Bucket4j token buckets.
 *
 * KNOWN LIMITATION: In-memory Bucket4j is single-node only.
 * If a multi-instance deployment is added later, this will need to move to a shared store
 * (e.g., Redis-backed buckets via bucket4j-redis or Lettuce/Jedis).
 */
@Component
public class RateLimitingService {

    @Value("${fintel.rate-limit.auth-limit:5}")
    private long authLimit;

    @Value("${fintel.rate-limit.ai-limit:20}")
    private long aiLimit;

    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> aiBuckets = new ConcurrentHashMap<>();

    private Bucket createBucket(long limit) {
        Bandwidth bandwidth = Bandwidth.classic(limit, Refill.intervally(limit, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(bandwidth).build();
    }

    public boolean tryConsumeAuth(String ipAddress) {
        Bucket bucket = authBuckets.computeIfAbsent(ipAddress, k -> createBucket(authLimit));
        return bucket.tryConsume(1);
    }

    public boolean tryConsumeAi(String userOrIpKey) {
        Bucket bucket = aiBuckets.computeIfAbsent(userOrIpKey, k -> createBucket(aiLimit));
        return bucket.tryConsume(1);
    }

    public void reset() {
        authBuckets.clear();
        aiBuckets.clear();
    }
}
