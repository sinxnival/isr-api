package com.htenc.isr.api.statistics.repository;

import com.htenc.isr.api.statistics.dto.ProjectStatisticsRequest;
import com.htenc.isr.api.statistics.dto.ProjectStatisticsRowDto;
import com.htenc.isr.api.statistics.dto.ProjectStatisticsSummaryDto;

import java.util.List;

public interface ProjectStatisticsRepository {
    /**
     * 프로젝트별 통계 그리드 데이터 조회
     */
    List<ProjectStatisticsRowDto> findProjectStatisticsGrid(ProjectStatisticsRequest request);

    /**
     * 프로젝트별 통계 요약 조회
     */
    ProjectStatisticsSummaryDto findProjectStatisticsSummary(ProjectStatisticsRequest request);
}

