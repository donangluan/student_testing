package org.example.student_testing.test.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RankLevel {

    private Integer rankId;

    private String rankCode;
    private String description;
    private BigDecimal minScore;
    private BigDecimal maxScore;
}
