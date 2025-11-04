package com.htenc.isr.api.code.dto;

import com.htenc.isr.api.code.domain.CodeGroupEntity;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CodeGroupUpdateRequest {
    private String groupCode;
    private String groupName;
    private String isUse;       // Y/N
    private Long sortOrder;
    private String description;
    private Long modUser;

    public void applyTo(CodeGroupEntity e) {
        if (groupName != null && !groupName.isBlank()) e.setGroupName(groupName.trim());
        if (isUse != null && !isUse.isBlank()) e.setIsUse(isUse);
        if (sortOrder != null) e.setSortOrder(sortOrder);
        if (description != null) e.setDescription(description);
        if (modUser != null) e.setModUser(modUser);
        // MOD_DATE는 엔티티에서 insertable=false, updatable=false → DB 트리거/기본값 가정
    }
}