package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.execptionhandler.GlobalErrorHandler;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactorRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Array;
import java.util.List;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;


@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalErrorHandler.class})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {

    @MockBean
    private ReviewReactorRepository reviewReactorRepository;

    @Autowired
    private WebTestClient webTestClient;

    static String REVIEWS_URL = "/v1/reviews";

    @Test
    void AddReview() {
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        when(reviewReactorRepository.save(isA(Review.class))).thenReturn(Mono.just(new Review("1", 1L, "Awesome Movie", 9.0)));

        webTestClient.post().uri(REVIEWS_URL).bodyValue(review).exchange().expectStatus().isCreated().expectBody(Review.class).consumeWith(reviewEntityExchangeResult -> {
            var savedReview = reviewEntityExchangeResult.getResponseBody();
            assert savedReview != null;
            assert savedReview.getReviewId() != null;
        });
    }

    @Test
    void AddReviewValidarion() {
        var review = new Review(null, null, "Awesome Movie", -9.0);

        when(reviewReactorRepository.save(isA(Review.class))).thenReturn(Mono.just(new Review("1", 1L, "Awesome Movie", 9.0)));

        webTestClient.post().uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .isEqualTo("rating.movieInfoId: must not be null,rating.negative : please pass a non-negative value");
    }

    @Test
    void getAllReviews() {
        var reviews = List.of(new Review("abc", 1L, "Awesome Movie", 9.0),
                new Review("1", 2L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie", 9.0));

        when(reviewReactorRepository.findAll()).thenReturn(Flux.fromIterable(reviews));

        webTestClient
                .get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(3);

    }

    @Test
    void updateReview(){
        var review = new Review("abc", 1L, "Awesome Movie", 9.5);
        var reviewUpdated = new Review("abc", 1L, "Awesome Movie Updated", 8.5);

        when(reviewReactorRepository.findById(isA(String.class))).thenReturn(Mono.just(review));
        when(reviewReactorRepository.save(isA(Review.class))).thenReturn(Mono.just(reviewUpdated));


        webTestClient.put()
                .uri(REVIEWS_URL+"/{id}", "abc")
                .bodyValue(reviewUpdated)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    var reviewNew = reviewEntityExchangeResult.getResponseBody();
                    assert "abc".equals(reviewNew.getReviewId());
                    assert "Awesome Movie Updated".equals(reviewNew.getComment());
                });

    }

    @Test
    void deleteReview() {
        var review = new Review("abc", 1L, "Awesome Movie", 9.5);

        when(reviewReactorRepository.findById(isA(String.class))).thenReturn(Mono.just(review));
        when(reviewReactorRepository.deleteById(isA(String.class))).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(REVIEWS_URL+"/{id}", "abc")
                .exchange()
                .expectStatus()
                .isNoContent();

        when(reviewReactorRepository.findAll()).thenReturn(Flux.empty());

        webTestClient.get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectBodyList(Review.class)
                .hasSize(0);

    }

    @Test
    void getReviewByMovieId(){
        var review = List.of(new Review("abc", 1L, "Awesome Movie", 9.5),

        new Review("abc", 1L, "Awesome Movie", 9.5));

        when(reviewReactorRepository.findReviewByMovieInfoId(isA(Long.class))).thenReturn(Flux.fromIterable(review));

        var url = UriComponentsBuilder.fromUriString(REVIEWS_URL)
                .queryParam("movieInfoId", 1L)
                .buildAndExpand()
                .toUriString();

        webTestClient.get()
                .uri(url)
                .exchange()
                .expectBodyList(Review.class)
                .hasSize(2);

    }

}
