package com.htenc.isr.api.code.repository;

import com.htenc.isr.api.code.domain.CodeEntity;
import com.htenc.isr.api.code.domain.id.CodeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CodeRepository extends JpaRepository<CodeEntity, CodeId> {

    /**
     * 특정 코드 그룹 내의 코드 목록 조회
     * - 사용여부(Y/N) 필터
     * - 정렬 순서 기준 오름차순
     */
    List<CodeEntity> findById_CodeGrpIdAndIsUseOrderBySortOrderAsc(Long codeGrpId, String isUse);

    /**
     * 코드 그룹 내 특정 코드 존재 여부 확인
     */
    boolean existsById_CodeGrpIdAndId_Code(Long codeGrpId, String code);

    /**
     * 그룹 내 전체 코드 조회 (사용여부 무관)
     */
    List<CodeEntity> findById_CodeGrpIdOrderBySortOrderAsc(Long codeGrpId);
}