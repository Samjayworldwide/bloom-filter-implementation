package com.samjay.bloom_filter_project.web;


import com.samjay.bloom_filter_project.bloom.DynamicBloomFilter;
import com.samjay.bloom_filter_project.dtos.CheckResponse;
import com.samjay.bloom_filter_project.dtos.RegisterRequest;
import com.samjay.bloom_filter_project.dtos.StatsResponse;
import com.samjay.bloom_filter_project.dtos.UsernameCheckResult;
import com.samjay.bloom_filter_project.model.AppUser;
import com.samjay.bloom_filter_project.service.UsernameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@SuppressWarnings("NullableProblems")
@RestController
@RequestMapping("/api/v1/usernames")
@RequiredArgsConstructor
public class UsernameController {

    private final UsernameService usernameService;

    @GetMapping("/check")
    public ResponseEntity<CheckResponse> check(@RequestParam String username) {

        UsernameCheckResult result = usernameService.checkUsername(username);

        return ResponseEntity.ok(new CheckResponse(username, result.exists(), result.reason()));

    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            AppUser saved = usernameService.registerUser(request.firstName(), request.lastName(), request.username());
            return ResponseEntity.ok(saved);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    // GET /api/usernames/stats -> watch m and k grow as you register more users
    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> stats() {

        DynamicBloomFilter bf = usernameService.getBloomFilter();

        return ResponseEntity.ok(new StatsResponse(
                bf.getBitArraySize(),
                bf.getHashFunctionCount(),
                bf.getExpectedInsertions(),
                bf.getCount(),
                bf.getFalsePositiveRate())
        );
    }
}
