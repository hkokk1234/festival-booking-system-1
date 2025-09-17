package com.example.festival_management.entity;

public class Enums {

    public enum RoleType {
        VISITOR,
        ARTIST,
        STAFF,
        ORGANIZER
    }

    public enum FestivalState {
        CREATED,
        SUBMISSION,
        ASSIGNMENT,
        REVIEW,
        SCHEDULING,
        FINAL_SUBMISSION,
        DECISION,
        ANNOUNCED
    }

    public enum PerformanceStatus {
        CREATED,
        SUBMITTED,
        APPROVED,
        REJECTED,
        FINAL_SUBMITTED,
        ACCEPTED
    }
}
