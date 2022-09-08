package com.worldfamilyenglish.vegas.service.zoom.domain;

import java.util.Date;

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
public class ZoomCreateMeetingResponse {

	@JsonProperty("uuid")
	public String _uuid;
	
	@JsonProperty("id")
	public long _id;
	
	@JsonProperty("host_id")
	public String _hostId;
	
	@JsonProperty("host_email")
	public String _hostEmail;
	
	@JsonProperty("topic")
	public String _topic;
	
	@JsonProperty("type")
	public int _type;
	
	@JsonProperty("status")
	public String _status;
	
	@JsonProperty("start_time")
	public Date _startTime;
	
	@JsonProperty("duration")
	public int _duration;
	
	@JsonProperty("timezone")
	public String _timezone;
	
	@JsonProperty("agenda")
	public String _agenda;
	
	@JsonProperty("created_at")
	public Date _createdAt;
	
	@JsonProperty("start_url")
	public String _startUrl;
	
	@JsonProperty("join_url")
	public String _joinUrl;
	
	@JsonProperty("password")
	public String _password;
	
	@JsonProperty("h323_password")
	public String _h323Password;
	
	@JsonProperty("pstn_password")
	public String _pstnPassword;
	
	@JsonProperty("encrypted_password")
	public String _encryptedPassword;
	
	@JsonProperty("pre_schedule")
	public boolean _preSchedule;
}