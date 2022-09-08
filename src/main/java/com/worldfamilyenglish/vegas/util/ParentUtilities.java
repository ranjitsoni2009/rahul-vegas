package com.worldfamilyenglish.vegas.util;

import com.worldfamilyenglish.vegas.domain.Booking;
import com.worldfamilyenglish.vegas.domain.Child;
import com.worldfamilyenglish.vegas.domain.Parent;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ParentUtilities {

    private ParentUtilities() {
        // utility class not for instantiation
    }

    public static List<Booking> getBookingsFromChildren(final Parent parent) {
        if (Objects.isNull(parent) || CollectionUtils.isEmpty(parent.getChildren())) {
            return Collections.emptyList();
        }

        final List<Child> children = parent.getChildren();

        final List<Booking> bookings = children
                .stream()
                .filter(child -> !CollectionUtils.isEmpty(child.getBookings()))
                .flatMap(child -> child.getBookings().stream())
                .collect(Collectors.toList());

        return bookings;
    }
}