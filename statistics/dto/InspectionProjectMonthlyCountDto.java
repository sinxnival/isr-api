package com.htenc.isr.api.statistics.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.htenc.isr.api.common.validation.YearMonthDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.YearMonth;

@Schema(description = "프로젝트별 월별 검사 건수")
public record InspectionProjectMonthlyCountDto(

        @Schema(description = "프로젝트 번호(JNO)")
        Long projectNo,

        @Schema(description = "프로젝트명")
        String projectName,

        @JsonDeserialize(using = YearMonthDeserializer.class)
        @Schema(description = "통계 년월 (yyyy-MM 또는 yyyyMM)")
        YearMonth statisticsYearMonth,

        @Schema(description = "검사 건수(취소 제외)")
        long count
) {
}

