package edu.ensign.cs460.recommendation;

import com.example.explorecalijpa.repo.TourRatingRepository; // <- adjust if your repo package is different
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecommendationService {

  private final TourRatingRepository repo;

  public RecommendationService(TourRatingRepository repo) {
    this.repo = repo;
  }

  @Transactional(readOnly = true)
  public List<TourRecommendation> recommendTopN(int limit) {
    var page = PageRequest.of(0, limit);
    return repo.findTopTours(page).stream()
        .map(s -> new TourRecommendation(
            s.getTourId(), s.getTitle(), s.getAvgScore(), s.getReviewCount()))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<TourRecommendation> recommendForCustomer(int customerId, int limit) {
    var page = PageRequest.of(0, limit);
    return repo.findRecommendedForCustomer(customerId, page).stream()
        .map(s -> new TourRecommendation(
            s.getTourId(), s.getTitle(), s.getAvgScore(), s.getReviewCount()))
        .toList();
  }

  public void evictAll() {
    /* no-op (stretch endpoint) */ }
}
