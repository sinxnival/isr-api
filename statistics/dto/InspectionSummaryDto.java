package com.htenc.isr.api.statistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대시보드 상태별 집계 통합")
public record InspectionSummaryDto(
        long totalCount,
        long doneCount,
        long progressCount,
        long canceledCount,
        long residentCount,
        long todayNewCount,
        long unassignedCount,
        long delayedCount,
        long ncrCount,
        double progressRate
) {
}
