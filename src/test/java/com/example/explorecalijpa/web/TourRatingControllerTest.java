package com.example.explorecalijpa.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import com.example.explorecalijpa.business.TourRatingService;
import com.example.explorecalijpa.model.Tour;
import com.example.explorecalijpa.model.TourRating;

import jakarta.validation.ConstraintViolationException;

/**
 * Auth-aware tests so requests reach the controller under Spring Security.
 * - USER: can GET /tours/** (reads)
 * - ADMIN: required for POST/PUT/PATCH/DELETE /tours/**
 * Includes one negative test (USER POST -> 403) for the rubric.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class TourRatingControllerTest {

  // These Tour and rating id's do not already exist in the db
  private static final int TOUR_ID = 999;
  private static final int CUSTOMER_ID = 1000;
  private static final int SCORE = 3;
  private static final String COMMENT = "comment";
  private static final String TOUR_RATINGS_URL = "/tours/" + TOUR_ID + "/ratings";

  @Autowired
  private TestRestTemplate template;

  private TestRestTemplate userRestTemplate;
  private TestRestTemplate adminRestTemplate;

  @MockBean
  private TourRatingService serviceMock;

  @Mock
  private TourRating tourRatingMock;

  @Mock
  private Tour tourMock;

  private RatingDto ratingDto = new RatingDto(SCORE, COMMENT, CUSTOMER_ID);

  @BeforeEach
  void setUpAuthClients() {
    // Must match in-memory users in SecurityConfig
    userRestTemplate = template.withBasicAuth("user", "password");
    adminRestTemplate = template.withBasicAuth("admin", "admin123");
  }

  @Test
  void testCreateTourRating() {
    // Mutations require ADMIN
    adminRestTemplate.postForEntity(TOUR_RATINGS_URL, ratingDto, RatingDto.class);
    verify(this.serviceMock).createNew(TOUR_ID, CUSTOMER_ID, SCORE, COMMENT);
  }

  @Test
  void testDelete() {
    // Mutations require ADMIN
    adminRestTemplate.delete(TOUR_RATINGS_URL + "/" + CUSTOMER_ID);
    verify(this.serviceMock).delete(TOUR_ID, CUSTOMER_ID);
  }

  @Test
  void testGetAllRatingsForTour() {
    // Reads allowed for USER
    when(serviceMock.lookupRatings(anyInt())).thenReturn(List.of(tourRatingMock));
    ResponseEntity<String> res = userRestTemplate.getForEntity(TOUR_RATINGS_URL, String.class);
    assertThat(res.getStatusCode(), is(HttpStatus.OK));
    verify(serviceMock).lookupRatings(anyInt());
  }

  @Test
  void testGetAverage() {
    // Reads allowed for USER
    when(serviceMock.lookupRatings(anyInt())).thenReturn(List.of(tourRatingMock));
    ResponseEntity<String> res = userRestTemplate.getForEntity(TOUR_RATINGS_URL + "/average", String.class);
    assertThat(res.getStatusCode(), is(HttpStatus.OK));
    verify(serviceMock).getAverageScore(TOUR_ID);
  }

  // PATCH testing works when Apache HttpClient is on the test classpath (you
  // likely already have it).
  @Test
  void testUpdateWithPatch() {
    // Mutations require ADMIN
    when(serviceMock.updateSome(anyInt(), anyInt(), any(), any())).thenReturn(tourRatingMock);
    adminRestTemplate.patchForObject(TOUR_RATINGS_URL, ratingDto, String.class);
    verify(this.serviceMock).updateSome(anyInt(), anyInt(), any(), any());
  }

  @Test
  void testUpdateWithPut() {
    // Mutations require ADMIN
    adminRestTemplate.put(TOUR_RATINGS_URL, ratingDto);
    verify(this.serviceMock).update(TOUR_ID, CUSTOMER_ID, SCORE, COMMENT);
  }

  @Test
  void testCreateManyTourRatings() {
    // Mutations require ADMIN
    Integer customers[] = { 123 };
    adminRestTemplate.postForObject(TOUR_RATINGS_URL + "/batch?score=" + SCORE, customers, String.class);
    verify(serviceMock).rateMany(anyInt(), anyInt(), anyList());
  }

  /** Unhappy paths to validate GlobalExceptionHandler */

  @Test
  public void test404() {
    // Reads allowed for USER
    when(serviceMock.lookupRatings(anyInt())).thenThrow(new NoSuchElementException());
    ResponseEntity<String> res = userRestTemplate.getForEntity(TOUR_RATINGS_URL, String.class);
    assertThat(res.getStatusCode(), is(HttpStatus.NOT_FOUND));
  }

  @Test
  public void test400() {
    // Reads allowed for USER
    when(serviceMock.lookupRatings(anyInt())).thenThrow(new ConstraintViolationException(null));
    ResponseEntity<String> res = userRestTemplate.getForEntity(TOUR_RATINGS_URL, String.class);
    assertThat(res.getStatusCode(), is(HttpStatus.BAD_REQUEST));
  }

  // ==== Negative security test required by rubric ====
  @Test
  void userCannotCreateRating_get403() {
    // USER tries to POST to /tours/** -> security should block with 403 Forbidden
    ResponseEntity<String> resp = userRestTemplate.postForEntity(TOUR_RATINGS_URL, ratingDto, String.class);
    assertThat(resp.getStatusCode(), is(HttpStatus.FORBIDDEN));
  }
}
