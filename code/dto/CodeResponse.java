package com.htenc.isr.api.code.dto;

import com.htenc.isr.api.code.domain.CodeEntity;
import com.htenc.isr.api.code.domain.id.CodeId;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CodeResponse(
        Long codeGrpId,
        String code,
        String codeVal,
        String isUse,
        Long sortOrder,
        String description,
        Long regUser,
        LocalDateTime regDate,
        Long modUser,
        LocalDateTime modDate
) {
    public static CodeResponse fromEntity(CodeEntity e) {
        CodeId id = e.getId();
        return CodeResponse.builder()
                .codeGrpId(id.getCodeGrpId())
                .code(id.getCode())
                .codeVal(e.getCodeVal())
                .isUse(e.getIsUse())
                .sortOrder(e.getSortOrder())
                .description(e.getDescription())
                .regUser(e.getRegUser())
                .regDate(e.getRegDate())
                .modUser(e.getModUser())
                .modDate(e.getModDate())
                .build();
    }
}