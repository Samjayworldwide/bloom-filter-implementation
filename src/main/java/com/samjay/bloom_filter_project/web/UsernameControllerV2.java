package com.samjay.bloom_filter_project.web;

import com.samjay.bloom_filter_project.bloom.DynamicBloomFilter;
import com.samjay.bloom_filter_project.dtos.CheckResponse;
import com.samjay.bloom_filter_project.dtos.RegisterRequest;
import com.samjay.bloom_filter_project.dtos.UsernameCheckResult;
import com.samjay.bloom_filter_project.model.AppUser;
import com.samjay.bloom_filter_project.service.UsernameServiceV2;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@SuppressWarnings("NullableProblems")
@RestController
@RequestMapping("/api/v2/usernames")
@RequiredArgsConstructor
public class UsernameControllerV2 {

    private final UsernameServiceV2 usernameServiceV2;

    @GetMapping("/check")
    public ResponseEntity<CheckResponse> check(@RequestParam String username) {

        UsernameCheckResult result = usernameServiceV2.checkUsername(username);

        return ResponseEntity.ok(new CheckResponse(username, result.exists(), result.reason()));

    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        try {

            AppUser saved = usernameServiceV2.registerUser(request.firstName(), request.lastName(), request.username());

            return ResponseEntity.ok(saved);

        } catch (IllegalStateException e) {

            return ResponseEntity.status(409).body(e.getMessage());

        }
    }

    // GET /api/usernames/stats -> watch m and k grow as you register more users
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {


        var result = usernameServiceV2.getRedisBloomFilter().info();

        return ResponseEntity.ok(result);

    }
}
