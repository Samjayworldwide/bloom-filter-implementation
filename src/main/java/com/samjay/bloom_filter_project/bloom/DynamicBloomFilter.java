package com.samjay.bloom_filter_project.bloom;

import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Getter
@Setter
public class DynamicBloomFilter {

    private BitSet bits;

    private int m; // bit array size

    private int k; // number of hash functions

    private int expectedInsertions; // capacity this filter is currently sized for

    private int count; // how many items have actually been inserted

    private final double falsePositiveRate;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public DynamicBloomFilter(int expectedInsertions, double falsePositiveRate) {

        this.expectedInsertions = Math.max(expectedInsertions, 1);

        this.falsePositiveRate = falsePositiveRate;

        this.m = optimalM(this.expectedInsertions, falsePositiveRate);

        this.k = optimalK(this.m, this.expectedInsertions);

        this.bits = new BitSet(this.m);

        this.count = 0;

    }

    /**
     * Adds a single item without triggering a rebuild.
     */
    public void add(String item) {

        lock.writeLock().lock();

        try {

            setBits(item);

            count++;

        } finally {

            lock.writeLock().unlock();

        }
    }

    /**
     * Returns false -> item is DEFINITELY NOT in the set.
     * Returns true  -> item is PROBABLY in the set (could be a false positive).
     */
    public boolean mightContain(String item) {

        lock.readLock().lock();

        try {

            long[] h = hash(item);

            for (int i = 0; i < k; i++) {

                int pos = position(h[0], h[1], i, m);

                if (!bits.get(pos)) {

                    return false;

                }
            }

            return true;

        } finally {

            lock.readLock().unlock();

        }
    }

    /**
     * True once the filter has filled up enough that accuracy would start degrading.
     */
    public boolean needsResize() {

        lock.readLock().lock();

        try {

            return count >= (int) (expectedInsertions * 0.75);

        } finally {

            lock.readLock().unlock();

        }
    }

    /**
     * Rebuilds the filter for a larger capacity and re-adds every item from
     * the supplied list. This is the "grow" operation - it recomputes m and k
     * for the new capacity, throws away the old bit array, and re-hashes
     * everything into a fresh one.
     */
    public void growAndRebuild(int newExpectedInsertions, List<String> allItems) {

        lock.writeLock().lock();

        try {

            this.expectedInsertions = Math.max(newExpectedInsertions, Math.max(allItems.size(), 1));

            this.m = optimalM(this.expectedInsertions, falsePositiveRate);

            this.k = optimalK(this.m, this.expectedInsertions);

            this.bits = new BitSet(this.m);

            this.count = 0;

            for (String item : allItems) {

                setBits(item);

                count++;

            }

        } finally {

            lock.writeLock().unlock();

        }
    }

    private void setBits(String item) {

        long[] h = hash(item);

        for (int i = 0; i < k; i++) {

            int pos = position(h[0], h[1], i, m);

            bits.set(pos);

        }
    }

    // Double hashing: derive k positions from just 2 real hash values
    // instead of writing k independent hash functions by hand.
    private int position(long h1, long h2, int i, int m) {

        long combined = h1 + i * h2;

        int pos = (int) (combined % m);

        return Math.abs(pos);

    }

    private long[] hash(String item) {

        try {

            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] digest = md.digest(item.getBytes(StandardCharsets.UTF_8));

            long h1 = bytesToLong(digest, 0);

            long h2 = bytesToLong(digest, 8);

            return new long[]{h1, h2};

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException("MD5 algorithm not available", e);

        }
    }

    private long bytesToLong(byte[] bytes, int offset) {

        long result = 0;

        for (int i = 0; i < 8; i++) {

            result <<= 8;

            result |= (bytes[offset + i] & 0xFF);

        }

        return result;

    }

    // m = -(n * ln(p)) / (ln(2)^2)
    private static int optimalM(int n, double p) {

        double m = -(n * Math.log(p)) / (Math.log(2) * Math.log(2));

        return Math.max((int) Math.ceil(m), 8);

    }

    // k = (m/n) * ln(2)
    private static int optimalK(int m, int n) {

        int k = (int) Math.round(((double) m / n) * Math.log(2));

        return Math.max(k, 1);

    }

    public int getBitArraySize() {

        return m;

    }

    public int getHashFunctionCount() {

        return k;

    }
}