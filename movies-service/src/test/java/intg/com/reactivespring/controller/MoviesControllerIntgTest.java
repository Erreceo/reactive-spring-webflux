package com.reactivespring.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port=8084)
@TestPropertySource(
        properties = {
                "restClient.moviesInfoUrl=http://localhost:8084/v1/movieinfo",
                "restClient.reviewsUrl=http://localhost:8084/v1/reviews"
        }
)
public class MoviesControllerIntgTest {

        @Autowired
        WebTestClient webTestClient;

        @Test
        void retriveMovieById(){

                var movieId = "abc";

                stubFor(get(urlEqualTo("/v1/movieinfo"+"/"+movieId))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("movieinfo.json")));

                stubFor(get(urlPathEqualTo("/v1/reviews"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("reviews.json")));

                webTestClient.get()
                        .uri("/v1/movies/{id}", movieId)
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(Movie.class)
                        .consumeWith(movieEntityExchangeResult -> {
                                var movie = movieEntityExchangeResult.getResponseBody();
                                assert Objects.requireNonNull(movie).getReviewList().size() == 2;
                                assertEquals("Batman Begins", movie.getMovieInfo().getName());
                        });
        }

    @Test
    void retriveMovieById_404() {
        var movieId = "abc";

        stubFor(get(urlEqualTo("/v1/movieinfo" + "/" + movieId))
                .willReturn(aResponse().withStatus(404)));
        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));
        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody(String.class)
                .consumeWith(s -> assertEquals("There is no MovieInfo Available for the passed in Id: abc", s.getResponseBody()));

        WireMock.verify(1, getRequestedFor(urlEqualTo("/v1/movieinfo"+"/"+movieId)));

    }
    @Test
    void retriveMovieById_reviews_404() {
        var movieId = "abc";

        stubFor(get(urlEqualTo("/v1/movieinfo"+"/"+movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withStatus(404)));

        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    var movie = movieEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(movie).getReviewList().size() == 0;
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                });

    }
    @Test
    void retriveMovieById_5xx() {
        var movieId = "abc";

        stubFor(get(urlEqualTo("/v1/movieinfo" + "/" + movieId))
                .willReturn(aResponse().withStatus(500)
                        .withBody("Movie Info Unavailable")));

        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .consumeWith(s -> assertEquals("Server Execption in MoviesInfoService Movie Info Unavailable", s.getResponseBody()));

        WireMock.verify(4, getRequestedFor(urlEqualTo("/v1/movieinfo"+ "/" + movieId)));

    }

    @Test
    void retriveMovieById_Review_5xx() {
        var movieId = "abc";

        stubFor(get(urlEqualTo("/v1/movieinfo"+"/"+movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Review Service Not Available")));

        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .consumeWith(s -> assertEquals("Server Execption in ReviewService Review Service Not Available", s.getResponseBody()));

        WireMock.verify(4, getRequestedFor(urlPathMatching("/v1/reviews")));

    }


}
