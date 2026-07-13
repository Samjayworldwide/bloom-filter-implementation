package com.samjay.bloom_filter_project.bloom;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.bloom.BFReserveParams;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisBloomFilter {

    private final RedisClient redisClient;

    private final String key;

    private final double errorRate;

    private final long capacity;

    private final int expansion;

    public void createIfAbsent() {

        if (!redisClient.exists(key)) {

            redisClient.bfReserve(
                    key,
                    errorRate,
                    capacity,
                    BFReserveParams.reserveParams().expansion(expansion)
            );

        }
    }

    public void add(String item) {

        log.info("Adding username to bloom filter: {}", item);

        boolean added = redisClient.bfAdd(key, item);

        log.info("BF.ADD {} -> {}", item, added);

    }

    public void addAll(List<String> items) {

        if (items == null || items.isEmpty()) {

            return;

        }

        redisClient.bfMAdd(key, items.toArray(new String[0]));

    }

    public boolean mightContain(String item) {

        return redisClient.bfExists(key, item);

    }

    public Map<String, Object> info() {


        return redisClient.bfInfo(key);

    }

}
