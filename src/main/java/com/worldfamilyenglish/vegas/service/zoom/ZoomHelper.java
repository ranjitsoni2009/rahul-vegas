package com.worldfamilyenglish.vegas.service.zoom;

import java.time.Duration;
import java.util.Base64;

import com.worldfamilyenglish.vegas.domain.ContentInfo;
import com.worldfamilyenglish.vegas.domain.DomainUser;
import com.worldfamilyenglish.vegas.domain.Period;
import com.worldfamilyenglish.vegas.service.zoom.domain.ZoomCreateMeetingRequst;
import com.worldfamilyenglish.vegas.service.zoom.domain.ZoomMeetingSettingRequest;

public class ZoomHelper {
	
	private static final String ENHANCED_ENCRYPTION = "enhanced_encryption";
	private static final String UTC_TIMEZONE = "UTC";

	public static ZoomCreateMeetingRequst createZoomMeetingRequest(ContentInfo contentInfo, Period period) {
		
		final String contactName = getName(period.getOwner());
		
		final int duration = getDuration(period);
		
		final ZoomMeetingSettingRequest meetingSettingRequest = ZoomMeetingSettingRequest.builder()
				.contactName(contactName)
				.contactEmail(period.getOwner().getEmail())
				.emailNotification(true)
				.encryptionType(ENHANCED_ENCRYPTION)
				.meetingAuthentication(false)
				.build();
		
		final ZoomCreateMeetingRequst zoomMeetingRequest = ZoomCreateMeetingRequst.builder()
				.agenda(contentInfo.getDescription())
				.topic(contentInfo.getName())
				.defaultPassword(true)
				.startTime(period.getFrom().toString())
				.duration(duration)
				.timezone(UTC_TIMEZONE)
				.meetingSettingRequest(meetingSettingRequest)
				.build();
		
		return zoomMeetingRequest;
	}

	private static int getDuration(final Period period) {

		return (int) Duration.between(period.getFrom(), period.getTo()).toMinutes();
	}

	private static String getName(final DomainUser owner) {
	
		return String.format("%s %s", owner.getGivenName(), owner.getFamilyName());
	}

	public static String getBase64EncodedAuthToken(final String clientId, final String clientSecret) {
		
		final String credentials = String.format("%s:%s", clientId, clientSecret);
    	final String base64EncodedAuthTokenStr = Base64.getEncoder().encodeToString(credentials.getBytes());
    	
    	return String.format("Basic %s", base64EncodedAuthTokenStr);	
	}

}
