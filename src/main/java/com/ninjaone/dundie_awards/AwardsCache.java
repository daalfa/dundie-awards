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

    private boolean isMarkedToInvalidate = false;
    private final StringRedisTemplate redisTemplate;
    private final EmployeeRepository employeeRepository;

    public AwardsCache(StringRedisTemplate redisTemplate,
                       EmployeeRepository employeeRepository) {
        this.redisTemplate = redisTemplate;
        this.employeeRepository = employeeRepository;
    }

    public void addAwards(long awardsToAdd) {
        try {
            Long total = redisTemplate.opsForValue().increment(TOTAL_AWARDS_KEY, awardsToAdd);
            log.info("AwardsCache.addAwards {} total: {}", awardsToAdd, total);
            redisTemplate.expire(TOTAL_AWARDS_KEY, TOTAL_AWARDS_TTL);
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            log.error("AwardsCache.addAwards Redis connection error: {}", e.getMessage());
            synchronized (this) {
                isMarkedToInvalidate = true;
            }
        }
    }

    public void setTotalAwards(long totalAwards) {
        try {
            log.info("AwardsCache.setTotalAwards {}", totalAwards);
            redisTemplate.opsForValue().set(TOTAL_AWARDS_KEY, String.valueOf(totalAwards), TOTAL_AWARDS_TTL);
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            log.error("AwardsCache.setTotalAwards Redis connection error: {}", e.getMessage());
            synchronized (this) {
                isMarkedToInvalidate = true;
            }
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
            this.setTotalAwards(total);
        }
        return total;
    }

    private String getValue() {
        try {
            String value = redisTemplate.opsForValue().get(TOTAL_AWARDS_KEY);
            synchronized (this) {
                if (isMarkedToInvalidate) {
                    log.warn("Invalidating cache");
                    redisTemplate.delete(TOTAL_AWARDS_KEY);
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
}
