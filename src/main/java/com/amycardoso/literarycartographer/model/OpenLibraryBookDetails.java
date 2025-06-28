package com.amycardoso.literarycartographer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenLibraryBookDetails(
        String title,
        @JsonProperty("description")
        Object description,
        String key
) {
}