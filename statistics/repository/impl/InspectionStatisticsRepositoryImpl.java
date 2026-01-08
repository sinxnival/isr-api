package com.htenc.isr.api.statistics.repository.impl;

import com.htenc.isr.api.application.type.ApplicationStatusType;
import com.htenc.isr.api.application.type.ApplicationType;
import com.htenc.isr.api.application.type.InspectionResultType;
import com.htenc.isr.api.statistics.dto.*;
import com.htenc.isr.api.statistics.repository.InspectionStatisticsRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;

import static com.htenc.isr.api.application.domain.QApplicationEntity.applicationEntity;
import static com.htenc.isr.api.application.domain.QApplicationItemEntity.applicationItemEntity;
import static com.htenc.isr.api.application.domain.QDispatchEntity.dispatchEntity;
import static com.htenc.isr.api.application.domain.QDispatchInspectorEntity.dispatchInspectorEntity;
import static com.htenc.isr.api.application.domain.QIrEntity.irEntity;
import static com.htenc.isr.api.application.domain.QItemMasterEntity.itemMasterEntity;
import static com.htenc.isr.api.application.domain.QItemTypeEntity.itemTypeEntity;
import static com.htenc.isr.api.application.domain.QItemTypeInspectionTypeEntity.itemTypeInspectionTypeEntity;
import static com.htenc.isr.api.application.domain.QNcrEntity.ncrEntity;
import static com.htenc.isr.api.application.domain.QProjectEntity.projectEntity;
import static com.htenc.isr.api.user.domain.QInspectorEntity.inspectorEntity;

@Repository
@RequiredArgsConstructor
public class InspectionStatisticsRepositoryImpl implements InspectionStatisticsRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<InspectionMainGridRowDto> findMainGrid(InspectionMainGridSearchCond cond) {

        // 1) QueryDSL Tuple 조회
        List<Tuple> rows = queryFactory
                .select(
                        applicationEntity.applicationNo,
                        applicationEntity.planStartDate,
                        projectEntity.jobName,
                        applicationEntity.vendorCompanyName,
                        applicationItemEntity.inspectionType,       // enum
                        inspectorEntity.inspectorName,              // String
                        applicationEntity.applicationStatusType     // enum
                )
                .from(applicationEntity)
                .leftJoin(applicationItemEntity).on(applicationItemEntity.applicationNo.eq(applicationEntity.applicationNo))
                .leftJoin(itemMasterEntity).on(itemMasterEntity.itemMasterNo.eq(applicationItemEntity.itemMasterNo))
                .leftJoin(projectEntity).on(projectEntity.jno.eq(applicationEntity.projectNo))
                .leftJoin(dispatchEntity).on(dispatchEntity.application.eq(applicationEntity))
                .leftJoin(dispatchInspectorEntity).on(dispatchInspectorEntity.dispatch.eq(dispatchEntity))
                .leftJoin(dispatchInspectorEntity.inspector, inspectorEntity)
                .where(
                        betweenDate(cond),
                        commonFilter(cond)
                )
                .orderBy(applicationEntity.planStartDate.desc(), applicationEntity.applicationNo.desc())
                .fetch();

        // 2) applicationNo 기준 그룹핑
        Map<Long, ApplicationGroupAcc> grouped = new LinkedHashMap<>();

        for (Tuple t : rows) {
            Long applNo = t.get(applicationEntity.applicationNo);

            ApplicationGroupAcc acc = grouped.computeIfAbsent(
                    applNo,
                    k -> ApplicationGroupAcc.fromFirstRow(k, t)
            );

            acc.accumulate(t);
        }

        // 3) Acc → DTO 변환
        return grouped.values().stream()
                .map(ApplicationGroupAcc::toDto)
                .toList();
    }
    /* ==================== 그룹핑 Accumulator ==================== */

    /**
     * applicationNo 기준으로 Tuple들을 모으는 Accumulator.
     * findMainGrid 메소드에서만 사용하는 중간 도메인 모델이라
     * private static final nested class 로 정의.
     */
    private static final class ApplicationGroupAcc {
        Long applNo;
        LocalDate planStartDate;
        String projectName;
        String vendorName;
        Set<String> inspectionTypes = new LinkedHashSet<>();
        Set<String> inspectorNames = new LinkedHashSet<>();
        ApplicationStatusType status;

        private ApplicationGroupAcc() {
        }

        static ApplicationGroupAcc fromFirstRow(Long applNo, Tuple t) {
            ApplicationGroupAcc acc = new ApplicationGroupAcc();
            acc.applNo = applNo;
            acc.planStartDate = t.get(applicationEntity.planStartDate);
            acc.projectName = t.get(projectEntity.jobName);
            acc.vendorName = t.get(applicationEntity.vendorCompanyName);
            acc.status = t.get(applicationEntity.applicationStatusType);
            return acc;
        }

        void accumulate(Tuple t) {
            // inspectionType (Enum → String)
            var inspTypeEnum = t.get(applicationItemEntity.inspectionType);
            if (inspTypeEnum != null) {
                inspectionTypes.add(inspTypeEnum.name());
            }

            // inspectorName
            String inspectorName = t.get(inspectorEntity.inspectorName);
            if (inspectorName != null) {
                inspectorNames.add(inspectorName);
            }
        }

        InspectionMainGridRowDto toDto() {
            return new InspectionMainGridRowDto(
                    applNo,
                    planStartDate,
                    projectName,
                    vendorName,
                    String.join(", ", inspectionTypes),
                    String.join(", ", inspectorNames),
                    status != null ? status.codeName() : null
            );
        }
    }

    @Override
    public List<InspectionMonthlyCountDto> countByMonth(InspectionMainGridSearchCond cond) {

        // year/month/count 표현식
        NumberExpression<Integer> yearExpr = applicationEntity.performEndDate.year();
        NumberExpression<Integer> monthExpr = applicationEntity.performEndDate.month();
        NumberExpression<Long> cntExpr = applicationEntity.applicationNo.countDistinct();

        // 1) QueryDSL Tuple 조회
        List<Tuple> rows = queryFactory
                .select(yearExpr, monthExpr, cntExpr)
                .from(applicationEntity)
                .leftJoin(applicationItemEntity).on(applicationItemEntity.applicationNo.eq(applicationEntity.applicationNo))
                .leftJoin(itemMasterEntity).on(itemMasterEntity.itemMasterNo.eq(applicationItemEntity.itemMasterNo))
                .leftJoin(projectEntity).on(projectEntity.jno.eq(applicationEntity.projectNo))
                .leftJoin(dispatchEntity).on(dispatchEntity.application.eq(applicationEntity))
                .leftJoin(dispatchInspectorEntity).on(dispatchInspectorEntity.dispatch.eq(dispatchEntity))
                .leftJoin(dispatchInspectorEntity.inspector, inspectorEntity)
                .where(
                        performEndDateBetween(cond),
                        applicationEntity.performEndDate.isNotNull(),
                        notCanceled(),
                        commonFilter(cond)
                )
                .groupBy(yearExpr, monthExpr)
                .orderBy(yearExpr.desc(), monthExpr.desc())
                .fetch();

        // 2) Tuple → DTO 변환
        return rows.stream()
                .map(t -> {
                    Integer year = t.get(yearExpr);
                    Integer month = t.get(monthExpr);
                    Long count = t.get(cntExpr);

                    YearMonth ym = (year != null && month != null) ? YearMonth.of(year, month) : null;

                    return new InspectionMonthlyCountDto(ym, count != null ? count : 0L);
                })
                .toList();
    }

    @Override
    public List<InspectionProjectMonthlyCountDto> countByProjectAndMonth(InspectionMainGridSearchCond cond) {

        // year / month / count 표현식
        NumberExpression<Integer> yearExpr = applicationEntity.performEndDate.year();
        NumberExpression<Integer> monthExpr = applicationEntity.performEndDate.month();
        NumberExpression<Long> cntExpr = applicationEntity.applicationNo.countDistinct();

        List<Tuple> rows = queryFactory
                .select(
                        projectEntity.jno,
                        projectEntity.jobName,
                        yearExpr,
                        monthExpr,
                        cntExpr
                )
                .from(applicationEntity)
                .leftJoin(applicationItemEntity).on(applicationItemEntity.applicationNo.eq(applicationEntity.applicationNo))
                .leftJoin(itemMasterEntity).on(itemMasterEntity.itemMasterNo.eq(applicationItemEntity.itemMasterNo))
                .leftJoin(projectEntity).on(projectEntity.jno.eq(applicationEntity.projectNo))
                .leftJoin(dispatchEntity).on(dispatchEntity.application.eq(applicationEntity))
                .leftJoin(dispatchInspectorEntity).on(dispatchInspectorEntity.dispatch.eq(dispatchEntity))
                .leftJoin(dispatchInspectorEntity.inspector, inspectorEntity)
                .where(
                        performEndDateBetween(cond),  // 종료일자 기준 between
                        applicationEntity.performEndDate.isNotNull(),
                        notCanceled(),                // 취소 제외
                        commonFilter(cond)
                )
                .groupBy(
                        projectEntity.jno,
                        projectEntity.jobName,
                        yearExpr,
                        monthExpr
                )
                .orderBy(
                        yearExpr.asc(),
                        monthExpr.asc()
                )
                .fetch();

        return rows.stream()
                .map(t -> {
                    Long projectNo = t.get(projectEntity.jno);
                    String projectName = t.get(projectEntity.jobName);

                    Integer year = t.get(yearExpr);
                    Integer month = t.get(monthExpr);
                    Long count = t.get(cntExpr);

                    YearMonth ym = (year != null && month != null) ? YearMonth.of(year, month) : null;

                    return new InspectionProjectMonthlyCountDto(
                            projectNo,
                            projectName,
                            ym,
                            count != null ? count : 0L
                    );
                })
                .toList();
    }

    @Override
    public List<FilterOptionDto> findAllProjectOptions() {
        return queryFactory
                .select(Projections.constructor(
                        FilterOptionDto.class,
                        projectEntity.jno.stringValue(),
                        projectEntity.jobName
                ))
                .from(applicationEntity)
                .join(projectEntity).on(projectEntity.jno.eq(applicationEntity.projectNo))
                .where(
                        projectEntity.jno.isNotNull()
                )
                .groupBy(projectEntity.jno, projectEntity.jobName)
                .orderBy(projectEntity.jobName.asc())
                .fetch();
    }

    @Override
    public List<FilterOptionDto> findAllVendorOptions() {
        return queryFactory
                .select(Projections.constructor(
                        FilterOptionDto.class,
                        applicationEntity.vendorCompanyNo.stringValue(),
                        applicationEntity.vendorCompanyName
                ))
                .from(applicationEntity)
                .where(
                        applicationEntity.vendorCompanyNo.isNotNull()
                )
                .groupBy(applicationEntity.vendorCompanyNo, applicationEntity.vendorCompanyName)
                .orderBy(applicationEntity.vendorCompanyName.asc())
                .fetch();
    }

    @Override
    public List<FilterOptionDto> findAllInspectorOptions() {
        return queryFactory
                .select(Projections.constructor(
                        FilterOptionDto.class,
                        inspectorEntity.inspectorNo.stringValue(),
                        inspectorEntity.inspectorName
                ))
                .from(dispatchInspectorEntity)
                .join(dispatchInspectorEntity.dispatch, dispatchEntity)
                .join(dispatchEntity.application, applicationEntity)
                .join(dispatchInspectorEntity.inspector, inspectorEntity)
                .where(
                        inspectorEntity.inspectorNo.isNotNull()
                )
                .groupBy(inspectorEntity.inspectorNo, inspectorEntity.inspectorName)
                .orderBy(inspectorEntity.inspectorName.asc())
                .fetch();
    }

    @Override
    public InspectionSummarySimpleDto fetchInspectionSummarySimple(InspectionMainGridSearchCond cond) {

        LocalDate today = LocalDate.now();

        NumberExpression<Long> total = applicationEntity.applicationNo.countDistinct();

        NumberExpression<Long> done = new CaseBuilder()
                .when(applicationEntity.applicationStatusType.eq(ApplicationStatusType.INSP_DONE))
                .then(applicationEntity.applicationNo)
                .otherwise((Long) null)
                .countDistinct();

        NumberExpression<Long> progress = new CaseBuilder()
                .when(applicationEntity.applicationStatusType.eq(ApplicationStatusType.INSP_PROGRESS))
                .then(applicationEntity.applicationNo)
                .otherwise((Long) null)
                .countDistinct();

        NumberExpression<Long> canceled = new CaseBuilder()
                .when(applicationEntity.applicationStatusType.eq(ApplicationStatusType.CANCELED))
                .then(applicationEntity.applicationNo)
                .otherwise((Long) null)
                .countDistinct();

        // APPL_TYPE=RESIDENT
        NumberExpression<Long> resident = new CaseBuilder()
                .when(applicationEntity.applicationType.eq(ApplicationType.RESIDENT))
                .then(applicationEntity.applicationNo)
                .otherwise((Long) null)
                .countDistinct();

        // 신규: regDate가 오늘
        BooleanExpression todayReg = applicationEntity.regDate.isNotNull()
                .and(applicationEntity.regDate.year().eq(today.getYear()))
                .and(applicationEntity.regDate.month().eq(today.getMonthValue()))
                .and(applicationEntity.regDate.dayOfMonth().eq(today.getDayOfMonth()));

        NumberExpression<Long> todayNew = new CaseBuilder()
                .when(todayReg)
                .then(applicationEntity.applicationNo)
                .otherwise((Long) null)
                .countDistinct();

        var tuple = queryFactory
                .select(total, done, progress, canceled, resident, todayNew)
                .from(applicationEntity)
                .where(
                        applicationEntity.applicationNo.isNotNull(),
                        commonFilter(cond)
                )
                .fetchOne();

        if (tuple == null) {
            return new InspectionSummarySimpleDto(0, 0, 0, 0, 0, 0);
        }

        return new InspectionSummarySimpleDto(
                nvl(tuple.get(total)),
                nvl(tuple.get(done)),
                nvl(tuple.get(progress)),
                nvl(tuple.get(canceled)),
                nvl(tuple.get(resident)),
                nvl(tuple.get(todayNew))
        );
    }

    /* ===================== 2) COMPLEX ===================== */
    @Override
    public InspectionSummaryComplexDto fetchInspectionSummaryComplex(InspectionMainGridSearchCond cond) {

        LocalDate today = LocalDate.now();

        // 지연
        BooleanExpression delayed = applicationEntity.performEndDate.isNotNull()
                .and(applicationEntity.performEndDate.lt(today))
                .and(
                        JPAExpressions.selectOne()
                                .from(irEntity)
                                .where(
                                        irEntity.applicationEntity.eq(applicationEntity)
                                )
                                .notExists()
                );

        Long unassignedCnt = queryFactory
                .select(applicationEntity.applicationNo.countDistinct())
                .from(applicationEntity)
                .leftJoin(dispatchEntity).on(dispatchEntity.application.eq(applicationEntity))
                // "유효한 inspector 배정 row"만 join 대상으로 인정
                .leftJoin(dispatchInspectorEntity).on(
                        dispatchInspectorEntity.dispatch.eq(dispatchEntity)
                                .and(dispatchInspectorEntity.inspector.isNotNull())
                                .and(dispatchInspectorEntity.inspector.inspectorNo.isNotNull())
                )
                .where(
                        applicationEntity.applicationNo.isNotNull(),
                        notCanceled(),
                        dispatchInspectorEntity.inspector.inspectorNo.isNull(),
                        commonFilter(cond)
                )
                .fetchOne();

        Long delayedCnt = queryFactory
                .select(applicationEntity.applicationNo.countDistinct())
                .from(applicationEntity)
                .where(
                        applicationEntity.applicationNo.isNotNull(),
                        delayed,
                        commonFilter(cond)
                )
                .fetchOne();

        Long ncrCnt = queryFactory
                .select(ncrEntity.ncrNo.count())
                .from(ncrEntity)
                .join(ncrEntity.applicationEntity, applicationEntity)
                .where(
                        applicationEntity.applicationNo.isNotNull(),
                        commonFilter(cond)
                )
                .fetchOne();

        return new InspectionSummaryComplexDto(
                nvl(unassignedCnt),
                nvl(delayedCnt),
                nvl(ncrCnt)
        );
    }

    /* ===================== 3) PROGRESS ===================== */
    @Override
    public InspectionSummaryProgressDto fetchInspectionSummaryProgress(InspectionMainGridSearchCond cond) {

        // ACCEPT면 percent, 아니면 0
        NumberExpression<Integer> acceptedPercent = new CaseBuilder()
                .when(applicationItemEntity.inspectionResultType.eq(InspectionResultType.ACCEPT))
                .then(itemTypeInspectionTypeEntity.percent)
                .otherwise(0);

        List<Integer> maxPercents = queryFactory
                .select(acceptedPercent.max())
                .from(applicationEntity)
                .join(applicationItemEntity).on(applicationItemEntity.applicationNo.eq(applicationEntity.applicationNo))
                .join(itemMasterEntity).on(itemMasterEntity.itemMasterNo.eq(applicationItemEntity.itemMasterNo))
                .join(itemTypeEntity).on(itemTypeEntity.itemTypeNo.eq(itemMasterEntity.itemTypeNo))
                .join(itemTypeInspectionTypeEntity).on(itemTypeInspectionTypeEntity.itemType.eq(itemTypeEntity))
                .where(
                        applicationEntity.applicationStatusType.ne(ApplicationStatusType.CANCELED),
                        applicationEntity.applicationNo.isNotNull(),
                        itemMasterEntity.itemMasterNo.isNotNull(),
                        itemTypeInspectionTypeEntity.percent.isNotNull(),
                        commonFilter(cond),
                        notCanceled()
                )
                .groupBy(itemMasterEntity.itemMasterNo)
                .fetch();

        if (maxPercents == null || maxPercents.isEmpty()) {
            return new InspectionSummaryProgressDto(0.0);
        }

        double avg = maxPercents.stream()
                .mapToInt(v -> v == null ? 0 : v)
                .average()
                .orElse(0.0);

        return new InspectionSummaryProgressDto(avg);
    }

    @Override
    public List<FilterOptionDto> findAllItemTypeOptions() {
        return queryFactory
                .select(Projections.constructor(
                        FilterOptionDto.class,
                        itemTypeEntity.itemTypeNo.stringValue(),
                        itemTypeEntity.itemTypeName
                ))
                .from(applicationItemEntity)
                .join(applicationItemEntity.applicationEntity, applicationEntity)
                .leftJoin(itemMasterEntity).on(itemMasterEntity.itemMasterNo.eq(applicationItemEntity.itemMasterNo))
                .leftJoin(itemTypeEntity).on(itemTypeEntity.itemTypeNo.eq(itemMasterEntity.itemTypeNo))
                .where(
                        itemTypeEntity.itemTypeNo.isNotNull()
                )
                .groupBy(itemTypeEntity.itemTypeNo, itemTypeEntity.itemTypeName)
                .orderBy(itemTypeEntity.itemTypeName.asc())
                .fetch();
    }

    /* ========= 조건 ========= */

    private BooleanExpression performEndDateBetween(InspectionMainGridSearchCond cond) {
        if (cond.fromDate() == null && cond.toDate() == null) return null;

        if (cond.fromDate() != null && cond.toDate() != null)
            return applicationEntity.performEndDate.between(cond.fromDate(), cond.toDate());

        if (cond.fromDate() != null)
            return applicationEntity.performEndDate.goe(cond.fromDate());

        return applicationEntity.performEndDate.loe(cond.toDate());
    }

    private BooleanExpression notCanceled() {
        return applicationEntity.applicationStatusType.ne(ApplicationStatusType.CANCELED);
    }

    private <T> BooleanExpression eqIfNotNull(T value, Function<T, BooleanExpression> exprFn) {
        return value == null ? null : exprFn.apply(value);
    }

    private BooleanExpression betweenDate(InspectionMainGridSearchCond cond) {
        if (cond.fromDate() == null && cond.toDate() == null) return null;

        if (cond.fromDate() != null && cond.toDate() != null) {
            return applicationEntity.planStartDate.between(cond.fromDate(), cond.toDate());
        }

        if (cond.fromDate() != null) {
            return applicationEntity.planStartDate.goe(cond.fromDate());
        }

        return applicationEntity.planStartDate.loe(cond.toDate());
    }

    private BooleanExpression eqProject(InspectionMainGridSearchCond cond) {
        return eqIfNotNull(cond.projectNo(), applicationEntity.projectNo::eq);
    }

    private BooleanExpression eqVendor(InspectionMainGridSearchCond cond) {
        return eqIfNotNull(cond.vendorCompNo(), applicationEntity.vendorCompanyNo::eq);
    }

//    private BooleanExpression eqInspector(InspectionMainGridSearchCond cond) {
//        return eqIfNotNull(cond.inspectorNo(), inspectorEntity.inspectorNo::eq);
//    }

    private BooleanExpression existsInspector(InspectionMainGridSearchCond cond) {
        if (cond.inspectorNo() == null) return null;

        return JPAExpressions
                .selectOne()
                .from(dispatchEntity)
                .join(dispatchInspectorEntity).on(dispatchInspectorEntity.dispatch.eq(dispatchEntity))
                .where(
                        dispatchEntity.application.eq(applicationEntity),
                        dispatchInspectorEntity.inspector.inspectorNo.eq(cond.inspectorNo())
                )
                .exists();
    }

    private Predicate commonFilter(InspectionMainGridSearchCond cond) {
        BooleanBuilder builder = new BooleanBuilder();

        BooleanExpression p = eqProject(cond);
        if (p != null) builder.and(p);

        BooleanExpression v = eqVendor(cond);
        if (v != null) builder.and(v);

        BooleanExpression i = existsInspector(cond);
        if (i != null) builder.and(i);

        return builder;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private long nvl(Long v) {
        return v == null ? 0L : v;
    }
}
