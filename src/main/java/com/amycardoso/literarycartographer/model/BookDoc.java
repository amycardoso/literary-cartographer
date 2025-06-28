package com.amycardoso.literarycartographer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record BookDoc(
        String key,
        @JsonProperty("first_sentence")
        String firstSentence,
        @JsonProperty("author_name")
        List<String> authorName,
        String description,
        List<String> subject
) {
}
