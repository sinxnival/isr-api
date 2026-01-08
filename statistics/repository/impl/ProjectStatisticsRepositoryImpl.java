package com.htenc.isr.api.statistics.repository.impl;

import com.htenc.isr.api.application.type.ApplicationStatusType;
import com.htenc.isr.api.statistics.dto.ProjectStatisticsRequest;
import com.htenc.isr.api.statistics.dto.ProjectStatisticsRowDto;
import com.htenc.isr.api.statistics.dto.ProjectStatisticsSummaryDto;
import com.htenc.isr.api.statistics.repository.ProjectStatisticsRepository;
import com.htenc.isr.api.user.type.UserType;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.Function;

import static com.htenc.isr.api.application.domain.QApplicationEntity.applicationEntity;
import static com.htenc.isr.api.application.domain.QDispatchEntity.dispatchEntity;
import static com.htenc.isr.api.application.domain.QDispatchInspectorEntity.dispatchInspectorEntity;
import static com.htenc.isr.api.application.domain.QNcrEntity.ncrEntity;
import static com.htenc.isr.api.application.domain.QProjectEntity.projectEntity;
import static com.htenc.isr.api.user.domain.QInspectorEntity.inspectorEntity;

@Repository
@RequiredArgsConstructor
public class ProjectStatisticsRepositoryImpl implements ProjectStatisticsRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ProjectStatisticsRowDto> findProjectStatisticsGrid(ProjectStatisticsRequest request) {
        // 완료된 검사 건수 (INSP_DONE만)
        NumberExpression<Long> totalInspectionCountExpr = applicationEntity.applicationNo.countDistinct();

        // NCR 건수
        NumberExpression<Long> ncrCountExpr = ncrEntity.ncrNo.count();

        // Project별로 그룹화하여 집계 (Inspector 건수는 별도 쿼리로 처리)
        List<Tuple> rows = queryFactory
                .select(
                        projectEntity.jno,
                        projectEntity.jobName,
                        projectEntity.projectCode,
                        totalInspectionCountExpr,
                        ncrCountExpr
                )
                .from(applicationEntity)
                .leftJoin(projectEntity).on(projectEntity.jno.eq(applicationEntity.projectNo))
                .leftJoin(ncrEntity).on(ncrEntity.applicationEntity.eq(applicationEntity))
                .where(
                        performEndDateBetween(request),
                        applicationEntity.performEndDate.isNotNull(),
                        notCanceled(),
//                        isCompleted(), // TODO 데이터가 없어 완료 필터 임시로 막아 놓음 (INSP_DONE)
                        eqProject(request),
                        projectEntity.jno.isNotNull()
                )
                .groupBy(
                        projectEntity.jno,
                        projectEntity.jobName,
                        projectEntity.projectCode
                )
                .orderBy(
                        projectEntity.jobName.asc()
                )
                .fetch();

        // Inspector 건수는 별도 쿼리로 집계 (DISTINCT 처리)
        java.util.Map<Long, InspectorCounts> inspectorCountsByProject = getInspectorCountsByProject(request);

        return rows.stream()
                .map(t -> {
                    Long projectNo = t.get(projectEntity.jno);
                    Long totalInspectionCount = t.get(totalInspectionCountExpr);
                    Long ncrCount = t.get(ncrCountExpr);

                    InspectorCounts inspectorCounts = inspectorCountsByProject.getOrDefault(projectNo, new InspectorCounts(0L, 0L));

                    // 검사 진행률 계산: 완료된 검사 건수는 이미 필터링되어 있으므로 100%
                    Double inspectionProgressRate = 100.0;

                    // NCR 비율 계산: NCR 건수 / 검사 신청 건수 * 100
                    Double ncrRatio = (totalInspectionCount != null && totalInspectionCount > 0 && ncrCount != null)
                            ? (double) ncrCount / totalInspectionCount * 100.0
                            : 0.0;

                    return new ProjectStatisticsRowDto(
                            projectNo,
                            t.get(projectEntity.jobName),
                            t.get(projectEntity.projectCode),
                            totalInspectionCount,
                            inspectionProgressRate,
                            ncrCount,
                            ncrRatio,
                            inspectorCounts.internalCount,
                            inspectorCounts.externalCount
                    );
                })
                .toList();
    }

    /**
     * 프로젝트별 Inspector 건수 집계 (DISTINCT)
     */
    private java.util.Map<Long, InspectorCounts> getInspectorCountsByProject(ProjectStatisticsRequest request) {
        List<Tuple> inspectorRows = queryFactory
                .select(
                        projectEntity.jno,
                        inspectorEntity.inspectorNo,
                        inspectorEntity.userType
                )
                .from(applicationEntity)
                .leftJoin(projectEntity).on(projectEntity.jno.eq(applicationEntity.projectNo))
                .leftJoin(dispatchEntity).on(dispatchEntity.application.eq(applicationEntity))
                .leftJoin(dispatchInspectorEntity).on(dispatchInspectorEntity.dispatch.eq(dispatchEntity))
                .leftJoin(dispatchInspectorEntity.inspector, inspectorEntity)
                .where(
                        performEndDateBetween(request),
                        applicationEntity.performEndDate.isNotNull(),
                        notCanceled(),
//                        isCompleted(), // TODO 데이터가 없어 완료 필터 임시로 막아 놓음 (INSP_DONE)
                        eqProject(request),
                        projectEntity.jno.isNotNull(),
                        inspectorEntity.inspectorNo.isNotNull()
                )
                .distinct()
                .fetch();

        java.util.Map<Long, InspectorCounts> result = new java.util.HashMap<>();
        for (Tuple t : inspectorRows) {
            Long projectNo = t.get(projectEntity.jno);
            UserType userType = t.get(inspectorEntity.userType);

            if (projectNo == null || userType == null) {
                continue;
            }

            InspectorCounts counts = result.computeIfAbsent(projectNo, k -> new InspectorCounts(0L, 0L));
            if (userType == UserType.BIZ) {
                counts.internalCount++;
            } else if (userType == UserType.PARTNER || userType == UserType.LOCAL) {
                counts.externalCount++;
            }
        }

        return result;
    }

    /**
     * Inspector 건수 보관용 내부 클래스
     */
    private static class InspectorCounts {
        long internalCount;
        long externalCount;

        InspectorCounts(long internalCount, long externalCount) {
            this.internalCount = internalCount;
            this.externalCount = externalCount;
        }
    }

    @Override
    public ProjectStatisticsSummaryDto findProjectStatisticsSummary(ProjectStatisticsRequest request) {
        // 완료된 검사 건수 (INSP_DONE만)
        NumberExpression<Long> totalInspectionCountExpr = applicationEntity.applicationNo.countDistinct();

        // 총 NCR 건수
        NumberExpression<Long> totalNcrCountExpr = ncrEntity.ncrNo.count();

        Tuple summary = queryFactory
                .select(
                        totalInspectionCountExpr,
                        totalNcrCountExpr
                )
                .from(applicationEntity)
                .leftJoin(projectEntity).on(projectEntity.jno.eq(applicationEntity.projectNo))
                .leftJoin(ncrEntity).on(ncrEntity.applicationEntity.eq(applicationEntity))
                .where(
                        performEndDateBetween(request),
                        applicationEntity.performEndDate.isNotNull(),
                        notCanceled(),
//                        isCompleted(), // TODO 데이터가 없어 완료 필터 임시로 막아 놓음 (INSP_DONE)
                        eqProject(request),
                        projectEntity.jno.isNotNull()
                )
                .fetchOne();

        // Inspector 건수는 별도 쿼리로 집계
        InspectorCounts totalInspectorCounts = getTotalInspectorCounts(request);

        if (summary == null) {
            return new ProjectStatisticsSummaryDto(0L, 100.0, 0L, 0.0, totalInspectorCounts.internalCount, totalInspectorCounts.externalCount);
        }

        Long totalInspectionCount = summary.get(totalInspectionCountExpr);
        Long totalNcrCount = summary.get(totalNcrCountExpr);

        // 전체 검사 진행률: 완료된 건수만 조회하므로 100%
        Double inspectionProgressRate = 100.0;

        // 전체 NCR 비율 계산
        Double totalNcrRatio = (totalInspectionCount != null && totalInspectionCount > 0 && totalNcrCount != null)
                ? (double) totalNcrCount / totalInspectionCount * 100.0
                : 0.0;

        return new ProjectStatisticsSummaryDto(
                totalInspectionCount != null ? totalInspectionCount : 0L,
                inspectionProgressRate,
                totalNcrCount != null ? totalNcrCount : 0L,
                totalNcrRatio,
                totalInspectorCounts.internalCount,
                totalInspectorCounts.externalCount
        );
    }

    /**
     * 전체 Inspector 건수 집계 (DISTINCT)
     */
    private InspectorCounts getTotalInspectorCounts(ProjectStatisticsRequest request) {
        List<Tuple> inspectorRows = queryFactory
                .select(
                        inspectorEntity.inspectorNo,
                        inspectorEntity.userType
                )
                .from(applicationEntity)
                .leftJoin(projectEntity).on(projectEntity.jno.eq(applicationEntity.projectNo))
                .leftJoin(dispatchEntity).on(dispatchEntity.application.eq(applicationEntity))
                .leftJoin(dispatchInspectorEntity).on(dispatchInspectorEntity.dispatch.eq(dispatchEntity))
                .leftJoin(dispatchInspectorEntity.inspector, inspectorEntity)
                .where(
                        performEndDateBetween(request),
                        applicationEntity.performEndDate.isNotNull(),
                        notCanceled(),
//                        isCompleted(), // TODO 데이터가 없어 완료 필터 임시로 막아 놓음 (INSP_DONE)
                        eqProject(request),
                        projectEntity.jno.isNotNull(),
                        inspectorEntity.inspectorNo.isNotNull()
                )
                .distinct()
                .fetch();

        InspectorCounts counts = new InspectorCounts(0L, 0L);
        for (Tuple t : inspectorRows) {
            UserType userType = t.get(inspectorEntity.userType);
            if (userType == null) {
                continue;
            }
            if (userType == UserType.BIZ) {
                counts.internalCount++;
            } else if (userType == UserType.PARTNER || userType == UserType.LOCAL) {
                counts.externalCount++;
            }
        }

        return counts;
    }

    /* ========= 조건 ========= */

    private BooleanExpression performEndDateBetween(ProjectStatisticsRequest request) {
        if (request.fromDate() == null && request.toDate() == null) return null;

        if (request.fromDate() != null && request.toDate() != null)
            return applicationEntity.performEndDate.between(request.fromDate(), request.toDate());

        if (request.fromDate() != null)
            return applicationEntity.performEndDate.goe(request.fromDate());

        return applicationEntity.performEndDate.loe(request.toDate());
    }

    private BooleanExpression notCanceled() {
        return applicationEntity.applicationStatusType.ne(ApplicationStatusType.CANCELED);
    }

    private BooleanExpression isCompleted() {
        return applicationEntity.applicationStatusType.eq(ApplicationStatusType.INSP_DONE);
    }

    private <T> BooleanExpression eqIfNotNull(T value, Function<T, BooleanExpression> exprFn) {
        return value == null ? null : exprFn.apply(value);
    }

    private BooleanExpression eqProject(ProjectStatisticsRequest request) {
        return eqIfNotNull(request.projectNo(), projectEntity.jno::eq);
    }
}

