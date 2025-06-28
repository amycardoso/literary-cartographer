package com.amycardoso.literarycartographer.service;

import com.amycardoso.literarycartographer.model.OpenLibraryBookDetails;
import com.amycardoso.literarycartographer.model.OpenLibraryResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenLibraryClient {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public OpenLibraryClient(RestClient.Builder restClientBuilder, ObjectMapper objectMapper) {
        this.restClient = restClientBuilder.baseUrl("https://openlibrary.org").build();
        this.objectMapper = objectMapper;
    }

    public OpenLibraryResponse searchByTitle(String title) {
        try {
            String responseStr = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search.json")
                            .queryParam("title", title)
                            .queryParam("limit", 1)
                            .build())
                    .retrieve()
                    .body(String.class);
            if (responseStr == null) return null;
            return objectMapper.readValue(responseStr, OpenLibraryResponse.class);
        } catch (Exception e) {
            System.err.println("Error calling or parsing OpenLibrary API: " + e.getMessage());
            return null;
        }
    }

    public OpenLibraryBookDetails getBookDetails(String olId) {
        try {
            String responseStr = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/works/" + olId + ".json")
                            .build())
                    .retrieve()
                    .body(String.class);
            if (responseStr == null) return null;
            return objectMapper.readValue(responseStr, OpenLibraryBookDetails.class);
        } catch (Exception e) {
            System.err.println("Error calling or parsing OpenLibrary API for book details: " + e.getMessage());
            return null;
        }
    }
}
