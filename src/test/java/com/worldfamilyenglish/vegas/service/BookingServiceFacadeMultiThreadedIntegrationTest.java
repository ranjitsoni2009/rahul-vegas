package com.worldfamilyenglish.vegas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.worldfamilyenglish.vegas.domain.Availability;
import com.worldfamilyenglish.vegas.domain.Booking;
import com.worldfamilyenglish.vegas.domain.BookingStatus;
import com.worldfamilyenglish.vegas.domain.CapLevel;
import com.worldfamilyenglish.vegas.domain.Child;
import com.worldfamilyenglish.vegas.domain.ContentInfo;
import com.worldfamilyenglish.vegas.domain.DomainUser;
import com.worldfamilyenglish.vegas.domain.Parent;
import com.worldfamilyenglish.vegas.persistence.IBookingRepository;
import com.worldfamilyenglish.vegas.service.zoom.ZoomAuthService;
import com.worldfamilyenglish.vegas.service.zoom.domain.ZoomCreateMeetingResponse;
import com.worldfamilyenglish.vegas.types.CreateChildInput;
import com.worldfamilyenglish.vegas.types.CreateParentInput;
import com.worldfamilyenglish.vegas.zoomclient.IZoomClient;

@SpringBootTest
@ActiveProfiles("integrationtest")
class BookingServiceFacadeMultiThreadedIntegrationTest {

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

    @Autowired
    private IBookingRepository _bookingRepository;
    
    @MockBean
    private IZoomClient _mockZoomClient;
    
    @MockBean
    private ZoomAuthService _mockZoomAuthService;

    /**
     * This is supposed to be a multi-threaded test, but only uses on thread.
     * H2 Database doesn't commit the transaction to allow other threads to view persisted data.
     *
     * TODO: Use a different database for integration tests and make multi-threaded again.
     */
    @Test
    public void GIVEN_100_users_booking_lessons_and_60_possible_seats_EXPECT_all_seats_are_booked_and_40_bookings_are_unfullfillable() throws InterruptedException {
        final int totalParentsAndChildren = 100;
        final int totalTeachers = 20;

        ConcurrentLinkedQueue<Parent> oneHundredParents = new ConcurrentLinkedQueue();
        List<Parent> processedParents = Collections.synchronizedList(new ArrayList<>());

        List<DomainUser> twentyTeachers = new ArrayList<>();

        for (int i = 0; i < totalParentsAndChildren; i++) {
            String parentId = "Parent_%03d".formatted(i + 1);
            String childId = "Child_%03d".formatted(i + 1);

            CreateChildInput childInput = CreateChildInput.
                    newBuilder()
                    .displayName(childId)
                    .uniqueExternalId(childId)
                    .dateOfBirth(LocalDate.parse("2010-05-01"))
                    .capLevel(CapLevel.GREEN)
                    .build();

            CreateParentInput parentInput = CreateParentInput
                    .newBuilder()
                    .uniqueExternalId(parentId)
                    .displayName(parentId)
                    .children(List.of(childInput))
                    .build();

            oneHundredParents.add(_parentService.createParent(parentInput));
        }

        assertThat(oneHundredParents).hasSize(totalParentsAndChildren);


        ContentInfo greenContent = _contentService.createContentInfo("The content everyone loves", "The description", CapLevel.GREEN, "");
        _contentService.createPublishingPlan(greenContent.getId(), OffsetDateTime.parse("2022-04-01T00:00Z"), OffsetDateTime.parse("2022-04-30T23:59Z"), List.of());

        /*
        Create the teachers, their content accreditation and their availability.
         */

        for (int i = 0; i < totalTeachers; i++) {
            String teacherEmail = "teacher%01d@wfe.com".formatted(i);
            DomainUser theTeacher = _domainUserService.createUser(DomainUser.builder().email(teacherEmail).build());
            _contentService.createContentPerformingAccreditation(theTeacher.getId(), greenContent.getId());

            Availability pendingAvailability = Availability.builder().from(OffsetDateTime.parse("2022-04-01T12:00Z")).to(OffsetDateTime.parse("2022-04-01T17:30Z")).isBookable(true).build();
            Availability availability = _availabilityService.create(pendingAvailability, theTeacher.getId());

            twentyTeachers.add(theTeacher);
        }
        
        ZoomCreateMeetingResponse zoomMeetingResponse = ZoomCreateMeetingResponse.builder().joinUrl("joinUrl").build();
        
        when(_mockZoomClient.createMeeting(Mockito.anyString(), Mockito.any(), Mockito.anyString())).thenReturn(zoomMeetingResponse);

        when(_mockZoomAuthService.buildAccessToken()).thenReturn("testAccessToken");	

        int THREAD_COUNT = 1;
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            executorService.submit(() -> {

                    while(!oneHundredParents.isEmpty()) {

                        Parent parentWantingToBook = oneHundredParents.poll();

                        Child child = parentWantingToBook.getChildren().get(0);

                        Booking booking = null;
                        try {
                            booking = _bookingServiceFacade.createBooking(child.getExternalId(), greenContent.getId(), OffsetDateTime.parse("2022-04-01T13:00Z"), 30, parentWantingToBook.getChildren());
                        } catch (Exception e) {
                            e.printStackTrace();
                            fail(e);
                        }

                        assertThat(booking).isNotNull();
                        assertThat(booking.getChild()).isEqualTo(child);

                        processedParents.add(parentWantingToBook);
                    }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        assertThat(oneHundredParents).isEmpty();
        assertThat(processedParents).hasSize(totalParentsAndChildren);


        List<Booking> allBookings = _bookingRepository.findAll();
        assertThat(allBookings).hasSize(totalParentsAndChildren);

        assertThat(countInstancesOf(allBookings, BookingStatus.UNFULFILLABLE)).isEqualTo(40);
        assertThat(countInstancesOf(allBookings, BookingStatus.PENDING)).isEqualTo(60);
    }

    private int countInstancesOf(final List<Booking> allBookings, final BookingStatus bookingStatus) {
        return (int) allBookings
                .stream()
                .filter(b -> b.getStatus() == bookingStatus)
                .count();
    }
}