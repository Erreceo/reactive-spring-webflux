package com.reactivespring.moviesinfoservice.repository;

import com.reactivespring.moviesinfoservice.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@DataMongoTest
@ActiveProfiles("test")
public class MovieInfoRepositoryIntgTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

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
    void findAll(){
        var moviesInfoFlux = movieInfoRepository.findAll().log();

        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(3)
                .verifyComplete();

    }

    @Test
    void findById(){
        var movieInfoMono = movieInfoRepository.findById("abc").log();

        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo ->
                    assertEquals("Dark Knight Rises", movieInfo.getName())
                ).verifyComplete();
    }
    @Test
    void saveMovieInfo(){
        var movieInfo = new MovieInfo(null, "Batman Begins1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        var movieInfoMono = movieInfoRepository.save(movieInfo).log();

        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo1 -> {
                    assertNotNull( movieInfo1.getMovieInfoId());
                    assertEquals( "Batman Begins1",movieInfo1.getName());
                }).verifyComplete();
    }

    @Test
    void updateMovieInfo(){
        var movieInfoMono = movieInfoRepository.findById("abc").block();
        if(movieInfoMono != null) {
            movieInfoMono.setYear(2021);
        var updatedMovieInfo = movieInfoRepository.save(movieInfoMono).log();
            StepVerifier.create(updatedMovieInfo)
                    .assertNext(movieInfo1 -> assertEquals( 2021,movieInfo1.getYear()) )
                    .verifyComplete();
        }
    }
    @Test
    void deleteMovieInfo(){
         movieInfoRepository.deleteById("abc").block();
        var movieInfoMono = movieInfoRepository.findAll();

            StepVerifier.create(movieInfoMono)
                    .expectNextCount(2)
                    .verifyComplete();



    }

    @Test
    void findByYear(){
        var moviesInfoFlux = movieInfoRepository.findByYear(2005).log();

        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(1)
                .verifyComplete();

    }


    @Test
    void findByName(){
        var moviesInfoFlux = movieInfoRepository.findByName("The Dark Knight").log();

        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(1)
                .verifyComplete();

    }

}