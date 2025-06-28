package com.amycardoso.literarycartographer.service;

import com.amycardoso.literarycartographer.model.BookDoc;
import com.amycardoso.literarycartographer.model.LocationTimeAnalysis;
import com.amycardoso.literarycartographer.model.OpenLibraryResponse;

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
    private final ChatClient chatClient;

    public LiteraryCartographerService(ChatClient.Builder chatClientBuilder, OpenLibraryClient openLibraryClient) {
        this.openLibraryClient = openLibraryClient;
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

        return new LocationTimeAnalysis(
            aiResponse.title() != null ? aiResponse.title() : title,
            aiResponse.author() != null ? aiResponse.author() : author,
            aiResponse.location(),
            aiResponse.latitude(),
            aiResponse.longitude(),
            aiResponse.timePeriod(),
            aiResponse.fictional(),
            aiResponse.basedOnRealWorld()
        );
    }
}
