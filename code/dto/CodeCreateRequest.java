package com.htenc.isr.api.code.dto;

import com.htenc.isr.api.code.domain.CodeEntity;
import com.htenc.isr.api.code.domain.id.CodeId;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CodeCreateRequest {
    @NotNull private Long codeGrpId;
    @NotBlank(message = "코드는 필수입니다.")
    private String code;
    @NotBlank(message = "코드값은 필수입니다.")
    private String codeVal;
    @Pattern(regexp = "[YN]") private String isUse; // null이면 서비스/DB 기본값 사용
    private Long sortOrder;
    private String description;
    private Long regUser;

    /** Service에서 바로 호출 → 엔티티 생성 책임을 DTO가 가짐 */
    public CodeEntity toEntity(String defaultUse) {
        return CodeEntity.builder()
                .id(new CodeId(codeGrpId, code))
                .codeVal(codeVal)
                .isUse(isUse == null || isUse.isBlank() ? defaultUse : isUse)
                .sortOrder(sortOrder)
                .description(description)
                .regUser(regUser)
                .build();
    }
}