package com.htenc.isr.api.statistics.repository;

import com.htenc.isr.api.statistics.dto.*;

import java.util.List;

public interface InspectionStatisticsRepository {
    /**
     * 메인화면 검사 List
     */
    List<InspectionMainGridRowDto> findMainGrid(InspectionMainGridSearchCond cond);

    /**
     * 월별 검사 건수(취소 제외, 종료일 기준)
     */
    List<InspectionMonthlyCountDto> countByMonth(InspectionMainGridSearchCond cond);

    /**
     * 프로젝트별 검사 건수(취소 제외, 종료일 기준)
     */
    List<InspectionProjectMonthlyCountDto> countByProjectAndMonth(InspectionMainGridSearchCond cond);

    /**
     * 상태별 집계
     */
    InspectionSummarySimpleDto fetchInspectionSummarySimple(InspectionMainGridSearchCond cond);

    InspectionSummaryComplexDto fetchInspectionSummaryComplex(InspectionMainGridSearchCond cond);

    InspectionSummaryProgressDto fetchInspectionSummaryProgress(InspectionMainGridSearchCond cond);

    /**
     * 필터 목록
     */
    List<FilterOptionDto> findAllProjectOptions();

    List<FilterOptionDto> findAllVendorOptions();

    List<FilterOptionDto> findAllInspectorOptions();

    List<FilterOptionDto> findAllItemTypeOptions();
}
