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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class LiteraryCartographerService {

    @Value("classpath:/prompts/location-time-prompt.st")
    private Resource locationTimePromptResource;

    private final OpenLibraryClient openLibraryClient;
    private final NominatimClient nominatimClient;
    private final ChatClient chatClient;

    private static final Logger logger = LoggerFactory.getLogger(LiteraryCartographerService.class);

    public LiteraryCartographerService(ChatClient.Builder chatClientBuilder, OpenLibraryClient openLibraryClient, NominatimClient nominatimClient) {
        this.openLibraryClient = openLibraryClient;
        this.nominatimClient = nominatimClient;
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * Analyzes a book by title, extracting likely location and time period using AI and geocoding services.
     * @param title The book title
     * @return LocationTimeAnalysis result, or null if not found or error
     */
    public LocationTimeAnalysis analyzeBook(String title) {
        OpenLibraryResponse openLibraryResponse = openLibraryClient.searchByTitle(title);
        if (openLibraryResponse == null || openLibraryResponse.docs() == null || openLibraryResponse.docs().isEmpty()) {
            logger.warn("No book found for title: {}", title);
            return null;
        }
        
        BookDoc book = openLibraryResponse.docs().get(0);
        String author = (book.authorName() != null && !book.authorName().isEmpty()) ?
                book.authorName().get(0) : "Unknown";
        OpenLibraryBookDetails bookDetails = openLibraryClient.getBookDetails(book.key().replace("/works/", ""));
        String description = extractDescription(book, bookDetails);

        BeanOutputConverter<LocationTimeAnalysis> converter = new BeanOutputConverter<>(LocationTimeAnalysis.class);
        PromptTemplate promptTemplate = new PromptTemplate(locationTimePromptResource);
        String userMessage = promptTemplate.render(
                Map.of(
                        "title", title,
                        "author", author,
                        "description", description,
                        "format", converter.getFormat()
                ));
        logger.debug("User message for AI: {}", userMessage);
        LocationTimeAnalysis aiResponse = null;
        try {
            aiResponse = chatClient.prompt()
                    .system("You are a literary analysis agent. Given a book description, extract the likely real-world location and time period where the story is set. If the story is set in a fictional world, state it clearly and provide a fictional reference instead.")
                    .user(userMessage)
                    .call()
                    .entity(LocationTimeAnalysis.class);
        } catch (Exception e) {
            logger.error("AI call or parsing failed: {}", e.getMessage(), e);
            return null;
        }

        if (aiResponse == null) {
            logger.warn("AI response is null for title: {}", title);
            return null;
        }

        return enrichWithGeocode(aiResponse, author, title);
    }

    /**
     * Extracts the best available description from book or book details.
     */
    private String extractDescription(BookDoc book, OpenLibraryBookDetails bookDetails) {
        String description = book.firstSentence() != null ? book.firstSentence() : "No description available.";
        if (bookDetails != null && bookDetails.description() != null) {
            if (bookDetails.description() instanceof String strDesc) {
                description = strDesc;
            } else if (bookDetails.description() instanceof java.util.Map<?,?> mapDesc) {
                Object value = mapDesc.get("value");
                if (value instanceof String strValue) {
                    description = strValue;
                }
            }
        }
        return description;
    }

    /**
     * Enriches the AI response with geocoding if the location is not fictional.
     */
    private LocationTimeAnalysis enrichWithGeocode(LocationTimeAnalysis aiResponse, String author, String title) {
        if (aiResponse.location() != null && Boolean.FALSE.equals(aiResponse.fictional())) {
            java.util.List<NominatimResponse> geocodeResponse = nominatimClient.geocode(aiResponse.location());
            if (!geocodeResponse.isEmpty()) {
                NominatimResponse firstResult = geocodeResponse.get(0);
                return new LocationTimeAnalysis(
                        aiResponse.title() != null ? aiResponse.title() : title,
                        aiResponse.author() != null ? aiResponse.author() : author,
                        aiResponse.location(),
                        firstResult.latitude(),
                        firstResult.longitude(),
                        aiResponse.timePeriod(),
                        aiResponse.fictional(),
                        aiResponse.basedOnRealWorld()
                );
            }
        }
        return aiResponse;
    }
    
}
