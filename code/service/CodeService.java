package com.htenc.isr.api.code.service;

import com.htenc.isr.api.code.domain.CodeEntity;
import com.htenc.isr.api.code.domain.id.CodeId;
import com.htenc.isr.api.code.dto.CodeCreateRequest;
import com.htenc.isr.api.code.dto.CodeResponse;
import com.htenc.isr.api.code.dto.CodeUpdateRequest;
import com.htenc.isr.api.code.repository.CodeRepository;
import com.htenc.isr.config.LoggingConfig;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeService {

    private final CodeRepository repository;

    @Transactional
    public CodeResponse create(CodeCreateRequest request) {
        CodeEntity saved = repository.save(request.toEntity("Y")); // 기본값 보정은 DTO 내부에서
        LoggingConfig.LoggingUtils.auditLog("CREATE", "ISR_CODE_SET", "SUCCESS",
                "id=" + saved.getId().getCodeGrpId() + "/" + saved.getId().getCode());
        return CodeResponse.fromEntity(saved);
    }

    public CodeResponse get(Long codeGrpId, String code) {
        CodeEntity e = repository.findById(new CodeId(codeGrpId, code))
                .orElseThrow(() -> new EntityNotFoundException("Code not found"));
        return CodeResponse.fromEntity(e);
    }

    public List<CodeResponse> listByGroup(Long codeGrpId) {
        return repository.findById_CodeGrpIdOrderBySortOrderAsc(codeGrpId)
                .stream().map(CodeResponse::fromEntity).toList();
    }

    @Transactional
    public CodeResponse update(Long codeGrpId, String code, CodeUpdateRequest request) {
        CodeEntity e = repository.findById(new CodeId(codeGrpId, code))
                .orElseThrow(() -> new EntityNotFoundException("Code not found"));
        request.applyTo(e); // 부분수정 책임을 DTO가 가짐
        LoggingConfig.LoggingUtils.auditLog("UPDATE", "ISR_CODE_SET", "SUCCESS",
                "id=" + codeGrpId + "/" + code);
        return CodeResponse.fromEntity(e); // JPA dirty checking으로 자동 flush
    }

    @Transactional
    public void delete(Long codeGrpId, String code) {
        CodeId id = new CodeId(codeGrpId, code);
        if (!repository.existsById(id)) throw new EntityNotFoundException("Code not found");
        repository.deleteById(id);
        LoggingConfig.LoggingUtils.auditLog("DELETE", "ISR_CODE_SET", "SUCCESS",
                "id=" + codeGrpId + "/" + code);
    }
}