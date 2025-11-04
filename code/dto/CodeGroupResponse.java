package com.htenc.isr.api.code.dto;

import com.htenc.isr.api.code.domain.CodeGroupEntity;

import java.time.LocalDateTime;

public record CodeGroupResponse(
        Long codeGrpId,
        String groupCode,
        String groupName,
        String isUse,
        Long sortOrder,
        String description,
        Long regUser,
        LocalDateTime regDate,
        Long modUser,
        LocalDateTime modDate
) {
    public static CodeGroupResponse from(CodeGroupEntity e) {
        return new CodeGroupResponse(
                e.getCodeGrpId(),
                e.getGroupCode(),
                e.getGroupName(),
                e.getIsUse(),
                e.getSortOrder(),
                e.getDescription(),
                e.getRegUser(),
                e.getRegDate(),
                e.getModUser(),
                e.getModDate()
        );
    }
}