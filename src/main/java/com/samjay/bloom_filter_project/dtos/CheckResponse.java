package com.samjay.bloom_filter_project.dtos;

public record CheckResponse(String username, boolean exists, String reason) {
}