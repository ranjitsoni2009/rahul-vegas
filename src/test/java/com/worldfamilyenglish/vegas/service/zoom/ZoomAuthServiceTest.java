package com.worldfamilyenglish.vegas.service.zoom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.worldfamilyenglish.vegas.service.zoom.domain.ZoomTokenInfo;

public class ZoomAuthServiceTest {
	
	private ZoomTokenRefresher _mockTokenRefresher;
	private ZoomAuthService _zoomAuthService;
	private ZoomTokenInfo _zoomTokenInfo;
	
	@BeforeEach
	void setUp() {
		_mockTokenRefresher = mock(ZoomTokenRefresher.class);
		_zoomAuthService = new ZoomAuthService(_mockTokenRefresher);
		_zoomTokenInfo = new ZoomTokenInfo("testAccessToken", Instant.now());
	}
	
	@Test
    public void GIVEN_token_is_not_null_then_Expect_return_token() {
		
		when(_mockTokenRefresher.getZoomTokenInfo()).thenReturn(_zoomTokenInfo);
		String accessToken = _zoomAuthService.buildAccessToken();
		
		assertThat(accessToken).isEqualTo("Bearer testAccessToken");
    }
	
	@Test
    public void GIVEN_token_is_null_and_Expect_IllegalStateException_is_thrown() {
		
		when(_mockTokenRefresher.getZoomTokenInfo()).thenReturn(null);

		assertThrows(NullPointerException.class, () -> _zoomAuthService.buildAccessToken());
    }
}
