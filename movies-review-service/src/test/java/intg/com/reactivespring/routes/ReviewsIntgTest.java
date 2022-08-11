package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactorRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.test.StepVerifier;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class ReviewsIntgTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewReactorRepository reviewReactorRepository;

    static String MOVIE_REVIEW_URL = "/v1/reviews";

    @BeforeEach
    void setUp() {
        var reviewsList = List.of(
                new Review("1", 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));
        reviewReactorRepository.saveAll(reviewsList).blockLast();
    }

    @AfterEach
    void tearDown() {
        reviewReactorRepository.deleteAll().block();
    }

    @Test
    void addReview() {
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        webTestClient
                .post()
                .uri(MOVIE_REVIEW_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    var saveMovieReview = reviewEntityExchangeResult.getResponseBody();
                    Assertions.assertNotNull(saveMovieReview);
                    Assertions.assertNotNull(saveMovieReview.getReviewId());
                });
    }

    @Test
    void getAllReviews() {
        webTestClient.get()
                .uri(MOVIE_REVIEW_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(3);
    }

    @Test
    void updateReview() {
        webTestClient.put()
                .uri(MOVIE_REVIEW_URL + "/{id}", "1")
                .bodyValue(new Review("1", 1L, "Awesome Movie Updated", 9.0))
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    var updatedMovie = reviewEntityExchangeResult.getResponseBody();
                    Assertions.assertEquals("1", updatedMovie.getReviewId());
                    Assertions.assertEquals("Awesome Movie Updated", updatedMovie.getComment());
                });
    }

    @Test
    void deleteReview() {
        webTestClient.delete()
                .uri(MOVIE_REVIEW_URL + "/{id}", "1")
                .exchange()
                .expectStatus()
                .isNoContent();

        webTestClient.get()
                .uri(MOVIE_REVIEW_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(2);

    }

    @Test
    void getAllReviewsByMovieId() {
        var url = UriComponentsBuilder.fromUriString(MOVIE_REVIEW_URL)
                .queryParam("movieInfoId", "1")
                .buildAndExpand()
                .toUri();

        webTestClient.get()
                .uri(url)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(2);
    }

}
