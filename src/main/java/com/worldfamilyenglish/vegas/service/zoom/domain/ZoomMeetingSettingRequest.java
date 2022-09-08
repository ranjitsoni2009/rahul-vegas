package com.worldfamilyenglish.vegas.service.zoom.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

@Accessors(prefix = "_")
@Jacksonized
@Builder
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoomMeetingSettingRequest {
	
	@JsonProperty("contact_email")
    public String _contactEmail;
	
	@JsonProperty("contact_name")
    public String _contactName;
	
	@JsonProperty("email_notification")
    public boolean _emailNotification;
	
	@JsonProperty("encryption_type")
    public String _encryptionType;

	@JsonProperty("meeting_authentication")
	public boolean _meetingAuthentication;
	
}
