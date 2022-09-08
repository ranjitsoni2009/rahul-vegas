package com.worldfamilyenglish.vegas.service;

import com.worldfamilyenglish.vegas.domain.*;
import com.worldfamilyenglish.vegas.persistence.IBookingRepository;
import com.worldfamilyenglish.vegas.persistence.IChildRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class BookingService {

    private final IBookingRepository _bookingRepository;
    private final PeriodService _periodService;

    private final LessonService _lessonService;

    private static final int DEFAULT_TIMEOUT_IN_MINUTES = 6;

    @Autowired
    public BookingService(final IBookingRepository bookingRepository, final PeriodService periodService, final LessonService lessonService) {
        _bookingRepository = bookingRepository;
        _periodService = periodService;
        _lessonService = lessonService;
    }

    @Nullable
    @Transactional(propagation = Propagation.SUPPORTS, isolation = Isolation.READ_UNCOMMITTED)
    public Booking createBooking(final OffsetDateTime lessonTime, final ContentInfo foundContentInfo, final Child child, final Integer requestedTimeoutMinutes) {
        LOG.debug("....Doing actual booking...");

        Booking booking;
        List<Seat> freeSeats = _lessonService.findFreeSeats(foundContentInfo, lessonTime);
        boolean existingSeatAvailableToBook = freeSeats.size() > 0;

        LOG.debug("...Found {} free seats", freeSeats.size());

        final int timeoutMinutes = requestedTimeoutMinutes == null ? DEFAULT_TIMEOUT_IN_MINUTES : requestedTimeoutMinutes;
        final BookingStatus initialStatus = timeoutMinutes <= 0 ? BookingStatus.BOOKED : BookingStatus.PENDING;
        final OffsetDateTime bookingTimeoutTime = OffsetDateTime.now().plusMinutes(timeoutMinutes);


        if (existingSeatAvailableToBook) {
            LOG.info("Booking existing seat for child {}", child.getExternalId());
            booking = bookExistingSeat(child, freeSeats, initialStatus, bookingTimeoutTime);
        } else {
            booking = bookPeriod(lessonTime, foundContentInfo, child, initialStatus, bookingTimeoutTime);
        }

        return booking;
    }

    @Nullable
    private Booking bookPeriod(final OffsetDateTime lessonTime, final ContentInfo contentInfo, final Child child, final BookingStatus initialStatus, final OffsetDateTime bookingTimeoutTime) {
        Booking booking;

        List<Period> allBookableAndFreePeriods = _periodService.findBookablePeriodsForContent(lessonTime, contentInfo);

        if (allBookableAndFreePeriods.isEmpty()) {
            LOG.info("No free periods to turn into a lesson for {}", child.getExternalId());
            booking = createUnfulfillableBooking(child);
        } else {
            LOG.debug("Found {} free, bookable periods", allBookableAndFreePeriods.size());
            LOG.info("Creating new lesson and seat for child {}", child.getExternalId());
            booking = bookRandomPeriod(allBookableAndFreePeriods, child, contentInfo, initialStatus, bookingTimeoutTime);
        }

        return booking;
    }

    private Booking bookRandomPeriod(final List<Period> allBookableFreePeriods, final Child child, final ContentInfo contentInfo, final BookingStatus initialStatus, final OffsetDateTime bookingTimeoutTime) {

    	Seat seat = null;
        BookingStatus bookingStatus = initialStatus;

        int randomIndex = (int) (Math.random() * allBookableFreePeriods.size());
        Period chosenPeriod = allBookableFreePeriods.get(randomIndex);

    	try {
    		seat  = _lessonService.createLessonAndSeat(contentInfo, chosenPeriod);

    		Lesson lesson = seat.getLesson();
    		chosenPeriod.setLesson(lesson);
    		lesson.setPeriod(chosenPeriod);

    	} catch (Exception exception) {
    		LOG.error("Exception while booking the periods : {}", exception.getMessage());
    		bookingStatus = BookingStatus.UNFULFILLABLE;
    	}

        Booking newBooking = Booking
                .builder()
                .status(bookingStatus)
                .timeout(bookingTimeoutTime)
                .child(child)
                .seat(seat)
                .build();

    	if (Objects.nonNull(seat)) {
    		seat.setBooking(newBooking);
    	}

        return _bookingRepository.save(newBooking);
    }

    public Booking createUnfulfillableBooking(final Child child) {
        Booking unfulfillableBooking = Booking
                .builder()
                .status(BookingStatus.UNFULFILLABLE)
                .child(child)
                .build();

        return _bookingRepository.save(unfulfillableBooking);
    }

    private Booking bookExistingSeat(final Child child, final List<Seat> freeSeats, final BookingStatus initialStatus, final OffsetDateTime bookingTimeoutTime) {
        Seat randomSeat = freeSeats.get(0);

        Booking newBooking = Booking
                .builder()
                .status(initialStatus)
                .seat(randomSeat)
                .child(child)
                .timeout(bookingTimeoutTime)
                .build();

        randomSeat.setBooking(newBooking);

        return _bookingRepository.save(newBooking);
    }

    public void timeoutPendingBookings(final OffsetDateTime cutoffTime) {
        List<Booking> timedOutBookings = _bookingRepository.findByTimeoutBeforeAndStatus(cutoffTime, BookingStatus.PENDING);

        if (timedOutBookings.isEmpty()) {
            return;
        }

        LOG.info("Found {} bookings to timeout...", timedOutBookings.size());

        for (Booking timedOutBooking : timedOutBookings) {
            Seat seat = timedOutBooking.getSeat();
            seat.clearBooking();

            timedOutBooking.setStatus(BookingStatus.TIMED_OUT);

            _bookingRepository.save(timedOutBooking);

            LOG.info("Timing out and cancelling booking {} ", timedOutBooking);
            _lessonService.cancelSeatBooking(seat);
        }
    }
}
