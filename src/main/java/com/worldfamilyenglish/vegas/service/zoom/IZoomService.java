package com.worldfamilyenglish.vegas.service.zoom;

import com.worldfamilyenglish.vegas.domain.ContentInfo;
import com.worldfamilyenglish.vegas.domain.Period;

public interface IZoomService {
	
	public String createMeeting(final ContentInfo contentInfo, final Period period);
}
