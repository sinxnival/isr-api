package com.htenc.isr.api.code.domain;

import com.htenc.isr.api.code.domain.id.CodeId;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * ISR_CODE_SET 매핑: 코드 아이템 테이블
 */
@Entity
@Comment("코드 아이템 테이블(ISR_CODE_SET)")
@Table(
        name = "ISR_CODE_SET",
        indexes = {
                @Index(name = "IDX_ISR_CODE_SET_GRP_SORT", columnList = "CODE_GRP_ID, SORT_ORDER"),
                @Index(name = "IDX_ISR_CODE_SET_USE", columnList = "IS_USE")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeEntity {

    @EmbeddedId
    @Comment("복합키 (CODE_GRP_ID + CODE)")
    private CodeId id;

    @Column(name = "CODE_VAL", length = 100, nullable = false)
    @Comment("코드 값(표시명)")
    private String codeVal;

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