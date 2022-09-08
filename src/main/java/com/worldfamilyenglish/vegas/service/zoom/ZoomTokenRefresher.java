package com.worldfamilyenglish.vegas.service.zoom;

import static java.time.temporal.ChronoUnit.MINUTES;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.worldfamilyenglish.vegas.configuration.ZoomConfiguration;
import com.worldfamilyenglish.vegas.service.zoom.domain.ZoomTokenInfo;
import com.worldfamilyenglish.vegas.service.zoom.domain.ZoomTokenResponseDTO;
import com.worldfamilyenglish.vegas.zoomclient.IZoomAuthClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ZoomTokenRefresher {

    /**
     * Zoom tokens last for 60 minutes, so use 55 minutes as a safety buffer
     **/
    private static final int TOKEN_LIFESPAN_IN_MINUTES = 55;

    private final ZoomConfiguration _zoomConfiguration;
    private final IZoomAuthClient _zoomAuthClient;

    private ZoomTokenInfo _zoomTokenInfo;
    
    public ZoomTokenRefresher(final ZoomConfiguration zoomConfiguration, final IZoomAuthClient zoomAuthClient) {
        _zoomConfiguration = zoomConfiguration;
        _zoomAuthClient = zoomAuthClient;
    }

    public synchronized ZoomTokenInfo getZoomTokenInfo() {
        Instant now = Instant.now();

        if (_zoomTokenInfo == null || tokenHasExpired(now)) {
            LOG.debug("There's no current authentication token, it's either not available or expired");
            _zoomTokenInfo = refreshZoomTokenInfo();
        }

        return _zoomTokenInfo;
    }

    private boolean tokenHasExpired(final Instant currentTime) {
        return MINUTES.between(_zoomTokenInfo.getLastTokenFetchTime(), currentTime) > TOKEN_LIFESPAN_IN_MINUTES;
    }

    private ZoomTokenInfo refreshZoomTokenInfo() {

    	ZoomConfiguration.ZoomClientConfig clientConfig = _zoomConfiguration.getClientConfig();

    	ZoomTokenInfo updatedZoomTokenInfo;

    	final String base64EncodedAuthToken = ZoomHelper.getBase64EncodedAuthToken(clientConfig.getClientId(), clientConfig.getClientSecret());

    	try {
    		ZoomTokenResponseDTO zoomTokenResponseDTO = _zoomAuthClient.getAccessToken(base64EncodedAuthToken, clientConfig.getAccountId());

    		LOG.debug("Login response: {}", zoomTokenResponseDTO);

    		updatedZoomTokenInfo = new ZoomTokenInfo(zoomTokenResponseDTO.getAccessToken(), Instant.now());

    	} catch (final Exception ex) {
    		LOG.error("Error during token refresh / login operation", ex);
    		throw new IllegalStateException("Could not fetch new authentication token.");
    	}

    	return updatedZoomTokenInfo;
    }

	public void set_zoomTokenInfo(ZoomTokenInfo zoomTokenInfo) {
		_zoomTokenInfo = zoomTokenInfo;
	}
}
