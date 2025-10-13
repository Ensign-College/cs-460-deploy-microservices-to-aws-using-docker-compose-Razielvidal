package edu.ensign.cs460.recommendation;

import com.example.explorecalijpa.repo.TourRatingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RecommendationServiceTest {

  // Minimal projection stub to simulate DB results
  private static TourSummary ts(int id, String title, double avg, long count) {
    return new TourSummary() {
      public Integer getTourId() {
        return id;
      }

      public String getTitle() {
        return title;
      }

      public Double getAvgScore() {
        return avg;
      }

      public Long getReviewCount() {
        return count;
      }
    };
  }

  @Test
  void recommendTopN_maps_and_uses_page_request() {
    var repo = mock(TourRatingRepository.class);
    var svc = new RecommendationService(repo);

    when(repo.findTopTours(PageRequest.of(0, 3)))
        .thenReturn(List.of(
            ts(2, "In the Steps of John Muir", 5.0, 1),
            ts(1, "Big Sur Retreat", 4.0, 8),
            ts(3, "Zion Day Trip", 4.0, 3)));

    var out = svc.recommendTopN(3);

    assertThat(out).hasSize(3);
    assertThat(out.get(0).tourId()).isEqualTo(2);
    assertThat(out.get(1).title()).isEqualTo("Big Sur Retreat");
    assertThat(out.get(2).averageScore()).isEqualTo(4.0);

    verify(repo).findTopTours(PageRequest.of(0, 3));
  }

  @Test
  void recommendTopN_empty_is_ok() {
    var repo = mock(TourRatingRepository.class);
    var svc = new RecommendationService(repo);

    when(repo.findTopTours(PageRequest.of(0, 5))).thenReturn(List.of());

    var out = svc.recommendTopN(5);
    assertThat(out).isEmpty();
  }

  @Test
  void recommendForCustomer_maps_and_uses_page_request() {
    var repo = mock(TourRatingRepository.class);
    var svc = new RecommendationService(repo);

    when(repo.findRecommendedForCustomer(123, PageRequest.of(0, 2)))
        .thenReturn(List.of(
            ts(10, "Coastal Bike Ride", 4.7, 44),
            ts(11, "Wine Country Day Trip", 4.6, 62)));

    var out = svc.recommendForCustomer(123, 2);

    assertThat(out).extracting(TourRecommendation::tourId)
        .containsExactly(10, 11);

    verify(repo).findRecommendedForCustomer(123, PageRequest.of(0, 2));
  }
}
