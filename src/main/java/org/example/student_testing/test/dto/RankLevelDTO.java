package org.example.student_testing.test.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RankLevelDTO {


    private Integer rankId;

    private String rankCode;
    private String description;
    private BigDecimal minScore;
    private BigDecimal maxScore;
}
