package com.worldfamilyenglish.vegas.service.zoom;

import java.time.Duration;
import java.time.OffsetDateTime;

import com.worldfamilyenglish.vegas.service.zoom.domain.ZoomCreateMeetingRequst;
import com.worldfamilyenglish.vegas.service.zoom.domain.ZoomMeetingSettingRequest;

public class ZoomTestDataBuilder {
	
	public static ZoomCreateMeetingRequst createZoomMeetingTestRequest() {
		
		OffsetDateTime start = OffsetDateTime.parse("2022-08-21T07:30:55Z");
		OffsetDateTime end= OffsetDateTime.parse("2022-08-21T08:30:55Z");
		
		final String contactName = "GivenName FamilyName";
		final String email = "testowner@gmail.com";
		
		int duration = (int) Duration.between(start, end).toMinutes();
		
		ZoomMeetingSettingRequest meetingSettingRequest = ZoomMeetingSettingRequest.builder()
				.contactName(contactName)
				.contactEmail(email)
				.emailNotification(true)
				.encryptionType("enhanced_encryption")
				.meetingAuthentication(false)
				.build();
		
		ZoomCreateMeetingRequst zoomMeetingRequest = ZoomCreateMeetingRequst.builder()
				.agenda("Zoom Meeting Agenda")
				.topic("Zoom Meeting Topic")
				.defaultPassword(true)
				.startTime(start.toString())
				.duration(duration)
				.timezone("UTC")
				.meetingSettingRequest(meetingSettingRequest)
				.build();
		
		return zoomMeetingRequest;
	}
}
