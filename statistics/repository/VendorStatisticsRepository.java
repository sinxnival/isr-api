package com.htenc.isr.api.statistics.repository;

import com.htenc.isr.api.statistics.dto.VendorStatisticsRequest;
import com.htenc.isr.api.statistics.dto.VendorStatisticsRowDto;
import com.htenc.isr.api.statistics.dto.VendorStatisticsSummaryDto;

import java.util.List;

public interface VendorStatisticsRepository {
    /**
     * Vendor별 통계 그리드 데이터 조회
     */
    List<VendorStatisticsRowDto> findVendorStatisticsGrid(VendorStatisticsRequest request);

    /**
     * Vendor별 통계 요약 조회
     */
    VendorStatisticsSummaryDto findVendorStatisticsSummary(VendorStatisticsRequest request);
}

