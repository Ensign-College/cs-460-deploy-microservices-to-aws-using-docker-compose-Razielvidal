package edu.ensign.cs460.recommendation;

public interface TourSummary {
  Integer getTourId();

  String getTitle();

  Double getAvgScore();

  Long getReviewCount();
}
