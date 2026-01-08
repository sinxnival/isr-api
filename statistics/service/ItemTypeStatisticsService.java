package com.htenc.isr.api.statistics.service;

import com.htenc.isr.api.statistics.dto.ItemTypeStatisticsRequest;
import com.htenc.isr.api.statistics.dto.ItemTypeStatisticsRowDto;
import com.htenc.isr.api.statistics.dto.ItemTypeStatisticsSummaryDto;
import com.htenc.isr.api.statistics.repository.ItemTypeStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemTypeStatisticsService {

    private final ItemTypeStatisticsRepository repository;

    /**
     * Item Type별 통계 그리드 데이터 조회
     * 평균 검사 횟수 계산: Item Type별로 프로젝트별 검사 횟수의 평균
     */
    public List<ItemTypeStatisticsRowDto> getItemTypeStatisticsGrid(ItemTypeStatisticsRequest request) {
        List<ItemTypeStatisticsRowDto> rows = repository.findItemTypeStatisticsGrid(request);

        // Item Type별로 그룹화하여 평균 계산
        Map<Long, List<ItemTypeStatisticsRowDto>> groupedByItemType = rows.stream()
                .collect(Collectors.groupingBy(ItemTypeStatisticsRowDto::itemTypeNo));

        // 각 Item Type별로 프로젝트별 검사 횟수의 평균 계산
        return rows.stream()
                .map(row -> {
                    List<ItemTypeStatisticsRowDto> sameItemTypeRows = groupedByItemType.get(row.itemTypeNo());
                    if (sameItemTypeRows == null || sameItemTypeRows.isEmpty()) {
                        return row;
                    }

                    // 프로젝트별 검사 횟수의 평균
                    double average = sameItemTypeRows.stream()
                            .mapToLong(ItemTypeStatisticsRowDto::inspectionCount)
                            .average()
                            .orElse(0.0);

                    return new ItemTypeStatisticsRowDto(
                            row.itemTypeNo(),
                            row.itemTypeCode(),
                            row.itemTypeName(),
                            row.inspectionCount(),
                            average,
                            row.itemCount(),
                            row.projectNo(),
                            row.projectName(),
                            row.projectCode()
                    );
                })
                .toList();
    }

    /**
     * Item Type별 통계 요약 조회
     */
    public ItemTypeStatisticsSummaryDto getItemTypeStatisticsSummary(ItemTypeStatisticsRequest request) {
        return repository.findItemTypeStatisticsSummary(request);
    }
}

