package com.worldfamilyenglish.vegas.zoomclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.worldfamilyenglish.vegas.service.zoom.domain.ZoomCreateMeetingRequst;
import com.worldfamilyenglish.vegas.service.zoom.domain.ZoomCreateMeetingResponse;


@FeignClient(
        value = "zoomClient",
        url = "https://api.zoom.us/v2"
)
public interface IZoomClient {

	final String AUTHORIZATION_HEADER_KEY = "Authorization";

	@PostMapping(value = "/users/{userId}/meetings")
	ZoomCreateMeetingResponse createMeeting(
			@RequestHeader(AUTHORIZATION_HEADER_KEY) final String accessToken, 
			@RequestBody final ZoomCreateMeetingRequst zoomMeetingRequest, 
			@RequestParam("userId") final String userId);
}
