package com.worldfamilyenglish.vegas.integration;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.MonoGraphQLClient;
import com.worldfamilyenglish.vegas.domain.Booking;
import com.worldfamilyenglish.vegas.domain.CapLevel;
import com.worldfamilyenglish.vegas.domain.Child;
import com.worldfamilyenglish.vegas.domain.ContentInfo;
import com.worldfamilyenglish.vegas.domain.Parent;
import com.worldfamilyenglish.vegas.mutation.BookingMutation;
import com.worldfamilyenglish.vegas.service.BookingService;
import com.worldfamilyenglish.vegas.service.BookingServiceFacade;
import com.worldfamilyenglish.vegas.service.ContentService;
import com.worldfamilyenglish.vegas.service.ParentService;
import com.worldfamilyenglish.vegas.service.zoom.ZoomAuthService;
import com.worldfamilyenglish.vegas.service.zoom.domain.ZoomCreateMeetingResponse;
import com.worldfamilyenglish.vegas.types.CreateBookingInput;
import com.worldfamilyenglish.vegas.types.CreateChildInput;
import com.worldfamilyenglish.vegas.types.CreateParentInput;
import com.worldfamilyenglish.vegas.zoomclient.IZoomClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BookingMutationTest {

	private MonoGraphQLClient _monoGraphQLClient;
	
	@MockBean
	private ParentService _parentService;
	
	@MockBean
	private ContentService _contentService;
	
	@MockBean
	private BookingService _bookingService;
	
	@MockBean
    private IZoomClient _mockZoomClient;

    @MockBean
    private ZoomAuthService _mockZoomAuthService;
    
    @MockBean
    private BookingServiceFacade _mockBookingServiceFacade;
	
	public BookingMutationTest(@LocalServerPort Integer port) {
		String baseUrl = format("http://localhost:%s/graphql", port.toString());
        WebClient webClient = WebClient.create(baseUrl);
        
        _monoGraphQLClient = MonoGraphQLClient.createWithWebClient(webClient);
    }
	
    @Test
    public void GIVEN_bookable_value_EXPECT_booking() {
    	
    	givenZoomMeetingsGetCreated();
    	
    	CreateBookingInput input = generateTestBookingInput();
    	
    	Optional<Parent> parentDomainObject = generateParentChildDomainData(generateTestParentObjectWithChild());
		List<Child> childs = parentDomainObject.get().getChildren();
    	
    	when(_parentService.getAssociatedChildren(Mockito.anyString(), Mockito.any())).thenReturn(childs);
    	
    	ContentInfo foundContentInfo = ContentInfo.builder()
    			.id(1L)
    			.externalUrl("externalUrl")
    			.level(CapLevel.BLUE)
    			.build();
    	
    	when(_contentService.getContentInfo(1L)).thenReturn(foundContentInfo);
    	when(_contentService.isContentPublishedDuringPeriod(OffsetDateTime.parse("2022-08-21T11:00Z"), foundContentInfo)).thenReturn(true);
    	
    	when(_mockBookingServiceFacade.createBooking(input.getExternalChildId(), (long) input.getContentId(), input.getLessonTime(),
                input.getTimeoutMinutes(), childs)).thenReturn(null);
    	
    	final String createBookingQuery = getCreateBookingQuery();
        GraphQLResponse response = _monoGraphQLClient.reactiveExecuteQuery(createBookingQuery).block();
        Booking booking = response.extractValueAsObject("createBooking.id", Booking.class);

        assertThat(booking.getId()).isNotNull();
    }

	private void givenZoomMeetingsGetCreated() {
        ZoomCreateMeetingResponse zoomMeetingResponse = ZoomCreateMeetingResponse.builder().joinUrl("joinUrl").build();
        when(_mockZoomClient.createMeeting(anyString(), any(), anyString())).thenReturn(zoomMeetingResponse);
        when(_mockZoomAuthService.buildAccessToken()).thenReturn("testAccessToken");
    }
	
	private Optional<Parent> generateParentChildDomainData(CreateParentInput createParentInput) {

    	List<Child> childs = Arrays.asList(Child.builder()
    			.id(1L)
    			.externalId(createParentInput.getChildren().get(0).getUniqueExternalId())
    			.displayName(createParentInput.getChildren().get(0).getDisplayName())
    			.capLevel(CapLevel.BLUE)
    			.build()); 

    	Parent parent = Parent.builder()
    			.displayName(createParentInput.getDisplayName())
    			.externalId(createParentInput.getUniqueExternalId())
    			.children(childs).build();

		childs.get(0).setParent(parent);

		return Optional.ofNullable(parent);
    }
	
	private CreateBookingInput generateTestBookingInput(){
		
		return CreateBookingInput.newBuilder()
				.externalChildId("CHD001")
				.contentId(1)
				.createParentInput(generateTestParentObjectWithChild())
				.build();
	}
	
	private CreateParentInput generateTestParentObjectWithChild() {

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
				.uniqueExternalId("P1478")
				.children(createChildInputs)
				.build();
	}

	private String getCreateBookingQuery() {
    	
    	return """ 
    			mutation CREATE_A_BOOKING {
    			  createBooking (input: {	
    			    externalChildId: "CHD001",
    			    contentId: 1,
    			    lessonTime: "2022-08-21T12:30:00.00Z",
    			    createParentInput: {
    			      uniqueExternalId: "Parent01",
    			      givenName: "testparent01",
    			      familyName: "testfamily01",
    			      displayName: "testdisplay01",
    			      children: {
    			        uniqueExternalId: "CHD001",
    			        displayName: "testChildDisplay101",
    			        capLevel: BLUE,
    			        dateOfBirth: "2012-12-12"
    			      }
    			    },
    			    timeoutMinutes: 5
    			  }) {
    			        id
				        status
				        seat {
				            id
				            lesson {
				                id
				                seats {
				                    id
				                    lesson {
				                        id
				                        content
				                        zoomMeetingUrl
				                    }
				                    content
				                }
				                period {
				                    id
				                    from
				                    to
				                    isBookable
				                    type
				                }
				                content
				                zoomMeetingUrl
				            }
				            content
				            booking {
				                id
				                status
				                seat {
				                    id
				                    lesson {
				                        id
				                        content
				                        zoomMeetingUrl
				                    }
				                    content
				                }
				                timeout
				                created
				                updated
				            }
				        }
				        
				        created
				        updated
  }}
    			""";
	}

	//@Test
    public void GIVEN_null_input_EXPECT_exception_is_thrown() {
        BookingServiceFacade mockBookingServiceFacade = mock(BookingServiceFacade.class);
        ParentService mockParentService = mock(ParentService.class);

        BookingMutation testMutation = new BookingMutation(mockBookingServiceFacade, mockParentService);

        assertThrows(IllegalArgumentException.class, () -> testMutation.createBooking(null));
    }


    private CreateBookingInput createBookingInput(){
    	
        return CreateBookingInput
                .newBuilder()
                .externalChildId("CHD001")
                .createParentInput(generateParentChildTestData())
                .contentId(1)
                .timeoutMinutes(5).build();

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

}