# Dynamic Username Bloom Filter

A hands-on exploration of Bloom filters, built twice, two different ways, to
actually understand how systems like Instagram or Twitter check "is this
username taken?" for millions of accounts without hammering a database on
every keystroke.

## What's a Bloom filter, in one paragraph

A Bloom filter is a space-efficient way to test whether an item is *probably*
in a set or *definitely not* in a set. Instead of storing actual data, it
hashes each item into a handful of positions on a bit array and flips those
bits on. Checking later means re-hashing the item and checking if those same
bits are still on. If even one bit is off, the item is **definitely not** in
the set, no database call needed. If all the bits are on, it's **probably**
in the set (rare false positives are possible, false negatives are not), so
you fall back to a real database check only in that case.

## Why this repo has two implementations

**1. In-memory, dynamic `BitSet` version**

A Bloom filter built from scratch in Java using `java.util.BitSet`, where the
bit array size (`m`) and number of hash functions (`k`) are calculated from
the standard formulas based on expected insertions and a target false
positive rate, and grow by rebuilding from the database once the filter
starts to fill up. Good for understanding the mechanics, the hashing, the
double-hashing trick for deriving multiple positions from one hash, the
sizing math, with nothing hidden inside a library.

**2. Redis-backed version using RedisBloom + Jedis**

The production-shaped version. The filter lives in Redis instead of local
JVM memory, using the native RedisBloom module (`BF.RESERVE`, `BF.ADD`,
`BF.EXISTS`, `BF.INFO`) through Jedis's built-in Bloom filter support. This
means:
- The filter survives application restarts (it isn't sitting in one
  process's heap)
- Multiple app instances share the same filter instead of each keeping its
  own out-of-sync copy
- Redis handles bit array sizing and auto-growth internally — no manual
  resize/rebuild logic required

## Tech stack

- Java 17, Spring Boot 3
- Spring Data JPA + H2 (swap for PostgreSQL by changing the connection string)
- Jedis (Redis client with native RedisBloom command support)
- Docker Compose running `redis-stack-server` (bundles the RedisBloom module
  — plain Redis does **not** include it)

## Prerequisites

- JDK 21+
- Maven
- Docker (for Redis Stack)

## Getting started

```bash
# 1. Start Redis Stack (includes the RedisBloom module)
docker compose up -d

# 2. Run the app
mvn spring-boot:run
```

On startup, the app seeds a handful of usernames into the database (if empty)
and loads them into the Bloom filter.

## API endpoints

| Method | Endpoint                         | Description (Dynamic Bloom filter)                              |
|--------|----------------------------------|----------------------------------------------------------------|
| GET    | `/api/v1/usernames/check?username=` | Checks if a username exists — Bloom filter first, DB only on a "maybe" |
| POST   | `/api/v1/usernames/register`        | Registers a new user; adds the username to the Bloom filter    |
| GET    | `/api/v1/usernames/stats`           | outputs bloom filter — capacity, size, sub-filter count, items inserted |

| Method | Endpoint                         | Description (Redis Bloom filter)                                |
|--------|----------------------------------|----------------------------------------------------------------|
| GET    | `/api/v2/usernames/check?username=` | Checks if a username exists — Bloom filter first, DB only on a "maybe" |
| POST   | `/api/v2/usernames/register`        | Registers a new user; adds the username to the Bloom filter    |
| GET    | `/api/v2/usernames/stats`           | Raw `BF.INFO` output — capacity, size, sub-filter count, items inserted |

Example request body for `/register`:

```json
{
  "firstName": "Samuel",
  "lastName": "Adeyemi",
  "username": "samuel_dev123"
}
```

## License

MIT — feel free to use this for your own learning.
