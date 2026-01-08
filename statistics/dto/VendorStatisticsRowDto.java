package com.htenc.isr.api.statistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Vendor별 통계 그리드 행")
public record VendorStatisticsRowDto(
        @Schema(description = "Vendor 회사 번호") Long vendorCompNo,
        @Schema(description = "Vendor 회사명") String vendorCompanyName,
        @Schema(description = "프로젝트 관리번호") Long projectNo,
        @Schema(description = "프로젝트명") String projectName,
        @Schema(description = "프로젝트 코드") String projectCode,
        @Schema(description = "프로젝트별 총 검사 건수") Long totalInspectionCount,
        @Schema(description = "NCR 건수") Long ncrCount,
        @Schema(description = "NCR 비율 (NCR 건수 / 검사 신청 건수 * 100)") Double ncrRatio,
        @Schema(description = "NCR Type별 발생 횟수") Map<String, Long> ncrTypeCounts
) {
}

