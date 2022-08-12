package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repository.ReviewReactorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ReviewHandler {

    @Autowired
    private Validator validator;

    private ReviewReactorRepository reviewReactorRepository;

    Sinks.Many<Review> reviewSink = Sinks.many().replay().all();


    public ReviewHandler(ReviewReactorRepository reviewReactorRepository) {
        this.reviewReactorRepository = reviewReactorRepository;
    }


    public Mono<ServerResponse> addReview(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .flatMap(reviewReactorRepository::save)
                .doOnNext(review -> reviewSink.tryEmitNext(review))
                .flatMap(savedReview -> ServerResponse.status(HttpStatus.CREATED).bodyValue(savedReview));
    }

    private void validate(Review review) {
        var constraintViolations = validator.validate(review);
        log.info("constraintViolations: {}", constraintViolations);
        if (constraintViolations.size() > 0) {
            var errorMessage = constraintViolations.stream().map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(","));
            throw new ReviewDataException(errorMessage);

        }
    }

    public Mono<ServerResponse> getReviews(ServerRequest serverRequest) {
        var movieInfoId = serverRequest.queryParam("movieInfoId");

        if (movieInfoId.isPresent()) {
            var reviewFlux = reviewReactorRepository.findReviewByMovieInfoId(Long.valueOf(movieInfoId.get()));
            return buildReviewsResponse(reviewFlux);
        } else {
            var reviewsFlux = reviewReactorRepository.findAll();
            return buildReviewsResponse(reviewsFlux);
        }

    }

    private static Mono<ServerResponse> buildReviewsResponse(Flux<Review> reviewFlux) {
        return ServerResponse.ok().body(reviewFlux, Review.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest serverRequest) {
        var id = serverRequest.pathVariable("id");


        var existingReview = reviewReactorRepository.findById(id);
//                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found for the given Review id "+ id)));


        return existingReview
                .flatMap(review -> serverRequest.bodyToMono(Review.class)
                        .map(
                                reqReview -> {
                                    review.setComment(reqReview.getComment());
                                    review.setRating(reqReview.getRating());
                                    return review;
                                }
                        )).flatMap(reviewReactorRepository::save)
                .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deleteReview(ServerRequest serverRequest) {
        var id = serverRequest.pathVariable("id");

        var existingReview = reviewReactorRepository.findById(id);

        return existingReview
                .flatMap(review -> reviewReactorRepository.deleteById(id))
                .then(ServerResponse.noContent().build());

    }

    public Mono<ServerResponse> getReviewsStream(ServerRequest serverRequest) {

        return ServerResponse.ok().contentType(MediaType.APPLICATION_NDJSON)
                .body(reviewSink.asFlux(), Review.class).log();

    }
}
