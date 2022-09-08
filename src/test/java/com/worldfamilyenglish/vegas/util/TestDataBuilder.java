package com.worldfamilyenglish.vegas.util;

import com.worldfamilyenglish.vegas.domain.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class TestDataBuilder {

    public static Child getChild() {
        return Child.builder()
                .id(1L)
                .displayName("Baby John")
                .externalId("C7895")
                .capLevel(CapLevel.BLUE)
                .updated(OffsetDateTime.of(2019, 5, 2, 11, 17, 33, 0, ZoneOffset.UTC))
                .created(OffsetDateTime.of(2019, 1, 1, 12, 45, 0, 0, ZoneOffset.UTC))
                .build();
    }

    public static Parent getParent() {
        return Parent.builder()
                .id(1L)
                .displayName("Baby John")
                .externalId("P1478")
                .updated(OffsetDateTime.of(2010, 5, 2, 11, 17, 33, 0, ZoneOffset.UTC))
                .created(OffsetDateTime.of(2010, 1, 1, 12, 45, 0, 0, ZoneOffset.UTC))
                .build();
    }

    public static Booking getBooking() {
        return Booking.builder()
                .id(1L)
                .status(BookingStatus.BOOKED)
                .updated(OffsetDateTime.of(2022, 5, 2, 11, 17, 33, 0, ZoneOffset.UTC))
                .created(OffsetDateTime.of(2022, 1, 1, 12, 45, 0, 0, ZoneOffset.UTC))
                .build();
    }

    public static Parent getTestParentObjectWithChildButNoAssociatedBooking() {
        final Parent parent = getParent();

        final Child child = getChild();
        child.setParent(parent);

        parent.setChildren(List.of(child));

        return parent;
    }

    public static Parent getTestParentObjectWithChildAndAssociatedBooking() {
        final Parent parent = getParent();

        final Child child = getChild();

        final Booking booking = getBooking();
        booking.setChild(child);

        child.setBookings(List.of(booking));
        child.setParent(parent);

        parent.setChildren(List.of(child));

        return parent;
    }
}
