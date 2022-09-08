package com.worldfamilyenglish.vegas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.worldfamilyenglish.vegas.domain.Booking;
import com.worldfamilyenglish.vegas.domain.BookingStatus;
import com.worldfamilyenglish.vegas.domain.Child;
import com.worldfamilyenglish.vegas.domain.ContentInfo;
import com.worldfamilyenglish.vegas.domain.DomainUser;
import com.worldfamilyenglish.vegas.domain.Lesson;
import com.worldfamilyenglish.vegas.domain.Period;
import com.worldfamilyenglish.vegas.domain.Seat;
import com.worldfamilyenglish.vegas.persistence.IBookingRepository;

public class BookingServiceTest {
	
	private IBookingRepository _mockBookingRepository;
    private PeriodService _mockPeriodService;
    private LessonService _mockLessonService;
    
    private BookingService _bookingService;


    @BeforeEach
    public void setUp() {
    	_mockBookingRepository = mock(IBookingRepository.class);
    	_mockPeriodService = mock(PeriodService.class);
    	_mockLessonService = mock(LessonService.class);
    	
    	_bookingService = new BookingService(_mockBookingRepository, _mockPeriodService, _mockLessonService);
    }
    
    @Test
    public void GIVEN_valid_lesson_time_and_content_info_and_child_info_EXPECT_booking_is_created_with_pending_status() {

    	OffsetDateTime lessonTime = OffsetDateTime.parse("2022-08-21T07:30:55Z");
    	
    	ContentInfo contentInfo = BookingTestDataBuilder.getContentInfo();
    	Child child = BookingTestDataBuilder.getChildInfo();
    	DomainUser domainUser = BookingTestDataBuilder.getDomainUser();
    	Period period = BookingTestDataBuilder.getPeriod(domainUser);
    	Lesson lesson = BookingTestDataBuilder.getLesson(contentInfo, period);
    	Seat seat = BookingTestDataBuilder.getSeat(contentInfo, lesson);
    	Booking newBooking = BookingTestDataBuilder.getNewBooking(BookingStatus.PENDING, child, seat);
    	
    	List<Period> allBookableAndFreePeriods = Collections.singletonList(period);

    	when(_mockLessonService.findFreeSeats(Mockito.any(ContentInfo.class), Mockito.any(OffsetDateTime.class))).thenReturn(Collections.emptyList());
    	when(_mockPeriodService.findBookablePeriodsForContent(lessonTime, contentInfo)).thenReturn(allBookableAndFreePeriods);
    	when(_mockLessonService.createLessonAndSeat(contentInfo, period)).thenReturn(seat);
    	when(_mockBookingRepository.save(newBooking)).thenReturn(newBooking);
    	
    	Booking booking = _bookingService.createBooking(lessonTime, contentInfo, child, 30);

    	assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);
    }
    
    @Test
    public void GIVEN_valid_lesson_time_and_content_info_and_child_info_but_teacher_is_not_registered_with_zoom_EXPECT_booking_is_created_with_UNFULFILLABLE_status() {

    	OffsetDateTime lessonTime = OffsetDateTime.parse("2022-08-21T07:30:55Z");
    	
    	ContentInfo contentInfo = BookingTestDataBuilder.getContentInfo();
    	Child child = BookingTestDataBuilder.getChildInfo();
    	DomainUser domainUser = BookingTestDataBuilder.getDomainUser();
    	Period period = BookingTestDataBuilder.getPeriod(domainUser);
    	Lesson lesson = BookingTestDataBuilder.getLesson(contentInfo, period);
    	Seat seat = BookingTestDataBuilder.getSeat(contentInfo, lesson);
    	Booking newBooking = BookingTestDataBuilder.getNewBooking(BookingStatus.UNFULFILLABLE, child, seat);
    	
    	List<Period> allBookableAndFreePeriods = Collections.singletonList(period);

    	when(_mockLessonService.findFreeSeats(Mockito.any(ContentInfo.class), Mockito.any(OffsetDateTime.class))).thenReturn(Collections.emptyList());
    	when(_mockPeriodService.findBookablePeriodsForContent(lessonTime, contentInfo)).thenReturn(allBookableAndFreePeriods);
    	when(_mockLessonService.createLessonAndSeat(contentInfo, period)).thenThrow(IllegalStateException.class);
    	when(_mockBookingRepository.save(newBooking)).thenReturn(newBooking);
    	
    	Booking booking = _bookingService.createBooking(lessonTime, contentInfo, child, 30);

    	assertThat(booking.getStatus()).isEqualTo(BookingStatus.UNFULFILLABLE);
    }
    
    
}
