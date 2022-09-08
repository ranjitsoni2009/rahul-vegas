package com.worldfamilyenglish.vegas.service;

import com.worldfamilyenglish.vegas.domain.Booking;
import com.worldfamilyenglish.vegas.domain.Lesson;
import com.worldfamilyenglish.vegas.domain.Period;
import com.worldfamilyenglish.vegas.domain.Seat;
import com.worldfamilyenglish.vegas.persistence.ILessonRepository;
import com.worldfamilyenglish.vegas.persistence.IPeriodRepository;
import com.worldfamilyenglish.vegas.persistence.ISeatRepository;
import com.worldfamilyenglish.vegas.service.zoom.ZoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class LessonServiceTest {

    private ILessonRepository _mockLessonRepository;
    private ISeatRepository _mockSeatRepository;
    private LessonService _testLessonService;
    private IPeriodRepository _mockPeriodRepository;
    private ZoomService _mockZoomService;

    @BeforeEach
    void setUp() {

        _mockLessonRepository = mock(ILessonRepository.class);
        _mockSeatRepository = mock(ISeatRepository.class);
        _mockPeriodRepository = mock(IPeriodRepository.class);
        _mockZoomService = mock(ZoomService.class);

        _testLessonService = new LessonService(_mockLessonRepository, _mockSeatRepository, _mockPeriodRepository, _mockZoomService);
    }

    @Test
    public void GIVEN_a_seat_is_the_last_of_any_lesson_AND_the_seats_booking_is_cancelled_EXPECT_the_lesson_is_also_destroyed() {
        Seat testSeat = createTestSeatAndLesson();
        Lesson testLesson = testSeat.getLesson();

        when(_mockLessonRepository.findById(any())).thenReturn(Optional.of(testLesson));

        _testLessonService.cancelSeatBooking(testSeat);

        verify(_mockLessonRepository).deleteById(eq(testLesson.getId()));
    }

    @Test
    public void GIVEN_a_lesson_still_has_bookings_when_one_of_its_seats_bookings_is_cancelled_EXPECT_the_lesson_is_not_destroyed() {
        Lesson testLessonWithSeats = createTestLessonWithSeats(2);
        when(_mockLessonRepository.findById(any())).thenReturn(Optional.of(testLessonWithSeats));

        Booking booking = Booking.builder().build();
        testLessonWithSeats.getSeats().get(0).setBooking(booking);

        _testLessonService.cancelSeatBooking(testLessonWithSeats.getSeats().get(1));

        verify(_mockLessonRepository, never()).delete(any());
        verify(_mockSeatRepository, never()).deleteAll(any());
    }

    private Seat createTestSeatAndLesson() {

        Seat seat = Seat.builder().id(1L).build();

        Lesson lesson = Lesson.builder().id(1L).build();
        Period period = Period.builder().id(1L).build();

        lesson.addSeat(seat);

        lesson.addSeat(seat);
        period.setLesson(lesson);
        lesson.setPeriod(period);

        return seat;
    }

    private Lesson createTestLessonWithSeats(int seatCount) {

        Lesson lesson = Lesson.builder().id(1L).build();
        for (int i = 0; i < seatCount; i++) {
            Seat seat = Seat.builder().id((long) i).build();
            lesson.addSeat(seat);
        }

        Period period = Period.builder().id(1L).build();

        period.setLesson(lesson);
        lesson.setPeriod(period);

        return lesson;
    }
}