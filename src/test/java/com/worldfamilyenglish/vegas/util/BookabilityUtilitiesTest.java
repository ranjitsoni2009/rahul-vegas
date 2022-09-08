package com.worldfamilyenglish.vegas.util;

import com.worldfamilyenglish.vegas.domain.*;
import com.worldfamilyenglish.vegas.types.BookablePeriod;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class BookabilityUtilitiesTest {

    @Test
    public void GIVEN_multiple_free_seats_on_the_same_slot_EXPECT_free_seats_total_counts_them_all() {

        List<Seat> seats = List.of(
                createFreeSeat("2022-08-01T14:00Z"),
                createFreeSeat("2022-08-01T14:00Z"),
                createFreeSeat("2022-08-01T14:00Z"),
                createFreeSeat("2022-08-01T14:00Z")
        );

        List<BookablePeriod> bookablePeriods = BookabilityUtilities.buildBookablePeriods(seats, new ArrayList<>());

        assertThat(bookablePeriods).hasSize(1);

        BookablePeriod bookablePeriod = bookablePeriods.get(0);

        assertBookablePeriodCounts(bookablePeriod, "2022-08-01T14:00Z", 4, 0, 0);
    }

    @Test
    public void GIVEN_booked_and_free_seats_on_the_same_slot_EXPECT_totals_are_collapsed_into_the_same_object() {
        List<Seat> seats = List.of(
                createFreeSeat("2022-08-01T14:00Z"),
                createFreeSeat("2022-08-01T14:00Z"),
                createBookedSeat("2022-08-01T14:00Z")
        );

        List<BookablePeriod> bookablePeriods = BookabilityUtilities.buildBookablePeriods(seats, new ArrayList<>());

        assertThat(bookablePeriods).hasSize(1);

        BookablePeriod bookablePeriod = bookablePeriods.get(0);

        assertBookablePeriodCounts(bookablePeriod, "2022-08-01T14:00Z", 2, 1, 0);
    }

    @Test
    public void GIVEN_a_variety_of_free_and_booked_seats_across_the_same_day_EXPECT_periods_are_added_together_if_they_have_the_same_time() {
        List<Seat> seats = List.of(
                createFreeSeat("2022-08-01T14:00Z"),
                createFreeSeat("2022-08-01T14:00Z"),
                createBookedSeat("2022-08-01T14:00Z")
        );

        List<Period> periods = List.of(
                createPeriod("2022-08-01T14:00Z"),
                createPeriod("2022-08-01T14:00Z")
        );

        List<BookablePeriod> bookablePeriods = BookabilityUtilities.buildBookablePeriods(seats, periods);

        assertThat(bookablePeriods).hasSize(1);

        BookablePeriod bookablePeriod = bookablePeriods.get(0);

        assertBookablePeriodCounts(bookablePeriod, "2022-08-01T14:00Z", 2, 1, 2);
    }


    @Test
    public void GIVEN_seats_over_different_days_EXPECT_there_are_multiple_bookable_periods() {
        List<Seat> seats = List.of(
                createFreeSeat("2022-08-01T14:00Z"),
                createBookedSeat("2022-08-01T14:00Z"),

                createFreeSeat("2022-08-01T14:30Z"),
                createFreeSeat("2022-08-01T14:30Z"),
                createBookedSeat("2022-08-01T14:30Z"),

                createFreeSeat("2022-08-02T10:00Z")
        );

        List<BookablePeriod> bookablePeriods = BookabilityUtilities.buildBookablePeriods(seats, new ArrayList<>());

        assertThat(bookablePeriods).hasSize(3);

        assertBookablePeriodCounts(bookablePeriods.get(0), "2022-08-01T14:00Z", 1, 1, 0);
        assertBookablePeriodCounts(bookablePeriods.get(1), "2022-08-01T14:30Z", 2, 1, 0);
        assertBookablePeriodCounts(bookablePeriods.get(2), "2022-08-02T10:00Z", 1, 0, 0);
    }


    @Test
    public void GIVEN_one_booked_seat_EXPECT_one_bookable_period() {

        Seat testSeat = createBookedSeat("2022-08-15T14:30Z");

        List<BookablePeriod> bookablePeriods = BookabilityUtilities.buildBookablePeriods(List.of(testSeat), new ArrayList<>());

        assertThat(bookablePeriods).hasSize(1);
        BookablePeriod actual = bookablePeriods.get(0);

        assertBookablePeriodCounts(actual, "2022-08-15T14:30Z", 0, 1, 0);
    }

    @Test
    public void GIVEN_no_data_EXPECT_empty_list_is_returned() {

        List<BookablePeriod> bookablePeriods = BookabilityUtilities.buildBookablePeriods(new ArrayList<Seat>(), new ArrayList<Period>());

        assertThat(bookablePeriods).isNotNull();
        assertThat(bookablePeriods).isEmpty();
    }

    private Seat createBookedSeat(final String dateTime) {
        Booking testBooking = Booking
                .builder()
                .status(BookingStatus.PENDING)
                .build();

        Seat testSeat = Seat.builder()
                .booking(testBooking)
                .startTime(OffsetDateTime.parse(dateTime))
                .build();

        return testSeat;
    }

    private Seat createFreeSeat(final String dateTime) {

        Seat testSeat = Seat.builder()
                .startTime(OffsetDateTime.parse(dateTime))
                .build();

        return testSeat;
    }

    private Period createPeriod(final String startTime) {
        return Period
                .builder()
                .from(OffsetDateTime.parse(startTime))
                .isBookable(true)
                .build();
    }

    private void assertBookablePeriodCounts(final BookablePeriod bookablePeriod, String dateTime, int freeSeatCount, int bookedSeatsCount, int freePeriodsCount) {
        assertThat(bookablePeriod.getFrom()).isEqualTo(dateTime);
        assertThat(bookablePeriod.getFreeSeatsCount()).isEqualTo(freeSeatCount);
        assertThat(bookablePeriod.getBookedSeatsCount()).isEqualTo(bookedSeatsCount);
        assertThat(bookablePeriod.getFreePeriodsCount()).isEqualTo(freePeriodsCount);
    }
}