package com.reactivespring.moviesinfoservice.controller;

import com.reactivespring.moviesinfoservice.domain.MovieInfo;
import com.reactivespring.moviesinfoservice.service.MovieInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1")
public class MovieInfoController {

    private MovieInfoService movieInfoService;

    Sinks.Many<MovieInfo> movieInfoSink = Sinks.many().replay().latest();

    public MovieInfoController(MovieInfoService movieInfoService) {
        this.movieInfoService = movieInfoService;
    }

    @PostMapping("/movieinfo")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo) {
        return movieInfoService.addMovieInfo(movieInfo)
                        .doOnNext(savedInfo-> movieInfoSink.tryEmitNext(savedInfo));

        //publish that movie to something
        //subscriber to this movie info

    }

    @GetMapping(value = "/movieinfo/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<MovieInfo> addMovieInfoById() {
        return movieInfoSink.asFlux().log();
    }

    @GetMapping("/movieinfo")
    public Flux<MovieInfo> getAllMovies(@RequestParam(value = "year", required = false) Integer year,
                                        @RequestParam(value = "name", required = false) String name) {

        if (year != null) {
            return movieInfoService.getMovieInfoByYear(year).log();
        }
        if (name != null) {
            return movieInfoService.getMovieByName(name)
                    .flatMapMany((t) -> Flux.just(t));
        }
        return movieInfoService.getAllMovieInfos().log();

    }

    @GetMapping("/movieinfo/{id}")
    public Mono<ResponseEntity<MovieInfo>> getMovieInfoById(@PathVariable String id) {
        return movieInfoService.getMovieInfoById(id)
                .map(movieInfo -> ResponseEntity.ok().body(movieInfo))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build())).log();
    }

    @PutMapping("/movieinfo/{id}")
    public Mono<ResponseEntity<MovieInfo>> updateMovieInfo(@RequestBody MovieInfo updatedMovieInfo,
                                                           @PathVariable String id) {
        return movieInfoService.updateMovieInfo(updatedMovieInfo, id)
                .map(movieInfo -> ResponseEntity.ok().body(movieInfo))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build())).log();
    }

    @DeleteMapping("movieinfo/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMovieInfo(@PathVariable String id) {
        return movieInfoService.deleteMovieInfo(id).log();
    }

}
