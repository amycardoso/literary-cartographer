package com.amycardoso.literarycartographer.service;

import com.amycardoso.literarycartographer.model.NominatimResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

@Component
public class NominatimClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public NominatimClient(RestClient.Builder restClientBuilder, ObjectMapper objectMapper) {
        this.restClient = restClientBuilder.baseUrl("https://nominatim.openstreetmap.org").build();
        this.objectMapper = objectMapper;
    }

    public List<NominatimResponse> geocode(String location) {
        try {
            String responseStr = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("q", location)
                            .queryParam("format", "json")
                            .queryParam("limit", 1)
                            .build())
                    .retrieve()
                    .body(String.class);
            if (responseStr == null) return List.of();
            return Arrays.asList(objectMapper.readValue(responseStr, NominatimResponse[].class));
        } catch (Exception e) {
            System.err.println("Error calling or parsing Nominatim API: " + e.getMessage());
            return List.of();
        }
    }
}