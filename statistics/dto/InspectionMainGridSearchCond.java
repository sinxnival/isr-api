package com.htenc.isr.api.statistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record InspectionMainGridSearchCond(
        @Schema(description = "검색 시작 날짜") LocalDate fromDate,
        @Schema(description = "검색 종료 날짜") LocalDate toDate,
        @Schema(description = "프로젝트 번호(JNO)") Long projectNo,
        @Schema(description = "Vendor 회사 번호") Long vendorCompNo,
        @Schema(description = "검사원 고유번호") Long inspectorNo,
        @Schema(description = "아이템 마스터 번호(ITEM_MASTER_NO)") Long itemMasterNo,
        @Schema(description = "아이템 코드") String itemCode,
        @Schema(description = "기자재 분류 코드") String itemEquipType
) {
}
