package com.htenc.isr.api.statistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대시보드 진행률")
public record InspectionSummaryProgressDto(
        @Schema(description = "진행률(%)") double progressRate
) {
}
