package com.worldfamilyenglish.vegas.persistence;

import com.worldfamilyenglish.vegas.domain.Booking;
import com.worldfamilyenglish.vegas.domain.BookingStatus;
import liquibase.repackaged.net.sf.jsqlparser.statement.select.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class IBookingRepositoryTest {

    @Autowired
    private IBookingRepository _testRepository;
    private List<Booking> _testBookings;

    @BeforeEach
    void setUp() {
        _testBookings =
                List.of(createTestBooking("2022-04-22T14:00Z", BookingStatus.PENDING),
                        createTestBooking("2022-05-30T12:00Z", BookingStatus.PENDING),
                        createTestBooking("2022-06-01T17:30Z", BookingStatus.PENDING));

        _testBookings = _testRepository.saveAll(_testBookings);
    }

    @Test
    public void GIVEN_pending_bookings_BUT_none_have_timed_out_EXPECT_none_are_returned() {

        OffsetDateTime queryDate = OffsetDateTime.parse("2022-04-20T12:00Z");
        List<Booking> foundBookings = _testRepository.findByTimeoutBeforeAndStatus(queryDate, BookingStatus.PENDING);

        assertThat(foundBookings).isEmpty();

    }

    @Test
    public void GIVEN_pending_bookings_and_some_have_timed_out_EXPECT_timed_out_are_returned() {

        OffsetDateTime queryDate = OffsetDateTime.parse("2022-05-31T12:00Z");
        List<Booking> foundBookings = _testRepository.findByTimeoutBeforeAndStatus(queryDate, BookingStatus.PENDING);

        assertThat(foundBookings).hasSize(2);
        assertThat(foundBookings).extracting("timeout").contains(OffsetDateTime.parse("2022-04-22T14:00Z"), OffsetDateTime.parse("2022-05-30T12:00Z"));
    }

    @Test
    public void GIVEN_confirmed_bookings_have_timed_out_data_EXPECT_confirmed_bookings_are_not_returned() {
         _testRepository.saveAll(List.of(createTestBooking("2022-04-10T14:00Z", BookingStatus.BOOKED),
                        createTestBooking("2022-05-20T12:00Z", BookingStatus.BOOKED)));


        OffsetDateTime queryDate = OffsetDateTime.parse("2022-08-31T12:00Z");
        List<Booking> foundBookings = _testRepository.findByTimeoutBeforeAndStatus(queryDate, BookingStatus.PENDING);

        assertThat(foundBookings)
                .extracting("timeout")
                .containsOnly(OffsetDateTime.parse("2022-04-22T14:00Z"), OffsetDateTime.parse("2022-05-30T12:00Z"), OffsetDateTime.parse("2022-06-01T17:30Z"));
    }

    private Booking createTestBooking(final String dateTime, final BookingStatus status) {
        return Booking
                .builder()
                .status(status)
                .timeout(OffsetDateTime.parse(dateTime))
                .build();
    }
}