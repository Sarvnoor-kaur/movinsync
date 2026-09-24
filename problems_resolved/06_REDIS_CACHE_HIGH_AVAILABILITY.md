# Problem 06: High-Volume Cache Resilience & Fallback

## ❌ What Was The Problem?
Frequent DB lookups for rate slabs, contracts, and vehicle details during high-volume trip logging create database performance bottlenecks. While caching in **Redis** resolves latency, a Redis connection failure or server crash would throw runtime exceptions on `@Cacheable` methods, causing the backend API to fail.

---

## ✅ How Was It Resolved? (Simple Explanation)
We configured Spring Cache with Redis and implemented a custom `CacheErrorHandler` in [`RedisConfig.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/config/RedisConfig.java):
1. Normal operation reads/writes hot data in Redis (2ms latency).
2. If Redis drops connection or throws an exception, `CacheErrorHandler` catches the exception silently, logs a warning, and falls back to fetching data directly from MySQL database tables without failing user requests.

---

## 💡 Concrete Real-World Example
- **Redis Online**: API fetches active contract version from Redis cache in 2ms.
- **Redis Offline (Network Outage)**: System logs `WARN: Redis connection failed. Falling back to database read.`, fetches contract from MySQL in 25ms, and completes the trip logging API call successfully.

---

## 📄 Source Code Implementation
- Redis Configuration & Error Handler: [`RedisConfig.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/config/RedisConfig.java#L45-L70)
- Cached Service Layer: [`VendorService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/service/VendorService.java)
