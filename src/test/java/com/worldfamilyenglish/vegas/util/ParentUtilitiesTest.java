package com.worldfamilyenglish.vegas.util;

import com.worldfamilyenglish.vegas.domain.Booking;
import com.worldfamilyenglish.vegas.domain.Parent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ParentUtilitiesTest {

    @Test
    void GIVEN_null_parent_object_EXPECT_empty_booking_list_is_returned() {
        final Parent parent = null;

        List<Booking> bookings = ParentUtilities.getBookingsFromChildren(parent);

        assertThat(bookings).isEmpty();
    }

    @Test
    void GIVEN_parent_object_with_no_child_EXPECT_empty_booking_list_is_returned() {
        final Parent parent = TestDataBuilder.getParent();
        assertThat(parent.getChildren()).isNull();

        List<Booking> bookings = ParentUtilities.getBookingsFromChildren(parent);

        assertThat(bookings).isEmpty();
    }

    @Test
    void GIVEN_parent_object_with_only_child_EXPECT_empty_booking_list_is_returned() {
        final Parent parent = TestDataBuilder.getTestParentObjectWithChildButNoAssociatedBooking();
        assertThat(parent.getChildren()).isNotEmpty();

        List<Booking> bookings = ParentUtilities.getBookingsFromChildren(parent);

        assertThat(bookings).isEmpty();
    }

    @Test
    void GIVEN_parent_object_with_child_and_booking_EXPECT_valid_booking_list_is_returned() {
        final Parent parent = TestDataBuilder.getTestParentObjectWithChildAndAssociatedBooking();
        assertThat(parent.getChildren()).isNotEmpty();

        List<Booking> bookings = ParentUtilities.getBookingsFromChildren(parent);

        assertThat(bookings).isNotEmpty();
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(1L);
    }
}