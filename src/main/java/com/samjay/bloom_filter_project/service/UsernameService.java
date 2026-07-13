package com.samjay.bloom_filter_project.service;

import com.samjay.bloom_filter_project.bloom.DynamicBloomFilter;
import com.samjay.bloom_filter_project.dtos.UsernameCheckResult;
import com.samjay.bloom_filter_project.model.AppUser;
import com.samjay.bloom_filter_project.repository.AppUserRepository;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Getter
public class UsernameService {

    private static final int INITIAL_CAPACITY = 20;

    private static final double FALSE_POSITIVE_RATE = 0.01;

    private final AppUserRepository repository;

    private final DynamicBloomFilter bloomFilter;

    public UsernameService(AppUserRepository repository) {

        this.repository = repository;

        this.bloomFilter = new DynamicBloomFilter(INITIAL_CAPACITY, FALSE_POSITIVE_RATE);

    }

    /**
     * Called on startup: rebuilds the bloom filter from every username currently in the DB.
     */
    public void loadAllIntoBloomFilter() {

        List<String> usernames = repository.findAllUsernames();

        bloomFilter.growAndRebuild(Math.max(INITIAL_CAPACITY, usernames.size()), usernames);

    }

    /**
     * Checks whether a username exists. The bloom filter answers "definitely
     * not taken" without ever touching the database. Only a "maybe" from the
     * filter triggers a real database read to confirm.
     */
    public UsernameCheckResult checkUsername(String username) {

        if (!bloomFilter.mightContain(username)) {

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

        bloomFilter.add(username);

        if (bloomFilter.needsResize()) {

            loadAllIntoBloomFilter();

        }

        return saved;

    }
}