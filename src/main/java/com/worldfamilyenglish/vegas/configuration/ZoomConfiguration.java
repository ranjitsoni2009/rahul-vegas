package com.worldfamilyenglish.vegas.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("zoom")
@Getter
@Setter
public class ZoomConfiguration {

    private ZoomClientConfig _clientConfig;

    @Getter
    @Setter
    public static class ZoomClientConfig {

    	private String _accountId;
    	
        private String _clientId;

        private String _clientSecret;

    }
}