package com.htenc.isr.api.statistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record InspectionMainGridRowDto(
        @Schema(description = "검사 신청서 번호(APPL_NO)") Long applNo,             // 신청 PK (row 클릭 시 상세 이동용)
        @Schema(description = "검사 일자 (검사 예정 시작일)") LocalDate inspectionDate,
        @Schema(description = "프로젝트명") String projectName,
        @Schema(description = "Vendor 회사명") String vendorName,
        @Schema(description = "검사 유형") String kindOfInspection,
        @Schema(description = "검사원 이름") String inspectorName,
        @Schema(description = "상태") String status
) {
}
