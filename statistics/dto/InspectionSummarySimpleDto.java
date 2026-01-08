package com.htenc.isr.api.statistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대시보드 요약 통계")
public record InspectionSummarySimpleDto(
        @Schema(description = "전체 신청 건수") long totalCount,
        @Schema(description = "완료") long doneCount,
        @Schema(description = "진행중 건수") long progressCount,
        @Schema(description = "취소 건수") long canceledCount,
        @Schema(description = "상주검사 건수") long residentCount,
        @Schema(description = "신규 건수") long todayNewCount
) {
}
