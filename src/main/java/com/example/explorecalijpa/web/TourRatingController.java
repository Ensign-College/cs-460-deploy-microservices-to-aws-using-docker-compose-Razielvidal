package com.example.explorecalijpa.web;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.explorecalijpa.business.TourRatingService;
import com.example.explorecalijpa.config.FeatureFlagService;
import com.example.explorecalijpa.model.TourRating;

import io.swagger.v3.oas.annotations.Operation;

/**
 * Tour Rating REST Controller.
 *
 * Security expectations (from SecurityConfig):
 * - USER can GET (reads)
 * - ADMIN required for POST/PUT/PATCH/DELETE (writes)
 */
@RestController
@RequestMapping("/tours/{tourId}/ratings")
public class TourRatingController {

  private static final Logger log = LoggerFactory.getLogger(TourRatingController.class);

  private final TourRatingService tourRatingService;
  private final FeatureFlagService featureFlagService;

  public TourRatingController(TourRatingService tourRatingService,
      FeatureFlagService featureFlagService) {
    this.tourRatingService = tourRatingService;
    this.featureFlagService = featureFlagService;
  }

  /** Guard: ensures ratings endpoints are enabled via feature flag. */
  private void checkRatingsEnabled() {
    if (!featureFlagService.isEnabled("tour-ratings")) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour ratings feature disabled");
    }
  }

  // --------- READS (USER allowed) ---------

  @GetMapping
  @Operation(summary = "Lookup All Ratings for a Tour")
  public List<RatingDto> getAllRatingsForTour(@PathVariable("tourId") int tourId) {
    checkRatingsEnabled();
    log.info("GET /tours/{}/ratings", tourId);
    List<TourRating> tourRatings = tourRatingService.lookupRatings(tourId);
    return tourRatings.stream().map(RatingDto::new).toList();
  }

  @GetMapping("/average")
  @Operation(summary = "Get Average Score for a Tour")
  public Map<String, Double> getAverage(@PathVariable("tourId") int tourId) {
    checkRatingsEnabled();
    log.info("GET /tours/{}/ratings/average", tourId);
    return Map.of("average", tourRatingService.getAverageScore(tourId));
  }

  // --------- WRITES (ADMIN required) ---------

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a Rating for a Tour")
  public RatingDto createTourRating(@PathVariable("tourId") int tourId,
      @Valid @RequestBody RatingDto ratingDto) {
    checkRatingsEnabled();
    log.info("POST /tours/{}/ratings  body={}", tourId, ratingDto);
    TourRating rating = tourRatingService.createNew(
        tourId,
        ratingDto.getCustomerId(),
        ratingDto.getScore(),
        ratingDto.getComment());
    return new RatingDto(rating);
  }

  @PutMapping
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Update a Rating (PUT)")
  public void updateWithPut(@PathVariable("tourId") int tourId,
      @Valid @RequestBody RatingDto ratingDto) {
    checkRatingsEnabled();
    log.info("PUT /tours/{}/ratings  body={}", tourId, ratingDto);
    tourRatingService.update(
        tourId,
        ratingDto.getCustomerId(),
        ratingDto.getScore(),
        ratingDto.getComment());
  }

  @PatchMapping
  @Operation(summary = "Partially Update a Rating (PATCH)")
  public RatingDto updateWithPatch(@PathVariable("tourId") int tourId,
      @RequestBody RatingDto ratingDto) {
    checkRatingsEnabled();
    log.info("PATCH /tours/{}/ratings  body={}", tourId, ratingDto);
    // Your RatingDto likely has nullable getters (no Optional methods).
    // Wrap them here before calling the service:
    TourRating updated = tourRatingService.updateSome(
        tourId,
        ratingDto.getCustomerId(),
        Optional.ofNullable(ratingDto.getScore()),
        Optional.ofNullable(ratingDto.getComment()));
    return new RatingDto(updated);
  }

  @DeleteMapping("/{customerId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete a Rating by Customer")
  public void delete(@PathVariable("tourId") int tourId,
      @PathVariable("customerId") int customerId) {
    checkRatingsEnabled();
    log.info("DELETE /tours/{}/ratings/{}", tourId, customerId);
    tourRatingService.delete(tourId, customerId);
  }

  @PostMapping("/batch")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Give Many Tours Same Score")
  public void createManyTourRatings(@PathVariable("tourId") int tourId,
      @RequestParam("score") int score,
      @RequestBody List<Integer> customers) {
    checkRatingsEnabled();
    log.info("POST /tours/{}/ratings/batch score={} customers={}", tourId, score, customers);
    tourRatingService.rateMany(tourId, score, customers);
  }
}
