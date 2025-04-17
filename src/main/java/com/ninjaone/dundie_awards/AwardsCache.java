package com.ninjaone.dundie_awards;

import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
public class AwardsCache {
    private static final String TOTAL_AWARDS_KEY = "total_awards";
    private static final Duration TOTAL_AWARDS_TTL = Duration.ofSeconds(300);

    private volatile boolean isMarkedToInvalidate = true;
    private final StringRedisTemplate redisTemplate;
    private final EmployeeRepository employeeRepository;

    public AwardsCache(StringRedisTemplate redisTemplate,
                       EmployeeRepository employeeRepository) {
        this.redisTemplate = redisTemplate;
        this.employeeRepository = employeeRepository;
    }

    public synchronized void invalidateCache() {
        isMarkedToInvalidate = true;
    }

    /**
     * Best-effort cache update
     */
    public void incrementAwards(long awardsToAdd) {
        try {
            Long total = redisTemplate.opsForValue().increment(TOTAL_AWARDS_KEY, awardsToAdd);
            log.info("AwardsCache.addAwards {} new total: {}", awardsToAdd, total);
            redisTemplate.expire(TOTAL_AWARDS_KEY, TOTAL_AWARDS_TTL);
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            log.error("AwardsCache.addAwards Redis connection error: {}", e.getMessage());
            invalidateCache();
        }
    }

    /**
     * Best-effort cache update
     */
    public void decrementAwards(long awardsToSubtract) {
        try {
            Long total = redisTemplate.opsForValue().decrement(TOTAL_AWARDS_KEY, awardsToSubtract);
            log.info("AwardsCache.decrementAwards {} new total: {}", awardsToSubtract, total);
            redisTemplate.expire(TOTAL_AWARDS_KEY, TOTAL_AWARDS_TTL);
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            log.error("AwardsCache.decrementAwards Redis connection error: {}", e.getMessage());
            invalidateCache();
        }
    }

    public long getTotalAwards() {
        String cachedValue = getValue();
        long total = 0L;
        if (cachedValue != null) {
            total = Long.parseLong(cachedValue);
            log.info("AwardsCache.getTotalAwards (cache hit) {}", total);
        } else {
            Long databaseTotal = employeeRepository.findTotalAwards();
            total = Optional.ofNullable(databaseTotal).orElse(0L);
            log.warn("AwardsCache.getTotalAwards (cache miss, fetched from db) {}", total);
            this.setValue(total);
        }
        return total;
    }

    private String getValue() {
        try {
            String value = redisTemplate.opsForValue().get(TOTAL_AWARDS_KEY);
            synchronized (this) {
                if (isMarkedToInvalidate) {
                    if(value != null) {
                        log.warn("Invalidating cache");
                        redisTemplate.delete(TOTAL_AWARDS_KEY);
                    }
                    isMarkedToInvalidate = false;
                    return null;
                }
            }
            return value;
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            log.error("AwardsCache.getValue Redis connection error: {}", e.getMessage());
            return null;
        }
    }

    private void setValue(long totalAwards) {
        try {
            log.info("AwardsCache.setValue {}", totalAwards);
            redisTemplate.opsForValue().set(TOTAL_AWARDS_KEY, String.valueOf(totalAwards), TOTAL_AWARDS_TTL);
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            log.error("AwardsCache.setValue Redis connection error: {}", e.getMessage());
            invalidateCache();
        }
    }
}
