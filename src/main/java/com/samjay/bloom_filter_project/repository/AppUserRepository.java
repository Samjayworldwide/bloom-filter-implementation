package com.samjay.bloom_filter_project.repository;

import com.samjay.bloom_filter_project.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@SuppressWarnings("NullableProblems")
@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    boolean existsByUsername(String username);

    // Used whenever the bloom filter needs to be rebuilt from the source of truth.
    @Query("select u.username from AppUser u")
    List<String> findAllUsernames();
}