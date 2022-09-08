package com.worldfamilyenglish.vegas.service.zoom.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

@Accessors(prefix = "_")
@Jacksonized
@Builder
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoomTokenRequestDTO {

    /**
     * Snake case for request POJO is intentional. The feign client isn't picking up the
     *
     * @JsonProperty from Jackson so I had to use the snake case in original POJO attributes.
     */
    String _refresh_token;
    String _client_id;
    String _client_secret;
    String _grant_type;
}
