package com.imcs.repository;

import com.imcs.entity.CacheDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CacheDataRepository extends JpaRepository<CacheDataEntity, String> {
}