package com.htenc.isr.api.statistics.service;

import com.htenc.isr.api.statistics.dto.ProjectStatisticsRequest;
import com.htenc.isr.api.statistics.dto.ProjectStatisticsRowDto;
import com.htenc.isr.api.statistics.dto.ProjectStatisticsSummaryDto;
import com.htenc.isr.api.statistics.repository.ProjectStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectStatisticsService {

    private final ProjectStatisticsRepository repository;

    /**
     * 프로젝트별 통계 그리드 데이터 조회
     */
    public List<ProjectStatisticsRowDto> getProjectStatisticsGrid(ProjectStatisticsRequest request) {
        return repository.findProjectStatisticsGrid(request);
    }

    /**
     * 프로젝트별 통계 요약 조회
     */
    public ProjectStatisticsSummaryDto getProjectStatisticsSummary(ProjectStatisticsRequest request) {
        return repository.findProjectStatisticsSummary(request);
    }
}

