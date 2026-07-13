package com.samjay.bloom_filter_project.service;

import com.samjay.bloom_filter_project.bloom.RedisBloomFilter;
import com.samjay.bloom_filter_project.dtos.UsernameCheckResult;
import com.samjay.bloom_filter_project.model.AppUser;
import com.samjay.bloom_filter_project.repository.AppUserRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Getter
@Slf4j
public class UsernameServiceV2 {

    private final AppUserRepository repository;

    private final RedisBloomFilter redisBloomFilter;

    public UsernameServiceV2(AppUserRepository repository, RedisBloomFilter redisBloomFilter) {

        this.repository = repository;

        this.redisBloomFilter = redisBloomFilter;

    }

    public void loadAllIntoBloomFilter() {

        List<String> usernames = repository.findAllUsernames();

        redisBloomFilter.addAll(usernames);

    }

    /**
     * Checks whether a username exists. The bloom filter answers "definitely
     * not taken" without ever touching the database. Only a "maybe" from the
     * filter triggers a real database read to confirm.
     */
    public UsernameCheckResult checkUsername(String username) {

        if (!redisBloomFilter.mightContain(username)) {

            return new UsernameCheckResult(false, "BLOOM_FILTER_DEFINITELY_NOT_TAKEN");

        }

        boolean actuallyExists = repository.existsByUsername(username);

        return new UsernameCheckResult(actuallyExists, actuallyExists ? "DATABASE_CONFIRMED_TAKEN" : "BLOOM_FILTER_FALSE_POSITIVE");

    }

    public AppUser registerUser(String firstName, String lastName, String username) {

        UsernameCheckResult check = checkUsername(username);

        if (check.exists()) {

            throw new IllegalStateException("Username already taken: " + username);

        }

        AppUser saved = repository.save(new AppUser(firstName, lastName, username));

        log.info("Saved user {}", username);

        redisBloomFilter.add(username);

        log.info("Added {} to bloom filter", username);

        log.info("Exists after add? {}", redisBloomFilter.mightContain(username));

        log.info("Bloom info {}", redisBloomFilter.info());

        return saved;

    }
}