package org.example.student_testing.test.entity;

import lombok.Data;

@Data
public class AnswerOption {

    private String optionCode;

    private String optionLabel;


    public String getOptionCode() {
        return optionCode;
    }

    public void setOptionCode(String optionCode) {
        this.optionCode = optionCode;
    }

    public String getOptionLabel() {
        return optionLabel;
    }

    public void setOptionLabel(String optionLabel) {
        this.optionLabel = optionLabel;
    }

    public AnswerOption(String optionCode, String optionLabel) {
        this.optionCode = optionCode;
        this.optionLabel = optionLabel;
    }
}
