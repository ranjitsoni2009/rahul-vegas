package com.worldfamilyenglish.vegas.service.zoom;

import org.springframework.stereotype.Service;

import com.worldfamilyenglish.vegas.service.zoom.domain.ZoomTokenInfo;

@Service
public class ZoomAuthService {

    private static final String OAUTH_PREFIX = "Bearer ";

    private final ZoomTokenRefresher _zoomTokenRefresher;

    public ZoomAuthService(final ZoomTokenRefresher tokenRefresher) {
    	_zoomTokenRefresher = tokenRefresher;
    }

    public String getCurrentAccessToken() {
        ZoomTokenInfo zoomTokenInfo = _zoomTokenRefresher.getZoomTokenInfo();
        return zoomTokenInfo.getAccessToken();
    }

    public String buildAccessToken() {
        return OAUTH_PREFIX + getCurrentAccessToken();
    }
}
