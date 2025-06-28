package com.amycardoso.literarycartographer.service;

import com.amycardoso.literarycartographer.model.BookDoc;
import com.amycardoso.literarycartographer.model.LocationTimeAnalysis;
import com.amycardoso.literarycartographer.model.NominatimResponse;
import com.amycardoso.literarycartographer.model.OpenLibraryResponse;
import com.amycardoso.literarycartographer.model.OpenLibraryBookDetails;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class LiteraryCartographerService {

    @Value("classpath:/prompts/location-time-prompt.st")
    private Resource locationTimePromptResource;

    private final OpenLibraryClient openLibraryClient;
    private final NominatimClient nominatimClient;
    private final ChatClient chatClient;

    public LiteraryCartographerService(ChatClient.Builder chatClientBuilder, OpenLibraryClient openLibraryClient, NominatimClient nominatimClient) {
        this.openLibraryClient = openLibraryClient;
        this.nominatimClient = nominatimClient;
        this.chatClient = chatClientBuilder.build();
    }

    public LocationTimeAnalysis analyzeBook(String title) {
        OpenLibraryResponse openLibraryResponse = openLibraryClient.searchByTitle(title);
        if (openLibraryResponse == null || openLibraryResponse.docs() == null || openLibraryResponse.docs().isEmpty()) {
            return null; // Book not found or error during parsing
        }
        BookDoc book = openLibraryResponse.docs().get(0);
        String description = book.firstSentence() != null ? book.firstSentence() : "No description available.";
        String author = (book.authorName() != null && !book.authorName().isEmpty()) ?
                book.authorName().get(0) : "Unknown";

        // Fetch full book details for description and subjects
        OpenLibraryBookDetails bookDetails = openLibraryClient.getBookDetails(book.key().replace("/works/", ""));
        if (bookDetails != null && bookDetails.description() != null) {
            if (bookDetails.description() instanceof String) {
                description = (String) bookDetails.description();
            } else if (bookDetails.description() instanceof java.util.Map) {
                // Handle cases where description is an object with a 'value' field
                Object value = ((java.util.Map) bookDetails.description()).get("value");
                if (value instanceof String) {
                    description = (String) value;
                }
            }
        }

        BeanOutputConverter<LocationTimeAnalysis> converter = new BeanOutputConverter<>(LocationTimeAnalysis.class);
        PromptTemplate promptTemplate = new PromptTemplate(locationTimePromptResource);
        String userMessage = promptTemplate.render(
                Map.of(
                        "title", title,
                        "author", author,
                        "description", description,
                        "format", converter.getFormat()
                ));

        LocationTimeAnalysis aiResponse = chatClient.prompt()
                .user(userMessage)
                .call()
                .entity(LocationTimeAnalysis.class);

        // Null safety: if AI response is null, return a minimal object
        if (aiResponse == null) {
            return new LocationTimeAnalysis(
                title,
                author,
                null,
                null,
                null,
                null,
                null,
                null
            );
        }

        // Geocode location if not fictional
        Double latitude = aiResponse.latitude();
        Double longitude = aiResponse.longitude();
        if (aiResponse.location() != null && !aiResponse.fictional()) {
            java.util.List<NominatimResponse> geocodeResponse = nominatimClient.geocode(aiResponse.location());
            if (!geocodeResponse.isEmpty()) {
                NominatimResponse firstResult = geocodeResponse.get(0);
                latitude = firstResult.latitude();
                longitude = firstResult.longitude();
            }
        }

        return new LocationTimeAnalysis(
            aiResponse.title() != null ? aiResponse.title() : title,
            aiResponse.author() != null ? aiResponse.author() : author,
            aiResponse.location(),
            latitude,
            longitude,
            aiResponse.timePeriod(),
            aiResponse.fictional(),
            aiResponse.basedOnRealWorld()
        );
    }
}
