package com.htenc.isr.api.code.dto;

import com.htenc.isr.api.code.domain.CodeEntity;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CodeUpdateRequest {
    @NotBlank private String codeVal;
    @Pattern(regexp = "[YN]") private String isUse; // null 허용(미변경)
    private Long sortOrder;
    private String description;
    private Long modUser;

    /** 부분 수정(PATCH) 책임을 DTO가 가짐 → 서비스가 간결해짐 */
    public void applyTo(CodeEntity e) {
        if (codeVal != null) e.setCodeVal(codeVal);
        if (isUse != null && !isUse.isBlank()) e.setIsUse(isUse);
        if (sortOrder != null) e.setSortOrder(sortOrder);
        if (description != null) e.setDescription(description);
        if (modUser != null) e.setModUser(modUser);
    }
}