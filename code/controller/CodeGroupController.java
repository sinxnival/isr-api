package com.htenc.isr.api.code.controller;

import com.htenc.isr.api.code.dto.CodeGroupCreateRequest;
import com.htenc.isr.api.code.dto.CodeGroupResponse;
import com.htenc.isr.api.code.dto.CodeGroupUpdateRequest;
import com.htenc.isr.api.code.service.CodeGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/code-group")
@RequiredArgsConstructor
@Validated
public class CodeGroupController {

    private final CodeGroupService service;

    /** 생성 */
    @PostMapping
    public ResponseEntity<CodeGroupResponse> create(@Valid @RequestBody CodeGroupCreateRequest req) {
        CodeGroupResponse created = service.create(req);
        URI location = URI.create("/api/code-group/" + created.groupCode());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public ResponseEntity<List<CodeGroupResponse>> list() {
        return ResponseEntity.ok(service.list());
    }

    /** 단건 조회 (ID) */
    @GetMapping("/id/{codeGrpId}")
    public ResponseEntity<CodeGroupResponse> getById(@PathVariable Long codeGrpId) {
        return ResponseEntity.ok(service.getById(codeGrpId));
    }

    /** 단건 조회 (GROUP_CODE) */
    @GetMapping("/{groupCode}")
    public ResponseEntity<CodeGroupResponse> getByCode(@PathVariable String groupCode) {
        return ResponseEntity.ok(service.getByCode(groupCode));
    }

    /** 수정 (GROUP_CODE 기준) */
    @PutMapping("/{groupCode}")
    public ResponseEntity<CodeGroupResponse> update(
            @PathVariable String groupCode,
            @Valid @RequestBody CodeGroupUpdateRequest req
    ) {
        return ResponseEntity.ok(service.updateByCode(groupCode, req));
    }

    /** 삭제 (GROUP_CODE 기준) */
    @DeleteMapping("/{groupCode}")
    public ResponseEntity<Void> delete(@PathVariable String groupCode) {
        service.deleteByCode(groupCode);
        return ResponseEntity.noContent().build();
    }
}
