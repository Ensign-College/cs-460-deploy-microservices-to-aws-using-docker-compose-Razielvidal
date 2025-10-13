package edu.ensign.cs460.recommendation;

public record TourRecommendation(
    Integer tourId,
    String title,
    Double averageScore,
    Long reviewCount) {
}
