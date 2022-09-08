package com.worldfamilyenglish.vegas.zoom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.worldfamilyenglish.vegas.configuration.ZoomConfiguration;
import com.worldfamilyenglish.vegas.configuration.ZoomConfiguration.ZoomClientConfig;
import com.worldfamilyenglish.vegas.service.zoom.ZoomTokenRefresher;
import com.worldfamilyenglish.vegas.service.zoom.domain.ZoomTokenInfo;
import com.worldfamilyenglish.vegas.service.zoom.domain.ZoomTokenResponseDTO;
import com.worldfamilyenglish.vegas.zoomclient.IZoomAuthClient;

public class ZoomTokenRefresherTest {

	private ZoomConfiguration _mockZoomConfiguration;
	private IZoomAuthClient _mockZoomAuthClient;
	
	private ZoomTokenRefresher _zoomTokenRefresher;
	
	private ZoomConfiguration.ZoomClientConfig _clientConfig;
	
	@BeforeEach
	void setUp() {
		_mockZoomConfiguration = mock(ZoomConfiguration.class);
		_mockZoomAuthClient = mock(IZoomAuthClient.class);
		_clientConfig = mock(ZoomClientConfig.class);
		
		_zoomTokenRefresher = new ZoomTokenRefresher(_mockZoomConfiguration, _mockZoomAuthClient);
	}
	
	@Test
	public void GIVEN_token_is_null_EXPECT_new_token_is_returned() {
		
		when(_mockZoomConfiguration.getClientConfig()).thenReturn(_clientConfig);
		when(_clientConfig.getAccountId()).thenReturn("accountId");
		when(_clientConfig.getClientId()).thenReturn("clientId");
		when(_clientConfig.getClientSecret()).thenReturn("clientSecret");
		
		ZoomTokenResponseDTO _zoomTokenResponseDTO = ZoomTokenResponseDTO.builder()
				.accessToken("testAccessToken").build();
		
		when(_mockZoomAuthClient.getAccessToken(Mockito.anyString(), Mockito.anyString())).thenReturn(_zoomTokenResponseDTO);
		
		ZoomTokenInfo zoomTokenInfo = _zoomTokenRefresher.getZoomTokenInfo();
		
		assertThat(zoomTokenInfo.getAccessToken()).isEqualTo("testAccessToken");
	}
	
	@Test
	public void GIVEN_token_has_not_expired_EXPECT_same_token_has_returned() {

		ZoomTokenInfo _zoomTokenInfo = ZoomTokenInfo.builder()
				.accessToken("testAccessToken")
				.lastTokenFetchTime(Instant.now())
				.build();

		_zoomTokenRefresher.set_zoomTokenInfo(_zoomTokenInfo);

		ZoomTokenInfo zoomTokenInfo = _zoomTokenRefresher.getZoomTokenInfo();	

		assertThat(zoomTokenInfo.getAccessToken()).isEqualTo("testAccessToken");
	}
	
	@Test
	public void GIVEN_token_has_expired_EXPECT_new_token_is_requested() {
		
		Instant instant = Instant.parse("2022-08-24T15:11:45.352228900Z");
		
		ZoomTokenInfo _zoomTokenInfo = ZoomTokenInfo.builder()
				.accessToken("oldAccessToken")
				.lastTokenFetchTime(instant)
				.build();

		_zoomTokenRefresher.set_zoomTokenInfo(_zoomTokenInfo);
		
		when(_mockZoomConfiguration.getClientConfig()).thenReturn(_clientConfig);
		when(_clientConfig.getAccountId()).thenReturn("accountId");
		when(_clientConfig.getClientId()).thenReturn("clientId");
		when(_clientConfig.getClientSecret()).thenReturn("clientSecret");
		
		ZoomTokenResponseDTO _zoomTokenResponseDTO = ZoomTokenResponseDTO.builder()
				.accessToken("newAccessToken").build();
		
		when(_mockZoomAuthClient.getAccessToken(Mockito.anyString(), Mockito.anyString())).thenReturn(_zoomTokenResponseDTO);

		ZoomTokenInfo zoomTokenInfo = _zoomTokenRefresher.getZoomTokenInfo();	

		assertThat(zoomTokenInfo.getAccessToken()).isEqualTo("newAccessToken");
	}
	
	@Test
	public void GIVEN_accesss_token_is_null_EXPECT_zoomAuthClient_return_null_response() {

		when(_mockZoomConfiguration.getClientConfig()).thenReturn(_clientConfig);
		when(_clientConfig.getAccountId()).thenReturn("accountId");
		when(_clientConfig.getClientId()).thenReturn("clientId");
		when(_clientConfig.getClientSecret()).thenReturn("clientSecret");
		
		when(_mockZoomAuthClient.getAccessToken(Mockito.anyString(), Mockito.anyString())).thenThrow(IllegalStateException.class);

		assertThrows(IllegalStateException.class, () -> _zoomTokenRefresher.getZoomTokenInfo());
	}
	
}
