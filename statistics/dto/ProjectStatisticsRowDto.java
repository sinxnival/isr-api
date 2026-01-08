package com.htenc.isr.api.statistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로젝트별 통계 그리드 행")
public record ProjectStatisticsRowDto(
        @Schema(description = "프로젝트 관리번호") Long projectNo,
        @Schema(description = "프로젝트명") String projectName,
        @Schema(description = "프로젝트 코드") String projectCode,
        @Schema(description = "총 검사 건수") Long totalInspectionCount,
        @Schema(description = "검사 진행률 (완료된 검사 건수 / 전체 검사 신청 건수 * 100)") Double inspectionProgressRate,
        @Schema(description = "NCR 건수") Long ncrCount,
        @Schema(description = "NCR 비율 (NCR 건수 / 검사 신청 건수 * 100)") Double ncrRatio,
        @Schema(description = "내부 Inspector 건수 (UserType = BIZ)") Long internalInspectorCount,
        @Schema(description = "외부 Inspector 건수 (UserType = PARTNER 또는 LOCAL)") Long externalInspectorCount
) {
}

