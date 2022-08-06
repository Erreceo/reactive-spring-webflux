package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ReviewHandler {


    private ReviewReactorRepository reviewReactorRepository;


    public ReviewHandler(ReviewReactorRepository reviewReactorRepository){
        this.reviewReactorRepository = reviewReactorRepository;
    }


    public Mono<ServerResponse> addReview(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(Review.class)
                .flatMap(reviewReactorRepository::save)
                .flatMap(savedReview -> ServerResponse.status(HttpStatus.CREATED).bodyValue(savedReview));
    }

    public Mono<ServerResponse> getReviews(ServerRequest serverRequest) {
        var reviewsFlux = reviewReactorRepository.findAll();
        return ServerResponse.ok().body(reviewsFlux, Review.class);
    }
}
