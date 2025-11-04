package com.htenc.isr.api.code.repository;

import com.htenc.isr.api.code.domain.CodeGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodeGroupRepository extends JpaRepository<CodeGroupEntity, Long> {
    boolean existsByGroupCode(String groupCode);
    Optional<CodeGroupEntity> findByGroupCode(String groupCode);
    List<CodeGroupEntity> findAllByOrderBySortOrderAsc();
}