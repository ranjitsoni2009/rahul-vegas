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
public class ZoomCreateMeetingRequst {
	  
	@JsonProperty("agenda")
	public String _agenda;
	
	@JsonProperty("default_password")
    public boolean _defaultPassword;

	@JsonProperty("duration")
	public int _duration;

	@JsonProperty("settings")
    public ZoomMeetingSettingRequest _meetingSettingRequest;
	
	@JsonProperty("start_time")
    public String _startTime;
	
	@JsonProperty("timezone")
    public String _timezone;
	
	@JsonProperty("topic")
    public String _topic;
	
}
