package com.worldfamilyenglish.vegas.service.zoom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.worldfamilyenglish.vegas.domain.ContentInfo;
import com.worldfamilyenglish.vegas.domain.DomainUser;
import com.worldfamilyenglish.vegas.domain.Period;
import com.worldfamilyenglish.vegas.service.zoom.domain.ZoomCreateMeetingRequst;
import com.worldfamilyenglish.vegas.service.zoom.domain.ZoomCreateMeetingResponse;
import com.worldfamilyenglish.vegas.zoomclient.IZoomClient;

public class ZoomServiceTest {
	
	private IZoomClient _mockZoomClient;
	private ZoomAuthService _mockZoomAuthService;
	
	private ZoomService _zoomService;
	
	@BeforeEach
	void setUp() {
		_mockZoomClient = mock(IZoomClient.class);
		_mockZoomAuthService = mock(ZoomAuthService.class);
		
		_zoomService = new ZoomService(_mockZoomClient, _mockZoomAuthService);
	}
	
	@Test
    public void GIVEN_valid_content_info_and_period_with_registered_zoom_user_EXPECT_zoom_meeting_is_created() {
		
		final String joiningUrl = "https://us05web.zoom.us/j/87121069370?pwd=Z0l5SmRvMTdrc2liemFBSjduZzhlQT09"; 
		
		ZoomCreateMeetingRequst zoomMeetingRequest = ZoomTestDataBuilder.createZoomMeetingTestRequest();

		ZoomCreateMeetingResponse zoomMeetingResponse = ZoomCreateMeetingResponse.builder()
				.joinUrl(joiningUrl)
				.build();
		
		when(_mockZoomClient.createMeeting("accessToken", zoomMeetingRequest, "userId")).thenReturn(zoomMeetingResponse);
		
		ContentInfo contentInfo = ContentInfo.builder()
				.name("name")
				.description("description")
				.build();
		
		DomainUser domainUser = DomainUser.builder()
				.givenName("GivenName")
				.familyName("FamilyName")
				.email("teacher@wfe.com").build();
		
		OffsetDateTime from = OffsetDateTime.parse("2022-08-21T07:30:55Z");
		OffsetDateTime to = OffsetDateTime.parse("2022-08-21T08:30:55Z");
		
		Period period = Period.builder()
				.owner(domainUser)
				.from(from)
				.to(to)
				.build();
		
		_zoomService.createMeeting(contentInfo, period);
		
        assertThat(zoomMeetingResponse.getJoinUrl()).isEqualTo(joiningUrl);
    }
	
	@Test
    public void GIVEN_ZoomMeetingResponse_is_null_EXPECT_return_null_joining_url() {
		
		ZoomCreateMeetingRequst zoomMeetingRequest = ZoomTestDataBuilder.createZoomMeetingTestRequest();

		when(_mockZoomClient.createMeeting("accessToken", zoomMeetingRequest, "userId")).thenReturn(null);
		
		ContentInfo contentInfo = ContentInfo.builder()
				.name("name")
				.description("description")
				.build();
		
		DomainUser domainUser = DomainUser.builder()
				.givenName("GivenName")
				.familyName("FamilyName")
				.email(null).build();
		
		OffsetDateTime from = OffsetDateTime.parse("2022-08-21T07:30:55Z");
		OffsetDateTime to = OffsetDateTime.parse("2022-08-21T08:30:55Z");
		
		Period period = Period.builder()
				.owner(domainUser)
				.from(from)
				.to(to)
				.build();
		
		String joiningUrl = _zoomService.createMeeting(contentInfo, period);
		
        assertThat(joiningUrl).isNull();
    }
	
	@Test
    public void GIVEN_owner_email_is_not_registered_with_zoom_EXPECT_exception_is_thrown() {

		ContentInfo contentInfo = ContentInfo.builder()
				.name("name")
				.description("description")
				.build();
		
		DomainUser domainUser = DomainUser.builder()
				.givenName("GivenName")
				.familyName("FamilyName")
				.email("teacher@wfe.com")
				.build();
		
		OffsetDateTime from = OffsetDateTime.parse("2022-08-21T07:30:55Z");
		OffsetDateTime to = OffsetDateTime.parse("2022-08-21T08:30:55Z");
		
		Period period = Period.builder()
				.owner(domainUser)
				.from(from)
				.to(to)
				.build();
		
		when(_mockZoomAuthService.buildAccessToken()).thenReturn("testAccessToken");
		when(_mockZoomClient.createMeeting(Mockito.anyString(), Mockito.any(), Mockito.anyString())).thenThrow(IllegalStateException.class);
		
		assertThrows(IllegalStateException.class, () -> _zoomService.createMeeting(contentInfo, period));
    }
	
}
