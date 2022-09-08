package com.worldfamilyenglish.vegas.domain;

public enum PeriodType {
    /**
     * This period is not bookable, not for teaching, a paid period for the teacher to sign in and warm up.
     */
    CHECK_IN,
    /**
     * This period is for teaching, potentially bookable by consumers.
     */
    TEACHING
}
