package com.htenc.isr.api.statistics.controller;

import com.htenc.isr.api.statistics.dto.*;
import com.htenc.isr.api.statistics.service.InspectionStatisticsService;
import com.htenc.isr.common.dto.CommonApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "통계 - 그리드")
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
@Validated
public class InspectionStatisticsController {

    private final InspectionStatisticsService service;

    @Operation(summary = "메인 대시보드 검사 그리드 조회")
    @GetMapping("/main-grid")
    public CommonApiResponse<List<InspectionMainGridRowDto>> getMainGrid(
            @Valid
            @Parameter(description = "검색 조건 (기간, 프로젝트, Vendor, 검사원, 품목, 기자재 분류 등)")
            InspectionMainGridSearchCond cond
    ) {
        return CommonApiResponse.success(service.getMainGrid(cond));
    }

    @Operation(summary = "월별 검사 건수 조회 (검사 종료일 기준, 취소 제외)")
    @GetMapping("/monthly-count")
    public CommonApiResponse<List<InspectionMonthlyCountDto>> getMonthlyCounts(
            @Parameter(description = "검색 조건 (기간, 프로젝트, Vendor, 검사원, 품목, 기자재 분류 등)")
            InspectionMainGridSearchCond cond
    ) {
        return CommonApiResponse.success(service.getMonthlyCounts(cond));
    }

    @Operation(summary = "프로젝트별 월별 검사 건수 조회 (검사 종료일 기준, 취소 제외)")
    @GetMapping("/project-monthly-count")
    public CommonApiResponse<List<InspectionProjectMonthlyCountDto>> getProjectMonthlyCounts(
            @ParameterObject
            @Valid
            InspectionMainGridSearchCond cond
    ) {
        return CommonApiResponse.success(service.getProjectMonthlyCounts(cond));
    }

    @Operation(summary = "상태별 집계")
    @GetMapping("/summary")
    public CommonApiResponse<InspectionSummaryDto> getInspectionSummary(
            @ParameterObject InspectionMainGridSearchCond cond
    ) {
        return CommonApiResponse.success(service.getInspectionSummary(cond));
    }

    @Operation(summary = "filter (project 목록)")
    @GetMapping("/filters/projects")
    public CommonApiResponse<List<FilterOptionDto>> getProjectOptions() {
        return CommonApiResponse.success(service.getAllProjectOptions());
    }

    @Operation(summary = "filter (vendor 목록)")
    @GetMapping("/filters/vendors")
    public CommonApiResponse<List<FilterOptionDto>> getVendorOptions() {
        return CommonApiResponse.success(service.getAllVendorOptions());
    }

    @Operation(summary = "filter (inspector 목록)")
    @GetMapping("/filters/inspectors")
    public CommonApiResponse<List<FilterOptionDto>> getInspectorOptions() {
        return CommonApiResponse.success(service.getAllInspectorOptions());
    }

    @Operation(summary = "filter (item type 목록)")
    @GetMapping("/filters/item-types")
    public CommonApiResponse<List<FilterOptionDto>> getItemTypeOptions() {
        return CommonApiResponse.success(service.getAllItemTypeOptions());
    }
}
