package com.htenc.isr.api.statistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로젝트별 통계 요약")
public record ProjectStatisticsSummaryDto(
        @Schema(description = "총 검사 건수") Long totalInspectionCount,
        @Schema(description = "전체 검사 진행률") Double inspectionProgressRate,
        @Schema(description = "총 NCR 건수") Long totalNcrCount,
        @Schema(description = "전체 NCR 비율") Double totalNcrRatio,
        @Schema(description = "총 내부 Inspector 건수") Long totalInternalInspectorCount,
        @Schema(description = "총 외부 Inspector 건수") Long totalExternalInspectorCount
) {
}

