package com.worldfamilyenglish.vegas.service.zoom.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

@Accessors(prefix = "_")
@Jacksonized
@Builder
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoomTokenResponseDTO {

    @JsonProperty("access_token")
    String _accessToken;

    @JsonProperty("token_type")
    String _tokenType;

    @JsonProperty("expires_in")
    Long _expiresIn;
    
    @JsonProperty("scope")
    String _scope;

}
