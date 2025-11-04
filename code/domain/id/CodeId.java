package com.htenc.isr.api.code.domain.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

/**
 * ISR_CODE_SET 복합키: CODE_GRP_ID + CODE
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@Embeddable
public class CodeId implements Serializable {

    @Column(name = "CODE_GRP_ID", nullable = false)
    private Long codeGrpId;

    @Column(name = "CODE", length = 50, nullable = false)
    private String code;
}