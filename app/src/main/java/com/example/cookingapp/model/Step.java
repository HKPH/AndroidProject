package com.example.cookingapp.model;

import java.io.Serializable;

public class Step implements Serializable {
    private String stepName;
    private String stepDetail;

    public Step() {
        // Constructor không đối số được yêu cầu bởi Firestore
    }

    public Step(String stepName, String stepDetail) {
        this.stepName = stepName;
        this.stepDetail = stepDetail;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getStepDetail() {
        return stepDetail;
    }

    public void setStepDetail(String stepDetail) {
        this.stepDetail = stepDetail;
    }
}
