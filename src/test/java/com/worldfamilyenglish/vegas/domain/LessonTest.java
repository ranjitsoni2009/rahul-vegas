package com.worldfamilyenglish.vegas.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LessonTest {

    @Test
    public void GIVEN_no_seat_has_bookings_EXPECT_has_bookings_is_false() {

        Lesson lesson = Lesson
                .builder().id(1L).build();

        lesson.addSeat(Seat.builder().id(1L).build());
        lesson.addSeat(Seat.builder().id(2L).build());

        assertThat(lesson.hasAnyBookings()).isFalse();
    }

    @Test
    public void GIVEN_a_seat_has_a_booking_EXPECT_has_bookings_is_true() {
        Lesson lesson = Lesson
                .builder().id(1L).build();

        lesson.addSeat(Seat.builder().id(1L).build());
        lesson.addSeat(Seat.builder().id(2L).build());

        lesson.getSeats().get(0).setBooking(Booking.builder().build());

        assertThat(lesson.hasAnyBookings()).isTrue();
    }

}