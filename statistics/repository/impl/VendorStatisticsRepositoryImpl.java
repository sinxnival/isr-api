package com.htenc.isr.api.statistics.repository.impl;

import com.htenc.isr.api.application.type.ApplicationStatusType;
import com.htenc.isr.api.statistics.dto.VendorStatisticsRequest;
import com.htenc.isr.api.statistics.dto.VendorStatisticsRowDto;
import com.htenc.isr.api.statistics.dto.VendorStatisticsSummaryDto;
import com.htenc.isr.api.statistics.repository.VendorStatisticsRepository;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.htenc.isr.api.application.domain.QApplicationEntity.applicationEntity;
import static com.htenc.isr.api.application.domain.QNcrEntity.ncrEntity;
import static com.htenc.isr.api.application.domain.QProjectEntity.projectEntity;

@Repository
@RequiredArgsConstructor
public class VendorStatisticsRepositoryImpl implements VendorStatisticsRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<VendorStatisticsRowDto> findVendorStatisticsGrid(VendorStatisticsRequest request) {
        // 검사 신청 건수 (ApplicationEntity 기준, 취소 제외)
        NumberExpression<Long> inspectionCountExpr = applicationEntity.applicationNo.countDistinct();

        // NCR 건수
        NumberExpression<Long> ncrCountExpr = ncrEntity.ncrNo.count();

        // Vendor별, Project별로 그룹화하여 집계
        List<Tuple> rows = queryFactory
                .select(
                        applicationEntity.vendorCompanyNo,
                        applicationEntity.vendorCompanyName,
                        projectEntity.jno,
                        projectEntity.jobName,
                        projectEntity.projectCode,
                        inspectionCountExpr,
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
                        eqVendor(request),
                        applicationEntity.vendorCompanyNo.isNotNull()
                )
                .groupBy(
                        applicationEntity.vendorCompanyNo,
                        applicationEntity.vendorCompanyName,
                        projectEntity.jno,
                        projectEntity.jobName,
                        projectEntity.projectCode
                )
                .orderBy(
                        applicationEntity.vendorCompanyName.asc(),
                        projectEntity.jobName.asc()
                )
                .fetch();

        // NCR Type별 집계를 위한 별도 쿼리
        Map<String, Map<Long, Map<String, Long>>> ncrTypeCountsByVendorAndProject = getNcrTypeCountsByVendorAndProject(request);

        return rows.stream()
                .map(t -> {
                    Long vendorCompNo = t.get(applicationEntity.vendorCompanyNo);
                    Long projectNo = t.get(projectEntity.jno);
                    Long inspectionCount = t.get(inspectionCountExpr);
                    Long ncrCount = t.get(ncrCountExpr);

                    // NCR 비율 계산: NCR 건수 / 검사 신청 건수 * 100
                    Double ncrRatio = (inspectionCount != null && inspectionCount > 0 && ncrCount != null)
                            ? (double) ncrCount / inspectionCount * 100.0
                            : 0.0;

                    // 해당 Vendor + Project의 NCR Type별 집계
                    Map<String, Long> ncrTypeCounts = ncrTypeCountsByVendorAndProject
                            .getOrDefault(String.valueOf(vendorCompNo), Collections.emptyMap())
                            .getOrDefault(projectNo, Collections.emptyMap());

                    return new VendorStatisticsRowDto(
                            vendorCompNo,
                            t.get(applicationEntity.vendorCompanyName),
                            projectNo,
                            t.get(projectEntity.jobName),
                            t.get(projectEntity.projectCode),
                            inspectionCount,
                            ncrCount,
                            ncrRatio,
                            ncrTypeCounts
                    );
                })
                .toList();
    }

    /**
     * NCR Type별 집계를 별도 쿼리로 조회
     * Map<vendorCompNo, Map<projectNo, Map<ncrType, count>>>
     */
    private Map<String, Map<Long, Map<String, Long>>> getNcrTypeCountsByVendorAndProject(VendorStatisticsRequest request) {
        List<Tuple> ncrTypeRows = queryFactory
                .select(
                        applicationEntity.vendorCompanyNo,
                        projectEntity.jno,
                        ncrEntity.ncrType,
                        ncrEntity.ncrNo.count()
                )
                .from(ncrEntity)
                .join(ncrEntity.applicationEntity, applicationEntity)
                .leftJoin(projectEntity).on(projectEntity.jno.eq(applicationEntity.projectNo))
                .where(
                        performEndDateBetween(request),
                        applicationEntity.performEndDate.isNotNull(),
                        notCanceled(),
//                        isCompleted(), // TODO 데이터가 없어 완료 필터 임시로 막아 놓음 (INSP_DONE)
                        eqProject(request),
                        eqVendor(request),
                        applicationEntity.vendorCompanyNo.isNotNull(),
                        ncrEntity.ncrType.isNotNull()
                )
                .groupBy(
                        applicationEntity.vendorCompanyNo,
                        projectEntity.jno,
                        ncrEntity.ncrType
                )
                .fetch();

        Map<String, Map<Long, Map<String, Long>>> result = new HashMap<>();
        for (Tuple t : ncrTypeRows) {
            Long vendorCompNo = t.get(applicationEntity.vendorCompanyNo);
            Long projectNo = t.get(projectEntity.jno);
            String ncrType = t.get(ncrEntity.ncrType);
            Long count = t.get(ncrEntity.ncrNo.count());

            if (vendorCompNo == null || projectNo == null || ncrType == null || count == null) {
                continue;
            }

            result.computeIfAbsent(String.valueOf(vendorCompNo), k -> new HashMap<>())
                    .computeIfAbsent(projectNo, k -> new HashMap<>())
                    .put(ncrType, count);
        }

        return result;
    }

    @Override
    public VendorStatisticsSummaryDto findVendorStatisticsSummary(VendorStatisticsRequest request) {
        // 총 검사 건수
        NumberExpression<Long> totalInspectionCountExpr = applicationEntity.applicationNo.countDistinct();

        // 프로젝트 개수 (DISTINCT)
        NumberExpression<Long> projectCountExpr = projectEntity.jno.countDistinct();

        // 총 NCR 건수
        NumberExpression<Long> totalNcrCountExpr = ncrEntity.ncrNo.count();

        Tuple summary = queryFactory
                .select(
                        totalInspectionCountExpr,
                        projectCountExpr,
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
                        eqVendor(request),
                        applicationEntity.vendorCompanyNo.isNotNull()
                )
                .fetchOne();

        if (summary == null) {
            return new VendorStatisticsSummaryDto(0L, 0, 0L, 0.0, Collections.emptyMap());
        }

        Long totalInspectionCount = summary.get(totalInspectionCountExpr);
        Long projectCount = summary.get(projectCountExpr);
        Long totalNcrCount = summary.get(totalNcrCountExpr);

        // 전체 NCR 비율 계산
        Double totalNcrRatio = (totalInspectionCount != null && totalInspectionCount > 0 && totalNcrCount != null)
                ? (double) totalNcrCount / totalInspectionCount * 100.0
                : 0.0;

        // NCR Type별 총 발생 횟수 집계
        Map<String, Long> ncrTypeCounts = getNcrTypeCounts(request);

        return new VendorStatisticsSummaryDto(
                totalInspectionCount != null ? totalInspectionCount : 0L,
                projectCount != null ? projectCount.intValue() : 0,
                totalNcrCount != null ? totalNcrCount : 0L,
                totalNcrRatio,
                ncrTypeCounts
        );
    }

    /**
     * NCR Type별 총 발생 횟수 집계
     */
    private Map<String, Long> getNcrTypeCounts(VendorStatisticsRequest request) {
        List<Tuple> ncrTypeRows = queryFactory
                .select(
                        ncrEntity.ncrType,
                        ncrEntity.ncrNo.count()
                )
                .from(ncrEntity)
                .join(ncrEntity.applicationEntity, applicationEntity)
                .leftJoin(projectEntity).on(projectEntity.jno.eq(applicationEntity.projectNo))
                .where(
                        performEndDateBetween(request),
                        applicationEntity.performEndDate.isNotNull(),
                        notCanceled(),
                        isCompleted(),
                        eqProject(request),
                        eqVendor(request),
                        applicationEntity.vendorCompanyNo.isNotNull(),
                        ncrEntity.ncrType.isNotNull()
                )
                .groupBy(ncrEntity.ncrType)
                .fetch();

        return ncrTypeRows.stream()
                .collect(Collectors.toMap(
                        t -> t.get(ncrEntity.ncrType),
                        t -> t.get(ncrEntity.ncrNo.count()),
                        Long::sum
                ));
    }

    /* ========= 조건 ========= */

    private BooleanExpression performEndDateBetween(VendorStatisticsRequest request) {
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

    private BooleanExpression eqProject(VendorStatisticsRequest request) {
        return eqIfNotNull(request.projectNo(), projectEntity.jno::eq);
    }

    private BooleanExpression eqVendor(VendorStatisticsRequest request) {
        return eqIfNotNull(request.vendorCompNo(), applicationEntity.vendorCompanyNo::eq);
    }
}

