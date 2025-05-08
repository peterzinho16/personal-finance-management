package com.bindord.financemanagement.model.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MicrosoftAccessTokenRecord(
    @JsonProperty("token_type")
    String tokenType,
    String scope,
    @JsonProperty("expires_in")
    Integer expiresIn,
    @JsonProperty("ext_expires_in")
    Integer extExpiresIn,
    @JsonProperty("access_token")
    String accessToken,
    @JsonProperty("id_token")
    String idToken
) {
}
