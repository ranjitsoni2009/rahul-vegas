package com.worldfamilyenglish.vegas.zoomclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.worldfamilyenglish.vegas.service.zoom.domain.ZoomTokenResponseDTO;


@FeignClient(value = "zoomLoginClient", url = "https://zoom.us")
public interface IZoomAuthClient {

	final String AUTHORIZATION_HEADER_KEY = "Authorization";

	@PostMapping(value = "/oauth/token?grant_type=account_credentials&account_id={accountId}")
    ZoomTokenResponseDTO getAccessToken(
    		@RequestHeader(AUTHORIZATION_HEADER_KEY) final String authToken, 
    		@RequestParam("accountId") String accountId);
}