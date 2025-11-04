package com.htenc.isr.api.code.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Comment("코드 그룹 테이블(ISR_CODE_GROUP)")
@Table(
        name = "ISR_CODE_GROUP",
        indexes = {
                @Index(name = "IDX_ISR_CODE_GROUP_USE",  columnList = "IS_USE"),
                @Index(name = "IDX_ISR_CODE_GROUP_SORT", columnList = "SORT_ORDER")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SequenceGenerator(name = "isr_code_grp_seq", sequenceName = "ISR_CODE_GRP_SEQ", allocationSize = 1)
public class CodeGroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "isr_code_grp_seq")
    @Column(name = "CODE_GRP_ID", nullable = false)
    @Comment("코드 그룹 식별자")
    private Long codeGrpId;

    @Column(name = "GROUP_CODE", length = 50, nullable = false)
    @Comment("그룹 코드(예: RISK_TYPE)")
    private String groupCode;

    @Column(name = "GROUP_NAME", length = 100, nullable = false)
    @Comment("그룹명")
    private String groupName;

    @Column(name = "IS_USE", length = 1, nullable = false)
    @Comment("사용 여부(Y/N) — DB DEFAULT 'Y'")
    private String isUse;

    @Column(name = "SORT_ORDER")
    @Comment("정렬 순서(NULL 허용)")
    private Long sortOrder;

    @Column(name = "DESCRIPTION")
    @Comment("설명")
    private String description;

    @Column(name = "REG_USER")
    @Comment("등록자(사용자 ID)")
    private Long regUser;

    @Column(name = "REG_DATE", columnDefinition = "TIMESTAMP", insertable = false, updatable = false)
    @Comment("등록 일시(DB 트리거)")
    private LocalDateTime regDate;

    @Column(name = "MOD_USER")
    @Comment("수정자(사용자 ID)")
    private Long modUser;

    @Column(name = "MOD_DATE", columnDefinition = "TIMESTAMP", insertable = false, updatable = false)
    @Comment("수정 일시(DB 트리거)")
    private LocalDateTime modDate;
}