package com.htenc.isr.api.statistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Item Type별 통계 그리드 행")
public record ItemTypeStatisticsRowDto(
        @Schema(description = "Item Type 관리번호") Long itemTypeNo,
        @Schema(description = "Item Type 코드") String itemTypeCode,
        @Schema(description = "Item Type 명") String itemTypeName,
        @Schema(description = "Item Type별 검사 횟수") Long inspectionCount,
        @Schema(description = "Item Type별 평균 검사 횟수") Double averageInspectionCount,
        @Schema(description = "Item 갯수") Long itemCount,
        @Schema(description = "프로젝트 관리번호") Long projectNo,
        @Schema(description = "프로젝트명") String projectName,
        @Schema(description = "프로젝트 코드") String projectCode
) {
}

