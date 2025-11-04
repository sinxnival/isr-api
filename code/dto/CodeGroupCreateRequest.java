package com.htenc.isr.api.code.dto;

import com.htenc.isr.api.code.domain.CodeGroupEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CodeGroupCreateRequest {
    @NotBlank(message = "그룹 코드는 필수입니다.")
    private String groupCode;
    @NotBlank(message = "그룹명은 필수입니다.")
    private String groupName;
    private String isUse;       // null/blank → Y 로 보정
    private Long sortOrder;
    private String description;
    private Long regUser;

    public CodeGroupEntity toEntityDefaultingY() {
        String use = (isUse == null || isUse.isBlank()) ? "Y" : isUse;
        return CodeGroupEntity.builder()
                .groupCode(groupCode.trim())
                .groupName(groupName.trim())
                .isUse(use)
                .sortOrder(sortOrder)
                .description(description)
                .regUser(regUser)
                .build();
    }
}