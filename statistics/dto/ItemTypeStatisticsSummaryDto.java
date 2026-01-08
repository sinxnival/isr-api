package com.htenc.isr.api.statistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Item Type별 통계 요약")
public record ItemTypeStatisticsSummaryDto(
        @Schema(description = "총 검사 횟수") Long totalInspectionCount,
        @Schema(description = "평균 검사 횟수") Double averageInspectionCount,
        @Schema(description = "Item Type 개수") Integer itemTypeCount,
        @Schema(description = "Item 개수") Integer itemCount,
        @Schema(description = "프로젝트 개수") Integer projectCount
) {
}

