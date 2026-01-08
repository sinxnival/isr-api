package com.htenc.isr.api.statistics.repository.impl;

import com.htenc.isr.api.application.type.ApplicationStatusType;
import com.htenc.isr.api.statistics.dto.ItemTypeStatisticsRequest;
import com.htenc.isr.api.statistics.dto.ItemTypeStatisticsRowDto;
import com.htenc.isr.api.statistics.dto.ItemTypeStatisticsSummaryDto;
import com.htenc.isr.api.statistics.repository.ItemTypeStatisticsRepository;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.Function;

import static com.htenc.isr.api.application.domain.QApplicationEntity.applicationEntity;
import static com.htenc.isr.api.application.domain.QApplicationItemEntity.applicationItemEntity;
import static com.htenc.isr.api.application.domain.QItemMasterEntity.itemMasterEntity;
import static com.htenc.isr.api.application.domain.QItemTypeEntity.itemTypeEntity;
import static com.htenc.isr.api.application.domain.QProjectEntity.projectEntity;

@Repository
@RequiredArgsConstructor
public class ItemTypeStatisticsRepositoryImpl implements ItemTypeStatisticsRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ItemTypeStatisticsRowDto> findItemTypeStatisticsGrid(ItemTypeStatisticsRequest request) {

        // 검사 횟수 = 신청서 건수 (Application 기준)
        NumberExpression<Long> inspectionCountExpr =
                applicationEntity.applicationNo.countDistinct();

        // 아이템 갯수 = ITEM_MASTER_NO 기준 중복 제거
        NumberExpression<Long> itemCountExpr =
                itemMasterEntity.itemMasterNo.countDistinct();

        List<Tuple> rows = queryFactory
                .select(
                        itemTypeEntity.itemTypeNo,
                        itemTypeEntity.itemTypeCode,
                        itemTypeEntity.itemTypeName,
                        inspectionCountExpr,
                        itemCountExpr,
                        projectEntity.jno,
                        projectEntity.jobName,
                        projectEntity.projectCode
                )
                .from(applicationItemEntity)
                .join(applicationItemEntity.applicationEntity, applicationEntity)
                .leftJoin(itemMasterEntity).on(itemMasterEntity.itemMasterNo.eq(applicationItemEntity.itemMasterNo))
                .leftJoin(itemTypeEntity).on(itemTypeEntity.itemTypeNo.eq(itemMasterEntity.itemTypeNo))
                .leftJoin(projectEntity).on(projectEntity.jno.eq(applicationItemEntity.projectNo))
                .where(
                        performEndDateBetween(request),
                        applicationEntity.performEndDate.isNotNull(),
                        notCanceled(),
                        eqProject(request),
                        eqVendor(request),
                        eqItemType(request),
                        itemTypeEntity.itemTypeNo.isNotNull(),
                        itemMasterEntity.itemMasterNo.isNotNull()
                )
                .groupBy(
                        itemTypeEntity.itemTypeNo,
                        itemTypeEntity.itemTypeCode,
                        itemTypeEntity.itemTypeName,
                        projectEntity.jno,
                        projectEntity.jobName,
                        projectEntity.projectCode
                )
                .orderBy(
                        itemTypeEntity.itemTypeName.asc(),
                        projectEntity.jobName.asc()
                )
                .fetch();

        return rows.stream()
                .map(t -> new ItemTypeStatisticsRowDto(
                        t.get(itemTypeEntity.itemTypeNo),
                        t.get(itemTypeEntity.itemTypeCode),
                        t.get(itemTypeEntity.itemTypeName),
                        t.get(inspectionCountExpr),   // ✅ 신청서 건수
                        null,                         // 평균은 Service에서 계산
                        t.get(itemCountExpr),         // ✅ ITEM_MASTER_NO distinct
                        t.get(projectEntity.jno),
                        t.get(projectEntity.jobName),
                        t.get(projectEntity.projectCode)
                ))
                .toList();
    }

    @Override
    public ItemTypeStatisticsSummaryDto findItemTypeStatisticsSummary(ItemTypeStatisticsRequest request) {

        // 총 검사 횟수 = 신청서 건수 (Application 기준)
        NumberExpression<Long> totalCountExpr =
                applicationEntity.applicationNo.countDistinct();

        // 총 아이템 갯수 = ITEM_MASTER_NO 기준 중복 제거
        NumberExpression<Long> itemCountExpr =
                itemMasterEntity.itemMasterNo.countDistinct();

        // Item Type 개수 (DISTINCT)
        NumberExpression<Long> itemTypeCountExpr =
                itemTypeEntity.itemTypeNo.countDistinct();

        // 프로젝트 개수 (DISTINCT)
        NumberExpression<Long> projectCountExpr =
                projectEntity.jno.countDistinct();

        Tuple summary = queryFactory
                .select(
                        totalCountExpr,
                        itemCountExpr,
                        itemTypeCountExpr,
                        projectCountExpr
                )
                .from(applicationItemEntity)
                .join(applicationItemEntity.applicationEntity, applicationEntity)
                .leftJoin(itemMasterEntity).on(itemMasterEntity.itemMasterNo.eq(applicationItemEntity.itemMasterNo))
                .leftJoin(itemTypeEntity).on(itemTypeEntity.itemTypeNo.eq(itemMasterEntity.itemTypeNo))
                .leftJoin(projectEntity).on(projectEntity.jno.eq(applicationItemEntity.projectNo))
                .where(
                        performEndDateBetween(request),
                        applicationEntity.performEndDate.isNotNull(),
                        notCanceled(),
                        eqProject(request),
                        eqVendor(request),
                        eqItemType(request),
                        itemTypeEntity.itemTypeNo.isNotNull(),
                        itemMasterEntity.itemMasterNo.isNotNull()
                )
                .fetchOne();

        if (summary == null) {
            return new ItemTypeStatisticsSummaryDto(0L, 0.0, 0, 0, 0);
        }

        Long totalCount    = summary.get(totalCountExpr);
        Long itemCount     = summary.get(itemCountExpr);
        Long itemTypeCount = summary.get(itemTypeCountExpr);
        Long projectCount  = summary.get(projectCountExpr);

        Double averageCount =
                (itemTypeCount != null && itemTypeCount > 0 && itemCount != null)
                        ? (double) itemCount / itemTypeCount
                        : 0.0;

        return new ItemTypeStatisticsSummaryDto(
                totalCount != null ? totalCount : 0L,
                averageCount,
                itemTypeCount != null ? itemTypeCount.intValue() : 0,
                itemCount != null ? itemCount.intValue() : 0,
                projectCount != null ? projectCount.intValue() : 0
        );
    }

    /* ========= 조건 ========= */

    private BooleanExpression performEndDateBetween(ItemTypeStatisticsRequest request) {
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

    private BooleanExpression eqProject(ItemTypeStatisticsRequest request) {
        return eqIfNotNull(request.projectNo(), projectEntity.jno::eq);
    }

    private BooleanExpression eqVendor(ItemTypeStatisticsRequest request) {
        return eqIfNotNull(request.vendorCompNo(), applicationEntity.vendorCompanyNo::eq);
    }

    private BooleanExpression eqItemType(ItemTypeStatisticsRequest request) {
        return eqIfNotNull(request.itemTypeNo(), itemTypeEntity.itemTypeNo::eq);
    }
}

