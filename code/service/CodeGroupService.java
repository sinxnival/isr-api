package com.htenc.isr.api.code.service;

import com.htenc.isr.api.code.domain.CodeGroupEntity;
import com.htenc.isr.api.code.dto.CodeGroupCreateRequest;
import com.htenc.isr.api.code.dto.CodeGroupResponse;
import com.htenc.isr.api.code.dto.CodeGroupUpdateRequest;
import com.htenc.isr.api.code.repository.CodeGroupRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CodeGroupService {

    private final CodeGroupRepository repository;

    @Transactional
    public CodeGroupResponse create(CodeGroupCreateRequest req) {
        if (repository.existsByGroupCode(req.getGroupCode())) {
            throw new IllegalStateException("이미 존재하는 그룹 코드입니다: " + req.getGroupCode());
        }
        CodeGroupEntity saved = repository.save(req.toEntityDefaultingY());
        return CodeGroupResponse.from(saved);
    }

    public List<CodeGroupResponse> list() {
        return repository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(CodeGroupResponse::from)
                .toList();
    }

    public CodeGroupResponse getById(Long codeGrpId) {
        CodeGroupEntity e = repository.findById(codeGrpId)
                .orElseThrow(() -> new EntityNotFoundException("그룹을 찾을 수 없습니다: id=" + codeGrpId));
        return CodeGroupResponse.from(e);
    }

    public CodeGroupResponse getByCode(String groupCode) {
        CodeGroupEntity e = repository.findByGroupCode(groupCode)
                .orElseThrow(() -> new EntityNotFoundException("그룹을 찾을 수 없습니다: " + groupCode));
        return CodeGroupResponse.from(e);
    }

    @Transactional
    public CodeGroupResponse updateByCode(String currentGroupCode, CodeGroupUpdateRequest req) {
        CodeGroupEntity e = repository.findByGroupCode(currentGroupCode)
                .orElseThrow(() -> new EntityNotFoundException("그룹을 찾을 수 없습니다: " + currentGroupCode));

        // 1) groupCode 변경(옵션)
        if (req.getGroupCode() != null && !req.getGroupCode().isBlank()) {
            String newCode = req.getGroupCode().trim();
            if (!newCode.equals(currentGroupCode) && repository.existsByGroupCode(newCode)) {
                throw new IllegalStateException("이미 존재하는 그룹 코드입니다: " + newCode);
            }
            e.setGroupCode(newCode);
        }

        // 2) 나머지 필드 업데이트
        req.applyTo(e); // 더티체킹으로 반영
        return CodeGroupResponse.from(e);
    }

    @Transactional
    public void deleteByCode(String groupCode) {
        CodeGroupEntity e = repository.findByGroupCode(groupCode)
                .orElseThrow(() -> new EntityNotFoundException("그룹을 찾을 수 없습니다: " + groupCode));
        repository.delete(e);
    }
}