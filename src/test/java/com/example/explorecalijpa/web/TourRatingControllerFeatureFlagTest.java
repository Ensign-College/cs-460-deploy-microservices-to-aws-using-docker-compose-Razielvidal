package com.example.explorecalijpa.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.explorecalijpa.business.TourRatingService;

/**
 * Verifies the tour rating API can be disabled via feature flags.
 * IMPORTANT: Authenticate as USER so we don't get 401 from Spring Security.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "features.tour-ratings=false")
public class TourRatingControllerFeatureFlagTest {

  private static final int TOUR_ID = 999;
  private static final String TOUR_RATINGS_URL = "/tours/" + TOUR_ID + "/ratings";

  @Autowired
  private TestRestTemplate template;

  private TestRestTemplate user;

  @MockBean
  private TourRatingService serviceMock;

  @BeforeEach
  void setUpAuth() {
    // must match your in-memory users in SecurityConfig
    user = template.withBasicAuth("user", "password");
  }

  @Test
  void ratingsDisabledReturnsNotFound() {
    ResponseEntity<String> res = user.getForEntity(TOUR_RATINGS_URL, String.class);
    assertThat(res.getStatusCode(), is(HttpStatus.NOT_FOUND));
  }
}
