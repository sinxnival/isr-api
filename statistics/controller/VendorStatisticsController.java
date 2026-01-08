package com.htenc.isr.api.statistics.controller;

import com.htenc.isr.api.statistics.dto.VendorStatisticsRequest;
import com.htenc.isr.api.statistics.dto.VendorStatisticsRowDto;
import com.htenc.isr.api.statistics.dto.VendorStatisticsSummaryDto;
import com.htenc.isr.api.statistics.service.VendorStatisticsService;
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

@Tag(name = "통계 - Vendor별")
@RestController
@RequestMapping("/statistics/vendors")
@RequiredArgsConstructor
@Validated
public class VendorStatisticsController {

    private final VendorStatisticsService service;

    @Operation(summary = "Vendor별 통계 그리드 조회")
    @GetMapping("/grid")
    public CommonApiResponse<List<VendorStatisticsRowDto>> getGrid(
            @Valid
            @Parameter(description = "검색 조건 (기간, 프로젝트, Vendor)")
            VendorStatisticsRequest request
    ) {
        return CommonApiResponse.success(service.getVendorStatisticsGrid(request));
    }

    @Operation(summary = "Vendor별 통계 요약 조회")
    @GetMapping("/summary")
    public CommonApiResponse<VendorStatisticsSummaryDto> getSummary(
            @Valid
            @Parameter(description = "검색 조건 (기간, 프로젝트, Vendor)")
            VendorStatisticsRequest request
    ) {
        return CommonApiResponse.success(service.getVendorStatisticsSummary(request));
    }
}

