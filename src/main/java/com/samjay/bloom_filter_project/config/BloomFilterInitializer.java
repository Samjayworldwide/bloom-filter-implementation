package com.samjay.bloom_filter_project.config;

import com.samjay.bloom_filter_project.bloom.RedisBloomFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import redis.clients.jedis.RedisClient;

import com.samjay.bloom_filter_project.service.UsernameServiceV2;

import static com.samjay.bloom_filter_project.utilities.Extensions.BLOOM_FILTER_KEY;

@Component
@RequiredArgsConstructor
@Slf4j
public class BloomFilterInitializer {

    private final RedisClient redisClient;

    private final UsernameServiceV2 usernameService;

    private final RedisBloomFilter redisBloomFilter;

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {

        boolean alreadyExists = redisClient.exists(BLOOM_FILTER_KEY);

        redisBloomFilter.createIfAbsent();

        if (!alreadyExists) {

            usernameService.loadAllIntoBloomFilter();

            log.info("Bloom filter created and loaded from database.");

        } else {

            log.info("Bloom filter already exists. Skipping database load.");

        }

    }

}