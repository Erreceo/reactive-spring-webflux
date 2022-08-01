package com.reactivespring.moviesinfoservice.controller;

import com.reactivespring.moviesinfoservice.domain.MovieInfo;
import com.reactivespring.moviesinfoservice.service.MovieInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = MovieInfoController.class)
@AutoConfigureWebTestClient
public class MovieInfoControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MovieInfoService movieInfoServiceMock;

    static String MOVIES_INFO_URI = "/v1/movieinfo";

    @Test
    void getAllMoviesInfo(){
        var movieInfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        when(movieInfoServiceMock.getAllMovieInfos()).thenReturn(Flux.fromIterable(movieInfos));

        webTestClient.get()
                .uri(MOVIES_INFO_URI)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }
    @Test
    void getMovieInfoById(){

        var movieInfo = new MovieInfo("abc", "Dark Knight Rises",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        var MOVIE_ID = "abc";
        when(movieInfoServiceMock.getMovieInfoById(MOVIE_ID)).thenReturn(Mono.just(movieInfo));

        webTestClient.get()
                .uri(MOVIES_INFO_URI+"/{id}",MOVIE_ID)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Dark Knight Rises");
    }

    @Test
    void addMovieInfo() {

        var movieInfo = new MovieInfo("mockid", "Batman Begins1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        when(movieInfoServiceMock.addMovieInfo(isA(MovieInfo.class))).thenReturn(Mono.just(movieInfo));

        webTestClient.post()
                .uri(MOVIES_INFO_URI)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(savedMovieInfo.getMovieInfoId());
                    assertEquals("mockid", savedMovieInfo.getMovieInfoId());
                });
    }

    @Test
    void addMovieInfo_validation() {

        var movieInfo = new MovieInfo("mockid", "",
                -2005, List.of(""), LocalDate.parse("2005-06-15"));

        when(movieInfoServiceMock.addMovieInfo(isA(MovieInfo.class))).thenReturn(Mono.just(movieInfo));

        webTestClient.post()
                .uri(MOVIES_INFO_URI)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    var responseBody = stringEntityExchangeResult.getResponseBody();
                    System.out.printf("response Body " + responseBody);
                    var expectedErrorMessage = "movieInfo.cast must be present,movieInfo.name must be present,movieInfo.year must be a Positive value";
                    assertEquals(expectedErrorMessage, responseBody);
                    assertNotNull(responseBody);
                });
//                .isCreated()
//                .expectBody(MovieInfo.class)
//                .consumeWith(movieInfoEntityExchangeResult -> {
//                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
//                    assertNotNull(savedMovieInfo.getMovieInfoId());
//                    assertEquals("mockid", savedMovieInfo.getMovieInfoId());
//                });
    }


    @Test
    void updateMovieInfoById() {

        var MOVIE_ID = "abc";
        var movieInfo = new MovieInfo("abc", "Dark Knight Rises 1",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        when(movieInfoServiceMock.updateMovieInfo(isA(MovieInfo.class), isA(String.class))).thenReturn(Mono.just(new MovieInfo(MOVIE_ID, "Dark Knight Rises 1", 2012,List.of("Christian Bale", "Tom Hardy"),  LocalDate.parse("2012-07-20"))));

        webTestClient.put()
                .uri(MOVIES_INFO_URI+"/{id}",MOVIE_ID)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()

                .expectBody(MovieInfo.class)
                .consumeWith(listEntityExchangeResult -> {
                    var updatedMovieInfo = listEntityExchangeResult.getResponseBody();
                    assertNotNull(updatedMovieInfo);
                    assertEquals("Dark Knight Rises 1", updatedMovieInfo.getName());

                });
    }

    @Test
    void deleteMovieInfo(){
        var MOVIE_ID = "abc";

        var movieInfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18"))
                );

        when(movieInfoServiceMock.deleteMovieInfo(isA(String.class))).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(MOVIES_INFO_URI+"/{id}",MOVIE_ID)
                .exchange()
                .expectStatus()
                .isNoContent();

        when(movieInfoServiceMock.getAllMovieInfos()).thenReturn(Flux.fromIterable(movieInfos));

        webTestClient.get()
                .uri(MOVIES_INFO_URI)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(2);
    }

}
