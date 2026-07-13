package com.samjay.bloom_filter_project.dtos;

public record StatsResponse(int bitArraySize,
                            int hashFunctionCount,
                            int expectedInsertions,
                            int currentCount,
                            double targetFalsePositiveRate) {
}