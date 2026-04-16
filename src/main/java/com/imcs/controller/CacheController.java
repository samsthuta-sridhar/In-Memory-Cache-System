package com.imcs.controller;

import com.imcs.response.CacheResponseView;
import com.imcs.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cache")
public class CacheController {

    // GRASP Controller — receives requests, delegates to CacheService
    @Autowired
    private CacheService cacheService;

    @GetMapping("/{key}")
    public ResponseEntity<CacheResponseView> get(@PathVariable String key) {
        String value = cacheService.getValue(key);
        if (value == null) {
            return ResponseEntity.status(404)
                .body(CacheResponseView.notFound(key));
        }
        return ResponseEntity.ok(CacheResponseView.hit(key, value));
    }

    @PostMapping
    public ResponseEntity<CacheResponseView> put(
            @RequestBody Map<String, String> body) {
        String key = body.get("key");
        String value = body.get("value");
        CacheService.PutResult result = cacheService.put(key, value);
        CacheResponseView response = new CacheResponseView(
            key, value, "STORED",
            result.evictionHappened
                ? "Stored — evicted " + result.evictedKey
                : "Stored successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{key}")
    public ResponseEntity<CacheResponseView> update(
            @PathVariable String key,
            @RequestBody Map<String, String> body) {
        cacheService.update(key, body.get("value"));
        return ResponseEntity.ok(new CacheResponseView(
            key, body.get("value"), "UPDATED", "Updated successfully"));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<CacheResponseView> delete(@PathVariable String key) {
        cacheService.delete(key);
        return ResponseEntity.ok(new CacheResponseView(
            key, null, "DELETED", "Deleted successfully"));
    }
}