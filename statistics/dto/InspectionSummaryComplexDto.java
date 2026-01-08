package com.htenc.isr.api.statistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대시보드 통계")
public record InspectionSummaryComplexDto(
        @Schema(description = "미할당 건수") long unassignedCount,
        @Schema(description = "지연 건수") long delayedCount,
        @Schema(description = "NCR 건수") long ncrCount
) {
}
