package com.htenc.isr.api.statistics.controller;

import com.htenc.isr.api.statistics.dto.ItemTypeStatisticsRequest;
import com.htenc.isr.api.statistics.dto.ItemTypeStatisticsRowDto;
import com.htenc.isr.api.statistics.dto.ItemTypeStatisticsSummaryDto;
import com.htenc.isr.api.statistics.service.ItemTypeStatisticsService;
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

@Tag(name = "통계 - Item Type별")
@RestController
@RequestMapping("/statistics/item-types")
@RequiredArgsConstructor
@Validated
public class ItemTypeStatisticsController {

    private final ItemTypeStatisticsService service;

    @Operation(summary = "Item Type별 통계 그리드 조회")
    @GetMapping("/grid")
    public CommonApiResponse<List<ItemTypeStatisticsRowDto>> getGrid(
            @Valid
            @Parameter(description = "검색 조건 (기간, 프로젝트, Vendor, Item Type)")
            ItemTypeStatisticsRequest request
    ) {
        return CommonApiResponse.success(service.getItemTypeStatisticsGrid(request));
    }

    @Operation(summary = "Item Type별 통계 요약 조회")
    @GetMapping("/summary")
    public CommonApiResponse<ItemTypeStatisticsSummaryDto> getSummary(
            @Valid
            @Parameter(description = "검색 조건 (기간, 프로젝트, Vendor, Item Type)")
            ItemTypeStatisticsRequest request
    ) {
        return CommonApiResponse.success(service.getItemTypeStatisticsSummary(request));
    }
}

