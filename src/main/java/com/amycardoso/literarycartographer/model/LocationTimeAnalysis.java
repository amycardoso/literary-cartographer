package com.amycardoso.literarycartographer.model;

public record LocationTimeAnalysis(
    String title,
    String author,
    String location,
    Double latitude,
    Double longitude,
    String timePeriod,
    Boolean fictional,
    String basedOnRealWorld
) {}
