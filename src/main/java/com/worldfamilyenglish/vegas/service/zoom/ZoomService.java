package com.worldfamilyenglish.vegas.service.zoom;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.worldfamilyenglish.vegas.domain.ContentInfo;
import com.worldfamilyenglish.vegas.domain.Period;
import com.worldfamilyenglish.vegas.service.zoom.domain.ZoomCreateMeetingRequst;
import com.worldfamilyenglish.vegas.service.zoom.domain.ZoomCreateMeetingResponse;
import com.worldfamilyenglish.vegas.zoomclient.IZoomClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ZoomService implements IZoomService {
	
	private final IZoomClient _zoomClient;
	private final ZoomAuthService _zoomAuthService;

	@Autowired
	public ZoomService(final IZoomClient zoomClient, final ZoomAuthService zoomAuthService) {
		_zoomClient = zoomClient;
		_zoomAuthService = zoomAuthService;
	}

	@Override
	public String createMeeting(final ContentInfo contentInfo, final Period period) {
		
		String zoomMeetingUrl = null;
		final ZoomCreateMeetingRequst zoomMeetingRequest = ZoomHelper.createZoomMeetingRequest(contentInfo, period);
		
		try {
			final ZoomCreateMeetingResponse zoomMeetingResponse = _zoomClient.createMeeting(
					_zoomAuthService.buildAccessToken(), zoomMeetingRequest, period.getOwner().getEmail());

			if (Objects.isNull(zoomMeetingResponse)) {
				LOG.error("Failed to create Zoom meeting for DomainUser's email {}.", period.getOwner().getEmail());
			} else {
				zoomMeetingUrl = zoomMeetingResponse.getJoinUrl();
				LOG.info("Created Zoom meeting with id: '{}'.", zoomMeetingResponse.getId());
			}
		}
		catch (Exception exception) {
			LOG.error("Exception while creating zoom meeting for DomainUser's email {} : {}", period.getOwner().getEmail(), exception.getMessage());
            throw new IllegalStateException(exception.getMessage());
		}
		
        return zoomMeetingUrl;
	}
	
}
