package com.worldfamilyenglish.vegas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;

import com.worldfamilyenglish.vegas.domain.Booking;
import com.worldfamilyenglish.vegas.domain.BookingStatus;
import com.worldfamilyenglish.vegas.domain.CapLevel;
import com.worldfamilyenglish.vegas.domain.Child;
import com.worldfamilyenglish.vegas.domain.ContentInfo;
import com.worldfamilyenglish.vegas.domain.DomainUser;
import com.worldfamilyenglish.vegas.domain.Lesson;
import com.worldfamilyenglish.vegas.domain.Parent;
import com.worldfamilyenglish.vegas.domain.Period;
import com.worldfamilyenglish.vegas.domain.Seat;
import com.worldfamilyenglish.vegas.persistence.IBookingRepository;
import com.worldfamilyenglish.vegas.persistence.IChildRepository;
import com.worldfamilyenglish.vegas.types.CreateChildInput;
import com.worldfamilyenglish.vegas.types.CreateParentInput;

class BookingServiceFacadeTest {


    private IChildRepository _mockChildRepository;
    private IBookingRepository _mockBookingRepository;
    private LessonService _mockLessonService;
    private PeriodService _mockPeriodService;
    private ParentService _mockParentService;
    
    private BookingServiceFacade _testService;

    private ContentService _mockContentService;

    private ContentInfo _testContentInfo;
    private Seat _testSeat;
    private BookingService _realBookingService;


    @BeforeEach
    void setUp() {
        _mockChildRepository = mock(IChildRepository.class);
        _mockBookingRepository = mock(IBookingRepository.class);
        _mockContentService = mock(ContentService.class);
        _mockPeriodService = mock(PeriodService.class);
        _mockLessonService = mock(LessonService.class);
        _mockParentService = mock(ParentService.class);

        _realBookingService = new BookingService(_mockBookingRepository, _mockPeriodService, _mockLessonService);
        _testService = new BookingServiceFacade(_realBookingService, _mockContentService);

        when(_mockBookingRepository.save(any(Booking.class))).then(AdditionalAnswers.returnsFirstArg());
    }

    @Test
    public void GIVEN_no_seats_or_spare_periods_EXPECT_the_booking_is_unfullfillable() {
        Child child = givenAChildExists();
        givenContentExists();
        givenContentIsPublished();

        Booking returnedBooking = _testService.createBooking("CHD001", 888L, OffsetDateTime.parse("2022-04-02T14:00Z"), 30, Arrays.asList(child));

        assertThat(returnedBooking).isNotNull();

        assertThat(returnedBooking.getStatus()).isEqualTo(BookingStatus.UNFULFILLABLE);
        assertThat(returnedBooking.getSeat()).isNull();

    }

    @Test
    public void GIVEN_a_seat_exists_EXPECT_that_seat_is_booked() {
        Child child = givenAChildExists();
        givenContentExists();
        givenContentIsPublished();
        givenALessonAndSeatExits("2022-04-02T14:00Z");

        Booking returnedBooking = _testService.createBooking("CHD001", 888L, OffsetDateTime.parse("2022-04-02T14:00Z"), 30, Arrays.asList(child));

        assertThat(returnedBooking).isNotNull();

        assertThat(returnedBooking.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(returnedBooking.getChild().getExternalId()).isEqualTo("CHD001");
        assertThat(returnedBooking.getSeat()).isNotNull();
        assertThat(returnedBooking.getSeat()).isSameAs(_testSeat);

    }

    @Test
    public void GIVEN_no_seats_available_EXPECT_a_new_period_is_used_to_create_a_lesson() {
    	Child child = givenAChildExists();
    	givenContentExists();
    	givenContentIsPublished();

    	OffsetDateTime periodFrom = OffsetDateTime.parse("2022-04-02T14:00Z");
    	OffsetDateTime periodTo = OffsetDateTime.parse("2022-04-02T14:30Z");

    	DomainUser teacher = DomainUser.builder().email("teacher@wfe.com").build();

    	Period bookablePeriod = Period
    			.builder()
    			.from(periodFrom)
    			.to(periodTo)
    			.owner(teacher)
    			.build();

    	List<Period> periods = List.of(bookablePeriod);

        Seat testSeat = Seat.builder().content(_testContentInfo).build();
        Lesson testLesson = Lesson.builder().period(bookablePeriod).content(_testContentInfo).build();
        testLesson.addSeat(testSeat);

    	when(_mockPeriodService.findBookablePeriodsForContent(periodFrom, _testContentInfo)).thenReturn(periods);
    	when(_mockLessonService.createLessonAndSeat(_testContentInfo, bookablePeriod)).thenReturn(testSeat);

        Booking returnedBooking = _testService.createBooking("CHD001", 888L, periodFrom, 30, Arrays.asList(child));

    	assertThat(returnedBooking).isNotNull();
    	assertThat(returnedBooking.getStatus()).isEqualTo(BookingStatus.PENDING);
    	assertThat(returnedBooking.getSeat()).isNotNull();
    	assertThat(returnedBooking.getSeat().getLesson()).isNotNull();
    	assertThat(returnedBooking.getSeat().getContent()).isEqualTo(_testContentInfo);
    	assertThat(returnedBooking.getSeat().getLesson().getPeriod()).isEqualTo(bookablePeriod);
    	assertThat(returnedBooking.getChild().getExternalId()).isEqualTo("CHD001");
    }

    @Test
    public void GIVEN_no_seats_for_given_time_EXPECT_the_bookings_is_unfullfillable() {
        Child child = givenAChildExists();
        givenContentExists();
        givenContentIsPublished();

        OffsetDateTime lessonBookingTime = OffsetDateTime.parse("2022-04-02T14:00Z");

        when(_mockLessonService.findFreeSeats(eq(_testContentInfo), eq(lessonBookingTime))).thenReturn(List.of());

        Booking returnedBooking = _testService.createBooking("CHD001", 888L, lessonBookingTime, 30, Arrays.asList(child));

        assertThat(returnedBooking).isNotNull();
        assertThat(returnedBooking.getStatus()).isEqualTo(BookingStatus.UNFULFILLABLE);
        assertThat(returnedBooking.getSeat()).isNull();
        assertThat(returnedBooking.getChild().getExternalId()).isEqualTo("CHD001");
    }


    @Test
    public void GIVEN_the_child_cannot_be_found_EXPECT_an_exception_is_thrown() {
        givenContentExists();
        OffsetDateTime lessonTime = OffsetDateTime.parse("2022-04-01T14:30Z");

        String errorMessage = assertThrows(IllegalArgumentException.class,
                () -> _testService.createBooking("CHD001", 888L, lessonTime, 30, Collections.emptyList())).getMessage();

        assertThat(errorMessage).isEqualTo("Could not find the associated child for creating the booking for the provided externalChildId CHD001");
    }
    
    @Test
    public void GIVEN_parent_child_input_data_AND_no_external_id_of_child_EXPECT_the_parent_child_created_with_booking_is_unfullfillable() {
    	
    	givenParentChildCreated();
    	Child child = givenAChildExists();
    	givenContentExists();
        givenContentIsPublished();
        
        Booking returnedBooking = _testService.createBooking("CHD001", 888L, OffsetDateTime.parse("2022-04-02T14:00Z"), 30, Arrays.asList(child));

        assertThat(returnedBooking).isNotNull();

        assertThat(returnedBooking.getStatus()).isEqualTo(BookingStatus.UNFULFILLABLE);
        assertThat(returnedBooking.getSeat()).isNull();
    }
    

    @Test
    public void GIVEN_no_timeout_provided_EXPECT_default_timeout_is_used() {
        Child child = givenAChildExists();
        givenContentExists();
        givenContentIsPublished();

        givenALessonAndSeatExits("2022-04-02T14:00Z");

        Booking returnedBooking = _testService.createBooking("CHD001", 888L, OffsetDateTime.parse("2022-04-02T14:00Z"), null, Arrays.asList(child));

        assertThat(returnedBooking.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(returnedBooking.getTimeout()).isEqualToIgnoringSeconds(OffsetDateTime.now().plusMinutes(6));
    }

    @Test
    public void GIVEN_timeout_is_zero_EXPECT_initial_status_is_booked() {
        Child child = givenAChildExists();
        givenContentExists();
        givenContentIsPublished();

        givenALessonAndSeatExits("2022-04-02T14:00Z");

        Booking returnedBooking = _testService.createBooking("CHD001", 888L, OffsetDateTime.parse("2022-04-02T14:00Z"), 0, Arrays.asList(child));

        assertThat(returnedBooking.getStatus()).isEqualTo(BookingStatus.BOOKED);
        assertThat(returnedBooking.getTimeout()).isEqualToIgnoringSeconds(OffsetDateTime.now());
    }

    @Test
    public void GIVEN_timeout_is_provided_AND_greater_than_zero_EXPECT_the_timeout_time_is_correctly_calculated() {
        Child child = givenAChildExists();
        givenContentExists();
        givenContentIsPublished();

        givenALessonAndSeatExits("2022-04-02T14:00Z");


        Booking returnedBooking = _testService.createBooking("CHD001", 888L, OffsetDateTime.parse("2022-04-02T14:00Z"), 30, Arrays.asList(child));

        assertThat(returnedBooking.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(returnedBooking.getTimeout()).isEqualToIgnoringSeconds(OffsetDateTime.now().plusMinutes(30));
    }

    private CreateParentInput generateParentChildTestData() {
    	
    	CreateChildInput createChildInput = CreateChildInput.newBuilder()
    			.capLevel(CapLevel.BLUE)
    			.displayName("Child_01_01")
    			.dateOfBirth(LocalDate.of(2002, 12, 12))
    			.uniqueExternalId("CHD001")
    			.build();
    	
    	List<CreateChildInput> createChildInputs = Arrays.asList(createChildInput);
    	
    	return CreateParentInput.newBuilder()
    			.displayName("Parent_01")
    			.givenName("ParentGiveName")
    			.familyName("ParentFamilyName")
    			.children(createChildInputs)
    			.build();
	}

	private Child generateTestChild(final long id, final String externalId, final String name) {
        return Child
                .builder()
                .id(id)
                .externalId(externalId)
                .displayName(name)
                .build();
    }

    private ContentInfo generateTestContent(final long id) {
        return ContentInfo.builder().level(CapLevel.BLUE).name("Some Blue Content").id(id).build();
    }

    private Child givenAChildExists() {
        Child child = generateTestChild(1, "CHD001", "Child_01_01");
        when(_mockChildRepository.findByExternalId("CHD001")).thenReturn(Optional.of(child));
        return child;
    }

    private void givenContentExists() {
        _testContentInfo = generateTestContent(888L);
        when(_mockContentService.getContentInfo(888L)).thenReturn(_testContentInfo);
    }

    private void givenContentIsPublished() {
        when(_mockContentService.isContentPublishedDuringPeriod(notNull(), notNull())).thenReturn(true);
    }

    private void givenALessonAndSeatExits(final String lessonStartTime) {
        OffsetDateTime lessonStartDateTime = OffsetDateTime.parse(lessonStartTime);
        Seat seat = Seat.builder().content(_testContentInfo).startTime(lessonStartDateTime).build();

        Lesson lesson = Lesson.builder().content(_testContentInfo).build();
        lesson.addSeat(seat);

        when(_mockLessonService.findFreeSeats(_testContentInfo, lessonStartDateTime)).thenReturn(List.of(seat));

        _testSeat = seat;
    }

    private void givenParentChildCreated() {

    	CreateParentInput createParentInput = generateParentChildTestData();
    	Parent parent = generateParentChildDomainData(createParentInput);
    	when(_mockParentService.createOrUpdateParent(createParentInput)).thenReturn(parent);
    }

    private Parent generateParentChildDomainData(CreateParentInput createParentInput) {

    	List<Child> childs = Arrays.asList(Child.builder()
    			.id(1L)
    			.externalId("CHD001")
    			.displayName("Child_01_01")
    			.capLevel(CapLevel.BLUE)
    			.build());

    	Parent parent = Parent.builder()
    			.displayName(createParentInput.getDisplayName())
    			.externalId(createParentInput.getUniqueExternalId())
    			.children(childs).build();

    	return parent;
    }
}