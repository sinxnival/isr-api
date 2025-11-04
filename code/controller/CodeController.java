package com.htenc.isr.api.code.controller;

import com.htenc.isr.api.code.dto.CodeCreateRequest;
import com.htenc.isr.api.code.dto.CodeResponse;
import com.htenc.isr.api.code.dto.CodeUpdateRequest;
import com.htenc.isr.api.code.service.CodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/code")
@RequiredArgsConstructor
@Validated
public class CodeController {

    private final CodeService service;

    /**
     * 그룹별 코드 목록 조회
     * GET /api/code/group/{codeGrpId}
     */
    @GetMapping("/group/{codeGrpId}")
    public ResponseEntity<List<CodeResponse>> listByGroup(
            @PathVariable Long codeGrpId
//            @RequestParam(required = false) String isUse
    ) {
        return ResponseEntity.ok(service.listByGroup(codeGrpId));
    }

    /**
     * 단건 조회
     * GET /api/code/{codeGrpId}/{code}
     */
    @GetMapping("/{codeGrpId}/{code}")
    public ResponseEntity<CodeResponse> get(
            @PathVariable Long codeGrpId,
            @PathVariable String code
    ) {
        return ResponseEntity.ok(service.get(codeGrpId, code));
    }

    /**
     * 생성
     * POST /api/code
     */
    @PostMapping
    public ResponseEntity<CodeResponse> create(@Valid @RequestBody CodeCreateRequest request) {
        CodeResponse created = service.create(request);
        // Location 헤더를 표준 형태로 반환
        URI location = URI.create(String.format("/api/code/%d/%s", created.codeGrpId(), created.code()));
        return ResponseEntity.created(location).body(created);
    }

    /**
     * 수정 (부분 수정 컨벤션)
     * PUT /api/code/{codeGrpId}/{code}
     */
    @PutMapping("/{codeGrpId}/{code}")
    public ResponseEntity<CodeResponse> update(
            @PathVariable Long codeGrpId,
            @PathVariable String code,
            @Valid @RequestBody CodeUpdateRequest request
    ) {
        return ResponseEntity.ok(service.update(codeGrpId, code, request));
    }

    /**
     * 삭제
     * DELETE /api/code/{codeGrpId}/{code}
     */
    @DeleteMapping("/{codeGrpId}/{code}")
    public ResponseEntity<Void> delete(
            @PathVariable Long codeGrpId,
            @PathVariable String code
    ) {
        service.delete(codeGrpId, code);
        return ResponseEntity.noContent().build();
    }
}
