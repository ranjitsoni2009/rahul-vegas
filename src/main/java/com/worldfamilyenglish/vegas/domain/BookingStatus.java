package com.worldfamilyenglish.vegas.domain;

public enum BookingStatus {

    /**
     * There are no seats or spare teachers available for the booking.
     */
    UNFULFILLABLE,

    /**
     * The booking has been accepted, a seat is matched, it just needs to be confirmed and paid for.
     */
    PENDING,

    /**
     * The booking has been confirmed and paid for.
     */
    BOOKED,

    /**
     * The booking was not confirmed in time.
     */
    TIMED_OUT,

    /**
     * The booking was cancelled by the parent or the system.
     */
    CANCELLED
}
