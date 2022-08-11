package com.reactivespring.router;

import com.reactivespring.handler.ReviewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
@Configuration
public class ReviewRouter {

    @Bean
    public RouterFunction<ServerResponse> reviewsRoute(ReviewHandler reviewHandler){
        return route()
                .path("/v1/reviews",builder -> builder

                                .POST( reviewHandler::addReview)
                                .GET( reviewHandler::getReviews)
                        .PUT("{id}", reviewHandler::updateReview)
                        .DELETE("{id}", reviewHandler::deleteReview)
                    )
                .GET("/v1/helloworld", request -> ServerResponse.ok().bodyValue("Hello World"))
                .build() ;
    }
}
