package com.worldfamilyenglish.vegas.service.zoom.domain;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(prefix = "_")
@Getter
@Builder
@AllArgsConstructor
public class ZoomTokenInfo {

    private final String _accessToken;
    
    private final Instant _lastTokenFetchTime;
}