package com.htenc.isr.api.statistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "필터 콤보박스 옵션")
public record FilterOptionDto(
        @Schema(description = "옵션 값(식별자)") String value,
        @Schema(description = "옵션 라벨(표시명)") String label
) {
}
