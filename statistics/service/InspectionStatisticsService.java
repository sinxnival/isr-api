package com.htenc.isr.api.statistics.service;

import com.htenc.isr.api.statistics.dto.*;
import com.htenc.isr.api.statistics.repository.InspectionStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InspectionStatisticsService {

    private final InspectionStatisticsRepository statisticsRepository;

    /**
     * 메인 대시보드용 검사 그리드 조회
     */
    public List<InspectionMainGridRowDto> getMainGrid(InspectionMainGridSearchCond cond) {
        return statisticsRepository.findMainGrid(cond);
    }

    /**
     * 월별 검사 건수 조회 (검사 종료일 기준, 취소 제외)
     * - fromDate ~ toDate 사이의 모든 월
     * 데이터가 없으면 count = 0 으로 채워서 리턴
     */
    public List<InspectionMonthlyCountDto> getMonthlyCounts(InspectionMainGridSearchCond cond) {

        // 1) 실제 DB에서 집계된 (년-월, 건수)만 가져옴
        List<InspectionMonthlyCountDto> rows = statisticsRepository.countByMonth(cond);

        // 데이터 자체가 하나도 없으면 바로 리턴
        if (rows.isEmpty()) {
            return rows;
        }

        // 2) DB에서 온 YearMonth 기준으로 최소/최대 YearMonth 계산
        YearMonth minYm = rows.stream()
                .map(InspectionMonthlyCountDto::statisticsYearMonth)
                .min(Comparator.naturalOrder())
                .orElseThrow();

        YearMonth maxYm = rows.stream()
                .map(InspectionMonthlyCountDto::statisticsYearMonth)
                .max(Comparator.naturalOrder())
                .orElse(minYm);

        // 3) 실제 사용할 startYm / endYm 결정 (A 방식)
        YearMonth startYm;
        YearMonth endYm;

        if (cond.fromDate() != null && cond.toDate() != null) {
            // 둘 다 있으면 그대로 구간 사용
            startYm = YearMonth.from(cond.fromDate());
            endYm = YearMonth.from(cond.toDate());
        } else if (cond.fromDate() != null) {
            // from만 있으면 → from 월 ~ 데이터가 있는 마지막 월
            startYm = YearMonth.from(cond.fromDate());
            endYm = maxYm;
        } else if (cond.toDate() != null) {
            // to만 있으면 → 데이터가 있는 첫 월 ~ to 월
            startYm = minYm;
            endYm = YearMonth.from(cond.toDate());
        } else {
            // 둘 다 없으면 → 데이터 기준 전체 구간
            startYm = minYm;
            endYm = maxYm;
        }

        // 4) rows -> Map<YearMonth, count> 로 변환
        Map<YearMonth, Long> countByYm = rows.stream()
                .collect(Collectors.toMap(
                        InspectionMonthlyCountDto::statisticsYearMonth,
                        InspectionMonthlyCountDto::count
                ));

        // 5) startYm ~ endYm 사이 모든 월을 돌면서, 없으면 0으로 채워서 DTO 생성
        List<InspectionMonthlyCountDto> result = new ArrayList<>();

        for (YearMonth ym = startYm; !ym.isAfter(endYm); ym = ym.plusMonths(1)) {
            long count = countByYm.getOrDefault(ym, 0L);
            result.add(new InspectionMonthlyCountDto(ym, count));
        }

        return result;
    }

    /**
     * 프로젝트별 월별 검사 건수 조회 (검사 종료일 기준, 취소 제외, 빈 월은 0으로 채움)
     */
    public List<InspectionProjectMonthlyCountDto> getProjectMonthlyCounts(InspectionMainGridSearchCond cond) {

        // 1) DB에서 실제 존재하는 (프로젝트,년월) 통계 조회
        List<InspectionProjectMonthlyCountDto> raw = statisticsRepository.countByProjectAndMonth(cond);
        if (raw.isEmpty()) {
            return raw;
        }

        // 2) from/to 기준으로 YearMonth 범위 계산
        YearMonth startYm;
        YearMonth endYm;

        // 요청에 from/to가 있으면 그걸 우선 사용
        if (cond.fromDate() != null && cond.toDate() != null) {
            startYm = YearMonth.from(cond.fromDate());
            endYm = YearMonth.from(cond.toDate());
        } else {
            // 없으면 실제 데이터 기준 최소/최대 YearMonth 사용
            YearMonth minYm = raw.stream()
                    .map(InspectionProjectMonthlyCountDto::statisticsYearMonth)
                    .min(Comparator.naturalOrder())
                    .orElse(YearMonth.now());

            YearMonth maxYm = raw.stream()
                    .map(InspectionProjectMonthlyCountDto::statisticsYearMonth)
                    .max(Comparator.naturalOrder())
                    .orElse(minYm);

            startYm = cond.fromDate() != null ? YearMonth.from(cond.fromDate()) : minYm;
            endYm = cond.toDate() != null ? YearMonth.from(cond.toDate()) : maxYm;
        }

        // startYm ~ endYm 사이 month 리스트 생성
        List<YearMonth> monthRange = new ArrayList<>();
        for (YearMonth ym = startYm; !ym.isAfter(endYm); ym = ym.plusMonths(1)) {
            monthRange.add(ym);
        }

        // 3) 프로젝트별로 (YearMonth -> count) 맵 구성
        //    + 프로젝트 번호 → 이름 매핑
        Map<Long, String> projectNameByNo = new HashMap<>();
        Map<Long, Map<YearMonth, Long>> countByProject = new LinkedHashMap<>();

        for (InspectionProjectMonthlyCountDto dto : raw) {
            Long projectNo = dto.projectNo();
            if (projectNo == null) continue;

            projectNameByNo.putIfAbsent(projectNo, dto.projectName());

            YearMonth ym = dto.statisticsYearMonth();
            countByProject
                    .computeIfAbsent(projectNo, k -> new HashMap<>())
                    .put(ym, dto.count());
        }

        // 4) 모든 프로젝트 × monthRange 조합에 대해 값 채우기 (없으면 0)
        List<InspectionProjectMonthlyCountDto> filled = new ArrayList<>();

        for (Map.Entry<Long, Map<YearMonth, Long>> entry : countByProject.entrySet()) {
            Long projectNo = entry.getKey();
            String projectName = projectNameByNo.get(projectNo);
            Map<YearMonth, Long> perMonth = entry.getValue();

            for (YearMonth ym : monthRange) {
                long count = perMonth.getOrDefault(ym, 0L);
                filled.add(new InspectionProjectMonthlyCountDto(
                        projectNo,
                        projectName,
                        ym,
                        count
                ));
            }
        }

        // 5) 정렬: 프로젝트 번호 → 년월 순
        filled.sort(
                Comparator
                        .comparing(InspectionProjectMonthlyCountDto::projectNo, Comparator.nullsFirst(Long::compareTo))
                        .thenComparing(InspectionProjectMonthlyCountDto::statisticsYearMonth)
        );

        return filled;
    }

    /**
     * 상태별 집계 합치기
     */
    public InspectionSummaryDto getInspectionSummary(InspectionMainGridSearchCond cond) {
        InspectionSummarySimpleDto s = statisticsRepository.fetchInspectionSummarySimple(cond);
        InspectionSummaryComplexDto c = statisticsRepository.fetchInspectionSummaryComplex(cond);
        InspectionSummaryProgressDto p = statisticsRepository.fetchInspectionSummaryProgress(cond);

        return new InspectionSummaryDto(
                s.totalCount(),
                s.doneCount(),
                s.progressCount(),
                s.canceledCount(),
                s.residentCount(),
                s.todayNewCount(),
                c.unassignedCount(),
                c.delayedCount(),
                c.ncrCount(),
                p.progressRate()
        );
    }

    public List<FilterOptionDto> getAllProjectOptions() {
        return statisticsRepository.findAllProjectOptions();
    }

    public List<FilterOptionDto> getAllVendorOptions() {
        return statisticsRepository.findAllVendorOptions();
    }

    public List<FilterOptionDto> getAllInspectorOptions() {
        return statisticsRepository.findAllInspectorOptions();
    }

    public List<FilterOptionDto> getAllItemTypeOptions() {
        return statisticsRepository.findAllItemTypeOptions();
    }
}
