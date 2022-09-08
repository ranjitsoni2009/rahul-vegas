package com.worldfamilyenglish.vegas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import com.worldfamilyenglish.vegas.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.worldfamilyenglish.vegas.service.zoom.ZoomAuthService;
import com.worldfamilyenglish.vegas.service.zoom.domain.ZoomCreateMeetingResponse;
import com.worldfamilyenglish.vegas.types.CreateChildInput;
import com.worldfamilyenglish.vegas.types.CreateParentInput;
import com.worldfamilyenglish.vegas.zoomclient.IZoomClient;


@SpringBootTest
@ActiveProfiles("integrationtest")
class BookingServiceFacadeIntegrationTest {

    @Autowired
    private BookingServiceFacade _bookingServiceFacade;

    @Autowired
    private ContentService _contentService;

    @Autowired
    private ParentService _parentService;

    @Autowired
    private AvailabilityService _availabilityService;

    @Autowired
    private IDomainUserService _domainUserService;

    @MockBean
    private IZoomClient _mockZoomClient;

    @MockBean
    private ZoomAuthService _mockZoomAuthService;

    private ContentInfo _testContent;
    private DomainUser _theTeacher;
    private Parent _parent;

    @Test
    @Transactional
    public void GIVEN_no_lessons_no_plans_EXPECT_booking_is_unfullfillable() {
        givenThereIsAParentAndChild();

        givenThereIsContent();

        Booking booking = _bookingServiceFacade.createBooking("Child_001", _testContent.getId(), OffsetDateTime.parse("2022-04-01T14:00Z"), 30, _parent.getChildren());

        assertThat(booking).isNotNull();
        assertThat(booking.getChild().getExternalId()).isEqualTo("Child_001");
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.UNFULFILLABLE);
    }

    @Test
    @Transactional
    public void GIVEN_a_period_is_available_EXPECT_a_pending_booking_is_made() {
        givenThereIsAParentAndChild();

    	givenThereIsContent();
        givenTheContentIsPublished();

        givenThereIsATeacher();
        givenTheTeacherHasAvailability();
        givenTheTeacherIsAccreditedToPerformTheContent();

        givenZoomMeetingsGetCreated();

        Booking booking = _bookingServiceFacade.createBooking("Child_001", _testContent.getId(), OffsetDateTime.parse("2022-04-01T14:00Z"), 30, _parent.getChildren());

        assertThat(booking).isNotNull();
        assertThat(booking.getChild().getExternalId()).isEqualTo("Child_001");
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);

        assertThat(booking.getSeat()).isNotNull();
        assertThat(booking.getSeat().getLesson().getPeriod().getOwner()).isEqualTo(_theTeacher);
    }

    @Test
    @Transactional
    public void GIVEN_a_period_is_available_BUT_the_teacher_is_not_accredited_to_teach_it_EXPECT_a_booking_is_unfullfillable() {
        givenThereIsAParentAndChild();
        givenThereIsContent();
        givenTheContentIsPublished();

        givenThereIsATeacher();
        givenTheTeacherHasAvailability();

        // NO CONTENT ACCREDITATION IS CREATED HERE, SO NO TEACHER SHOULD BE AVAILABLE

        Booking booking = _bookingServiceFacade.createBooking("Child_001", _testContent.getId(), OffsetDateTime.parse("2022-04-01T14:00Z"), 30, _parent.getChildren());

        assertThat(booking).isNotNull();
        assertThat(booking.getChild().getExternalId()).isEqualTo("Child_001");
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.UNFULFILLABLE);

    }

    @Test
    @Transactional
    public void GIVEN_a_period_is_available_BUT_the_teacher_is_not_registered_with_zoom_EXPECT_a_booking_is_unfullfillable() {
        givenThereIsAParentAndChild();
        givenThereIsContent();
        givenTheContentIsPublished();

        givenThereIsATeacher();
        givenTheTeacherIsAccreditedToPerformTheContent();
        givenTheTeacherHasAvailability();


        when(_mockZoomAuthService.buildAccessToken()).thenReturn("testAccessToken");
        when(_mockZoomClient.createMeeting(anyString(), any(), anyString())).thenThrow(IllegalStateException.class);

        Booking booking = _bookingServiceFacade.createBooking("Child_001", _testContent.getId(), OffsetDateTime.parse("2022-04-01T14:00Z"), 30, _parent.getChildren());
        
        assertThat(booking).isNotNull();
        assertThat(booking.getChild().getExternalId()).isEqualTo("Child_001");
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.UNFULFILLABLE);
    }

    @Test
    @Transactional
    public void GIVEN_the_last_seat_in_a_lesson_is_cancelled_EXPECT_the_lesson_is_also_deleted() {
        givenThereIsAParentAndChild();
        givenThereIsContent();
        givenTheContentIsPublished();

        givenThereIsATeacher();
        givenTheTeacherIsAccreditedToPerformTheContent();

        givenTheTeacherHasAvailability();

        Booking booking = _bookingServiceFacade.createBooking("Child_001", _testContent.getId(), OffsetDateTime.parse("2022-04-01T14:00Z"), 30, _parent.getChildren());

        assertThat(booking).isNotNull();
        assertThat(booking.getChild().getExternalId()).isEqualTo("Child_001");
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);

        assertThat(booking.getSeat()).isNotNull();

        OffsetDateTime sometimeWayInTheFuture = OffsetDateTime.parse("2022-10-20T00:00Z");
        _bookingServiceFacade.timeoutPendingBookings(sometimeWayInTheFuture);

        Optional<Parent> foundParent = _parentService.getParent("Parent_001");

        List<Booking> bookings = foundParent.get().getChildren().get(0).getBookings();

        assertThat(bookings).hasSize(1);

        Booking timedBooking = bookings.get(0);
        assertThat(timedBooking).isEqualTo(booking);
        assertThat(timedBooking.getStatus()).isEqualTo(BookingStatus.TIMED_OUT);
        assertThat(timedBooking.getSeat()).isNull();

        List<Availability> availabilities = _availabilityService.getRange(OffsetDateTime.parse("2022-04-01T12:00Z"), OffsetDateTime.parse("2022-04-02T12:00Z"));

        assertNoAvailabilityHasLessons(availabilities);
    }

    private void assertNoAvailabilityHasLessons(final List<Availability> availabilities) {
        for (Availability availability : availabilities) {
            List<Period> periods = availability.getPeriods();
            for (Period period : periods) {
                Lesson lesson = period.getLesson();
                if (lesson != null) {
                    fail("Found a lesson against period");
                }
            }
        }
    }

    @Test
    @Transactional
    public void GIVEN_given_a_lesson_is_deleted_EXPECT_the_period_can_be_reused_for_a_new_booking() {
        givenThereIsAParentAndChild();
        givenThereIsContent();
        givenTheContentIsPublished();

        givenThereIsATeacher();
        givenTheTeacherIsAccreditedToPerformTheContent();

        givenTheTeacherHasAvailability();

        Booking booking = _bookingServiceFacade.createBooking("Child_001", _testContent.getId(), OffsetDateTime.parse("2022-04-01T14:00Z"), 30, _parent.getChildren());

        assertThat(booking).isNotNull();
        assertThat(booking.getChild().getExternalId()).isEqualTo("Child_001");
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);

        assertThat(booking.getSeat()).isNotNull();

        OffsetDateTime sometimeWayInTheFuture = OffsetDateTime.parse("2022-10-20T00:00Z");
        _bookingServiceFacade.timeoutPendingBookings(sometimeWayInTheFuture);

        // now try to make a booking again.

        Booking secondBooking = _bookingServiceFacade.createBooking("Child_001", _testContent.getId(), OffsetDateTime.parse("2022-04-01T14:00Z"), 30, _parent.getChildren());

        assertThat(secondBooking).isNotNull();
        assertThat(secondBooking.getChild().getExternalId()).isEqualTo("Child_001");
        assertThat(secondBooking.getStatus()).isEqualTo(BookingStatus.PENDING);

        assertThat(secondBooking.getSeat()).isNotNull();
        assertThat(secondBooking.getSeat().getLesson().getPeriod().getOwner()).isEqualTo(_theTeacher);
    }


    private void givenTheTeacherIsAccreditedToPerformTheContent() {
        _contentService.createContentPerformingAccreditation(_theTeacher.getId(), _testContent.getId());
    }

    private void givenTheTeacherHasAvailability() {
        Availability pendingAvailability = Availability.builder().from(OffsetDateTime.parse("2022-04-01T12:00Z")).to(OffsetDateTime.parse("2022-04-01T17:30Z")).isBookable(true).build();
        _availabilityService.create(pendingAvailability, _theTeacher.getId());
    }

    private void givenThereIsATeacher() {
        _theTeacher = _domainUserService.createUser(DomainUser.builder().email("teacher@wfe.com").build());
    }

    private void givenThereIsAParentAndChild() {
    	_parent = _parentService.createParent(getTestParentAndChildren());
    }

    private CreateParentInput getTestParentAndChildren() {
        return getTestParentAndChildren("Child_001");
    }

    private CreateParentInput getTestParentAndChildren(final String childExternalId) {
        CreateChildInput childInput = CreateChildInput
                .newBuilder()
                .uniqueExternalId(childExternalId)
                .displayName("001")
                .capLevel(CapLevel.GREEN)
                .dateOfBirth(LocalDate.of(2010, 1, 1)).build();

        return CreateParentInput.newBuilder().uniqueExternalId("Parent_001").displayName("Parent_001").children(List.of(childInput)).build();
    }

    private void givenTheContentIsPublished() {
        _contentService.createPublishingPlan(_testContent.getId(), OffsetDateTime.parse("2022-04-01T00:00Z"), OffsetDateTime.parse("2022-04-30T23:59Z"), List.of());
    }

    private void givenThereIsContent() {
        _testContent = _contentService.createContentInfo("Test Green", "", CapLevel.GREEN, "https://example.com/content-green");
    }

    private void givenZoomMeetingsGetCreated() {
        ZoomCreateMeetingResponse zoomMeetingResponse = ZoomCreateMeetingResponse.builder().joinUrl("joinUrl").build();
        when(_mockZoomClient.createMeeting(anyString(), any(), anyString())).thenReturn(zoomMeetingResponse);
        when(_mockZoomAuthService.buildAccessToken()).thenReturn("testAccessToken");
    }
}