package com.imcs.config;

import com.imcs.eviction.EvictionPolicy;
import com.imcs.eviction.LRUEvictionPolicy;
import com.imcs.eviction.LFUEvictionPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CacheConfiguration {

    @Value("${cache.eviction-policy:LRU}")
    private String evictionPolicy;

    @Autowired
    private LRUEvictionPolicy lruEvictionPolicy;

    @Autowired
    private LFUEvictionPolicy lfuEvictionPolicy;

    @Bean
    @Primary
    public EvictionPolicy evictionPolicy() {
        if ("LFU".equalsIgnoreCase(evictionPolicy)) {
            return lfuEvictionPolicy;
        }
        return lruEvictionPolicy;
    }
}