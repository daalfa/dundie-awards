package com.ninjaone.dundie_awards;

import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class AwardsCache {
    private static final String TOTAL_AWARDS_KEY = "total_awards";

    private final StringRedisTemplate redisTemplate;

    private final EmployeeRepository employeeRepository;

    public AwardsCache(StringRedisTemplate redisTemplate,
                       EmployeeRepository employeeRepository) {
        this.redisTemplate = redisTemplate;
        this.employeeRepository = employeeRepository;
    }

    public void setTotalAwards(long totalAwards) {
        log.info("AwardsCache.setTotalAwards {}", totalAwards);
        redisTemplate.opsForValue().set(TOTAL_AWARDS_KEY, String.valueOf(totalAwards));
    }

    public long getTotalAwards() {
        String cachedValue = redisTemplate.opsForValue().get(TOTAL_AWARDS_KEY);
        long total = 0L;
        if (cachedValue != null) {
            total = Long.parseLong(cachedValue);
            log.info("AwardsCache.getTotalAwards (cache hit) {}", total);
        } else {
            Long databaseTotal = employeeRepository.findTotalAwards();
            total = Optional.ofNullable(databaseTotal).orElse(0L);
            redisTemplate.opsForValue().set(TOTAL_AWARDS_KEY, String.valueOf(total));
            log.info("AwardsCache.getTotalAwards (cache miss, fetched from db) {}", total);
        }
        return total;
    }

    public void addOneAward() {
        Long total = redisTemplate.opsForValue().increment(TOTAL_AWARDS_KEY);
        log.info("AwardsCache.addOneAward totaling {}", total);
    }

    public void addAwards(long awardsToAdd) {
        Long total = redisTemplate.opsForValue().increment(TOTAL_AWARDS_KEY, awardsToAdd);
        log.info("AwardsCache.addAwards {} new awards added totaling {}", awardsToAdd, total);
    }
}
