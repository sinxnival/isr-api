package com.htenc.isr.api.statistics.repository;

import com.htenc.isr.api.statistics.dto.ItemTypeStatisticsRequest;
import com.htenc.isr.api.statistics.dto.ItemTypeStatisticsRowDto;
import com.htenc.isr.api.statistics.dto.ItemTypeStatisticsSummaryDto;

import java.util.List;

public interface ItemTypeStatisticsRepository {
    /**
     * Item Type별 통계 그리드 데이터 조회
     */
    List<ItemTypeStatisticsRowDto> findItemTypeStatisticsGrid(ItemTypeStatisticsRequest request);

    /**
     * Item Type별 통계 요약 조회
     */
    ItemTypeStatisticsSummaryDto findItemTypeStatisticsSummary(ItemTypeStatisticsRequest request);
}

