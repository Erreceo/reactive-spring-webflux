package com.reactivespring.moviesinfoservice.controller;

import com.reactivespring.moviesinfoservice.domain.MovieInfo;
import com.reactivespring.moviesinfoservice.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MovieInfoControllerIntgTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @Autowired
    WebTestClient webTestClient;

    static String MOVIES_INFO_URI = "/v1/movieinfo";

    @BeforeEach
    void setUp() {
        var movieInfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        movieInfoRepository.saveAll(movieInfos).blockLast();

    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void addMovieInfo() {

        var movieInfo = new MovieInfo(null, "Batman Begins1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

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
                });
    }

    @Test
    void getAllMovieInfos() {
        webTestClient.get()
                .uri(MOVIES_INFO_URI)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getAllMovieInfosByYear() {
        var year = 2005;
        var url = UriComponentsBuilder.fromUriString(MOVIES_INFO_URI)
                .queryParam("year", year)
                        .buildAndExpand()
                                .toUri();

        webTestClient.get()
                .uri(url)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }
    @Test
    void getAllMovieInfosByYear_handling_notfound() {
        var year = 2030;
        var url = UriComponentsBuilder.fromUriString(MOVIES_INFO_URI)
                .queryParam("year", year)
                .buildAndExpand()
                .toUri();

        webTestClient.get()
                .uri(url)
                .exchange()
                .expectStatus()
                .isNotFound();
    }
    @Test
    void getAllMovieInfosByName() {
        var name = "Dark Knight Rises";
        var url = UriComponentsBuilder.fromUriString(MOVIES_INFO_URI)
                .queryParam("name", name)
                .buildAndExpand()
                .toUri();

        webTestClient.get()
                .uri(url)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }


    @Test
    void getMovieInfoById() {

        var MOVIE_ID = "abc";

        webTestClient.get()
                .uri(MOVIES_INFO_URI+"/{id}",MOVIE_ID)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Dark Knight Rises");

//                .expectBody(MovieInfo.class)
//                .consumeWith(listEntityExchangeResult -> {
//                    var movieInfo = listEntityExchangeResult.getResponseBody();
//                    assertEquals(MOVIE_ID,movieInfo.getMovieInfoId());
//                });
    }

    @Test
    void getMovieInfoById_handling_notfound() {

        var MOVIE_ID = "def";

        webTestClient.get()
                .uri(MOVIES_INFO_URI+"/{id}",MOVIE_ID)
                .exchange()
                .expectStatus()
                .isNotFound();


    }

    @Test
    void updateMovieInfoById() {

        var MOVIE_ID = "abc";
        var movieInfo = new MovieInfo("abc", "Dark Knight Rises 1",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

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
    void updateMovieInfoById_handling_notfound() {

        var MOVIE_ID = "def";
        var movieInfo = new MovieInfo("abc", "Dark Knight Rises 1",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        webTestClient.put()
                .uri(MOVIES_INFO_URI+"/{id}",MOVIE_ID)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void deleteMovieInfo(){
        var MOVIE_ID = "abc";


        webTestClient.delete()
                .uri(MOVIES_INFO_URI+"/{id}",MOVIE_ID)
                .exchange()
                .expectStatus()
                .isNoContent();

        webTestClient.get()
                .uri(MOVIES_INFO_URI)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(2);
    }

}