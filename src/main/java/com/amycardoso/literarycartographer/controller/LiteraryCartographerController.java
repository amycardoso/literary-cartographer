package com.amycardoso.literarycartographer.controller;

import com.amycardoso.literarycartographer.model.LocationTimeAnalysis;
import com.amycardoso.literarycartographer.service.LiteraryCartographerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/books")
public class LiteraryCartographerController {

    private final LiteraryCartographerService literaryCartographerService;

    public LiteraryCartographerController(LiteraryCartographerService literaryCartographerService) {
        this.literaryCartographerService = literaryCartographerService;
    }

    @GetMapping("/analyze")
    public LocationTimeAnalysis analyzeBook(@RequestParam String title) {
        return literaryCartographerService.analyzeBook(title);
    }

}
