package com.worldfamilyenglish.vegas.persistence;

import com.worldfamilyenglish.vegas.domain.Booking;
import com.worldfamilyenglish.vegas.domain.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface IBookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Find all bookings that timeouts have expired and in the given state.
     * @param now when to search for timeouts before - should be now()
     * @return any found bookings that match the criteria
     */
    List<Booking> findByTimeoutBeforeAndStatus(final OffsetDateTime now, final BookingStatus status);
}
