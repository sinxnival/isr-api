package com.htenc.isr.api.statistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "프로젝트별 통계 검색 조건")
public record ProjectStatisticsRequest(
        @Schema(description = "검색 시작 날짜") LocalDate fromDate,
        @Schema(description = "검색 종료 날짜") LocalDate toDate,
        @Schema(description = "프로젝트 번호(JNO)") Long projectNo
) {
}

