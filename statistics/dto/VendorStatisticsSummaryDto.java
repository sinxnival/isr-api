package com.htenc.isr.api.statistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Vendor별 통계 요약")
public record VendorStatisticsSummaryDto(
        @Schema(description = "총 검사 건수") Long totalInspectionCount,
        @Schema(description = "프로젝트 개수") Integer totalProjectCount,
        @Schema(description = "총 NCR 건수") Long totalNcrCount,
        @Schema(description = "전체 NCR 비율") Double totalNcrRatio,
        @Schema(description = "NCR Type별 총 발생 횟수") Map<String, Long> ncrTypeCounts
) {
}

