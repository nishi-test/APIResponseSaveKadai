package org.example.dto;

import org.h2.util.json.JSONObject;

import java.math.BigDecimal;

public class JsonValueDto {
    boolean success;
    String message;
    JSONObject estimatedData;
    Long classValue;
    BigDecimal confidence;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JSONObject getEstimatedData() {
        return estimatedData;
    }

    public void setEstimatedData(JSONObject estimatedData) {
        this.estimatedData = estimatedData;
    }

    public Long getClassValue() {
        return classValue;
    }

    public void setClassValue(Long classValue) {
        this.classValue = classValue;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }
}
