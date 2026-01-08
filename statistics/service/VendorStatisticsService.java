package com.htenc.isr.api.statistics.service;

import com.htenc.isr.api.statistics.dto.VendorStatisticsRequest;
import com.htenc.isr.api.statistics.dto.VendorStatisticsRowDto;
import com.htenc.isr.api.statistics.dto.VendorStatisticsSummaryDto;
import com.htenc.isr.api.statistics.repository.VendorStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VendorStatisticsService {

    private final VendorStatisticsRepository repository;

    /**
     * Vendor별 통계 그리드 데이터 조회
     */
    public List<VendorStatisticsRowDto> getVendorStatisticsGrid(VendorStatisticsRequest request) {
        return repository.findVendorStatisticsGrid(request);
    }

    /**
     * Vendor별 통계 요약 조회
     */
    public VendorStatisticsSummaryDto getVendorStatisticsSummary(VendorStatisticsRequest request) {
        return repository.findVendorStatisticsSummary(request);
    }
}

