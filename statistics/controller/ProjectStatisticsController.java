package com.htenc.isr.api.statistics.controller;

import com.htenc.isr.api.statistics.dto.ProjectStatisticsRequest;
import com.htenc.isr.api.statistics.dto.ProjectStatisticsRowDto;
import com.htenc.isr.api.statistics.dto.ProjectStatisticsSummaryDto;
import com.htenc.isr.api.statistics.service.ProjectStatisticsService;
import com.htenc.isr.common.dto.CommonApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "통계 - 프로젝트별")
@RestController
@RequestMapping("/statistics/projects")
@RequiredArgsConstructor
@Validated
public class ProjectStatisticsController {

    private final ProjectStatisticsService service;

    @Operation(summary = "프로젝트별 통계 그리드 조회")
    @GetMapping("/grid")
    public CommonApiResponse<List<ProjectStatisticsRowDto>> getGrid(
            @Valid
            @Parameter(description = "검색 조건 (기간, 프로젝트)")
            ProjectStatisticsRequest request
    ) {
        return CommonApiResponse.success(service.getProjectStatisticsGrid(request));
    }

    @Operation(summary = "프로젝트별 통계 요약 조회")
    @GetMapping("/summary")
    public CommonApiResponse<ProjectStatisticsSummaryDto> getSummary(
            @Valid
            @Parameter(description = "검색 조건 (기간, 프로젝트)")
            ProjectStatisticsRequest request
    ) {
        return CommonApiResponse.success(service.getProjectStatisticsSummary(request));
    }
}

