package com.example.festival_management.entity.enums;

public enum PerformanceStatus {
    CREATED,
    SUBMITTED,
    ASSIGNED,       // έχει ανατεθεί staff reviewer/handler
    REVIEWED,       // έχει γραφτεί review
    APPROVED,
    REJECTED,
    PENDING,
    FINAL_SUBMITTED,
    ACCEPTED        // μπήκε στο τελικό lineup
}
