package com.amycardoso.literarycartographer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NominatimResponse(
        @JsonProperty("lat")
        Double latitude,
        @JsonProperty("lon")
        Double longitude,
        @JsonProperty("display_name")
        String displayName
) {
}