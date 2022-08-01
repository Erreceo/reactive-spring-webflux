package com.learnreactiveprogramming.service;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;

class FluxAndMonoGeneratorServiceTest {

    FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

    @Test
    void namesFlux() {
        var namesFlux = fluxAndMonoGeneratorService.namesFlux();

        StepVerifier.create(namesFlux)
                //.expectNext("Alex", "Ben", "Chloe")
                .expectNext("Alex")
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void namesFlux_map() {
        var namesFlux = fluxAndMonoGeneratorService.namesFlux_map();

        StepVerifier.create(namesFlux)
                .expectNext("ALEX", "BEN", "CHLOE")
                .verifyComplete();

    }

    @Test
    void namesFlux_immutability() {
        var namesFlux = fluxAndMonoGeneratorService.namesFlux_immutability();

        StepVerifier.create(namesFlux)
                .expectNext("alex", "ben", "chloe")
                .verifyComplete();
    }

    @Test
    void namesFlux_filter() {
        var namesFlux = fluxAndMonoGeneratorService.namesFlux_filter(3);
        StepVerifier.create(namesFlux)
                .expectNext("4-alex", "5-chloe")
                .verifyComplete();
    }

    @Test
    void namesFlux_flatmap() {
        var namesFlux = fluxAndMonoGeneratorService.namesFlux_flatmap(3);
        StepVerifier.create(namesFlux)
                .expectNext("a", "l", "e", "x", "c", "h", "l", "o", "e")
                .verifyComplete();

    }

    @Test
    void namesFlux_flatmap_async() {
        var namesFlux = fluxAndMonoGeneratorService.namesFlux_flatmap_async(3);
        StepVerifier.create(namesFlux)
                //expectNext("a", "l", "e", "x", "c", "h", "l", "o", "e")
                .expectNextCount(9)
                .verifyComplete();
    }

    @Test
    void namesFlux_concatmap() {
        var namesFlux = fluxAndMonoGeneratorService.namesFlux_concatmap(3);
        StepVerifier.create(namesFlux)
                .expectNext("a", "l", "e", "x", "c", "h", "l", "o", "e")
                //.expectNextCount(9)
                .verifyComplete();
    }

    @Test
    void namesMono_flatmap() {

        var namesFlux = fluxAndMonoGeneratorService.namesMono_flatmap(3);

        StepVerifier.create(namesFlux)
                .expectNext(List.of("A", "L", "E", "X"))
                .verifyComplete();

    }

    @Test
    void namesMono_flatmapMany() {
        var namesFlux = fluxAndMonoGeneratorService.namesMono_flatmapMany(3);

        StepVerifier.create(namesFlux)
                .expectNext("A", "L", "E", "X")
                .verifyComplete();
    }

    @Test
    void namesFlux_transform() {
        var namesFlux = fluxAndMonoGeneratorService.namesFlux_transform(3);
        StepVerifier.create(namesFlux)
                .expectNext("A", "L", "E", "X", "C", "H", "L", "O", "E")
                .verifyComplete();
    }
    @Test
    void namesFlux_transform_1() {
        var namesFlux = fluxAndMonoGeneratorService.namesFlux_transform(6);
        StepVerifier.create(namesFlux)
                //.expectNext("A", "L", "E", "X", "C", "H", "L", "O", "E")
                .expectNext("default")
                .verifyComplete();
    }
    @Test
    void namesFlux_transform_switchifEmpty() {
        var namesFlux = fluxAndMonoGeneratorService.namesFlux_transform_switchifEmpty(6);
        StepVerifier.create(namesFlux)
                //.expectNext("A", "L", "E", "X", "C", "H", "L", "O", "E")
                .expectNext("D", "E", "F", "A","U", "L", "T")
                .verifyComplete();
    }

    @Test
    void explore_concat() {
        var concatFlux = fluxAndMonoGeneratorService.explore_concat();

        StepVerifier.create(concatFlux)
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();
    }

    @Test
    void explore_concatWith() {
        var concatFlux = fluxAndMonoGeneratorService.explore_concatWith();

        StepVerifier.create(concatFlux)
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();
    }

    @Test
    void explore_concatWithMono() {
        var concatFlux = fluxAndMonoGeneratorService.explore_concatWith_mono();

        StepVerifier.create(concatFlux)
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();
    }
    @Test
    void explore_Merge() {
        var merge = fluxAndMonoGeneratorService.explore_merge();

        StepVerifier.create(merge)
                .expectNext("A", "D", "B", "E", "C", "F")
                .verifyComplete();
    }
    @Test
    void explore_MergeWith() {
        var merge = fluxAndMonoGeneratorService.explore_mergeWith();

        StepVerifier.create(merge)
                .expectNext("A", "D", "B", "E", "C", "F")
                .verifyComplete();
    }
    @Test
    void explore_MergeWithMono() {
        var merge = fluxAndMonoGeneratorService.explore_mergeWith_mono();

        StepVerifier.create(merge)
                .expectNext("A", "B")
                .verifyComplete();
    }
    @Test
    void explore_MergeSequencial() {
        var merge = fluxAndMonoGeneratorService.explore_mergeSequencial();

        StepVerifier.create(merge)
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();
    }
    @Test
    void explore_zip(){
        var merge = fluxAndMonoGeneratorService.explore_zip();

        StepVerifier.create(merge)
                .expectNext("AD", "BE", "CF")
                .verifyComplete();
    }
    @Test
    void explore_zip_1(){
        var merge = fluxAndMonoGeneratorService.explore_zip_1();

        StepVerifier.create(merge)
                .expectNext("AD14", "BE25", "CF36")
                .verifyComplete();
    }
    @Test
    void explore_zipWith(){
        var merge = fluxAndMonoGeneratorService.explore_zipWith();

        StepVerifier.create(merge)
                .expectNext("AD", "BE", "CF")
                .verifyComplete();
    }
    @Test
    void explore_zipWith_Mono(){
        var merge = fluxAndMonoGeneratorService.explore_zipWith_mono();

        StepVerifier.create(merge)
                .expectNext("AB")
                .verifyComplete();
    }
}