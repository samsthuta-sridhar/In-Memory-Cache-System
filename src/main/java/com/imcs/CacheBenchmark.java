package com.imcs;

import com.imcs.cache.CacheManager;
import com.imcs.repository.CacheDataRepository;
import com.imcs.service.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

@Component
public class CacheBenchmark implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CacheBenchmark.class);

    @Autowired private CacheService cacheService;
    @Autowired private CacheDataRepository cacheDataRepository;
    @Autowired private CacheManager cacheManager;

    @Override
    public void run(ApplicationArguments args) {
        String testKey = "benchmark:001";
        cacheService.put(testKey, "benchmark-value");

        // ── Warm-up phase ──
        for (int i = 0; i < 100; i++) cacheManager.getEntry(testKey);
        for (int i = 0; i < 100; i++) cacheService.getValue(testKey);
        for (int i = 0; i < 100; i++) cacheDataRepository.findById(testKey);

        // ── 1. Raw cache lookup (bypasses audit/rate-limit/decrypt) ──
        long start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            cacheManager.getEntry(testKey);
        }
        long rawCacheAvgNs = (System.nanoTime() - start) / 1000;

        // ── 2. Full service path (rate limit + audit log + decrypt) ──
        start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            cacheService.getValue(testKey);
        }
        long fullServiceAvgNs = (System.nanoTime() - start) / 1000;

        // ── 3. Direct DB query (no cache, no audit overhead) ──
        cacheManager.removeEntry(testKey);
        for (int i = 0; i < 100; i++) cacheDataRepository.findById(testKey);

        start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            cacheDataRepository.findById(testKey);
        }
        long dbAvgNs = (System.nanoTime() - start) / 1000;

        double rawSpeedupVsDb = dbAvgNs > 0 ? (double) dbAvgNs / rawCacheAvgNs : 0;
        double auditOverheadFactor = rawCacheAvgNs > 0
            ? (double) fullServiceAvgNs / rawCacheAvgNs : 0;

        log.info("BENCHMARK | Raw cache lookup: {} ns | Full service path: {} ns | DB query: {} ns",
            String.format("%,d", rawCacheAvgNs),
            String.format("%,d", fullServiceAvgNs),
            String.format("%,d", dbAvgNs));
        log.info("BENCHMARK | Raw cache vs DB speedup: {}x | Audit/security overhead factor: {}x",
            String.format("%.1f", rawSpeedupVsDb),
            String.format("%.1f", auditOverheadFactor));

        // Write structured result to JSON file
        String json = String.format(
            "{%n" +
            "  \"rawCacheLookupAvgNs\": %d,%n" +
            "  \"fullServicePathAvgNs\": %d,%n" +
            "  \"dbQueryAvgNs\": %d,%n" +
            "  \"rawCacheSpeedupVsDb\": %.1f,%n" +
            "  \"auditSecurityOverheadFactor\": %.1f,%n" +
            "  \"timestamp\": \"%s\"%n" +
            "}%n",
            rawCacheAvgNs, fullServiceAvgNs, dbAvgNs,
            rawSpeedupVsDb, auditOverheadFactor, Instant.now());

        try {
            Files.writeString(Path.of("benchmark-result.json"), json);
            log.info("Benchmark result written to benchmark-result.json");
        } catch (Exception e) {
            log.error("Failed to write benchmark result file", e);
        }

        cacheService.delete(testKey);
    }
}