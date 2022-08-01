package com.learnreactiveprogramming.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class FluxAndMonoGeneratorService {

    public Flux<String> namesFlux() {
        return Flux.fromIterable(List.of("Alex", "Ben", "Chloe"))
                .log(); //db or a remote service call
    }

    public Mono<String> nameMono() {
        return Mono.just("alex")
                .log();
    }

    public Flux<String> namesFlux_map() {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .map(String::toUpperCase)
                .log(); //db or a remote service call
    }

    public Flux<String> namesFlux_immutability() {
        var namesFlux = Flux.fromIterable(List.of("alex", "ben", "chloe"));

        namesFlux.map(String::toUpperCase);

        return namesFlux;

    }

    public Flux<String> namesFlux_filter(int stringLength) {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .filter(s-> s.length()>stringLength)
                .map(s-> s.length() + "-" + s);
    }

    public Flux<String> namesFlux_flatmap(int stringLength) {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .filter(s-> s.length()>stringLength)
                .flatMap(s -> splitString(s))
                .log();
    }
    public Flux<String> namesFlux_flatmap_async(int stringLength) {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .filter(s-> s.length()>stringLength)
                .flatMap(s -> splitStringDelay(s))
                .log();
    }
    public Flux<String> namesFlux_concatmap(int stringLength) {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .filter(s-> s.length()>stringLength)
                .concatMap(s -> splitStringDelay(s))
                .log();
    }

    public Mono<String> namesMono_map_filter(int stringLength){
        return Mono.just("alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength);
    }

    public Mono<List<String>> namesMono_flatmap(int stringLength){
        return Mono.just("alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(this::splitStringMono);
    }
    public Flux<String> namesMono_flatmapMany(int stringLength){
        return Mono.just("alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMapMany(this::splitString);
    }

    public Flux<String> namesFlux_transform(int stringLength) {

        Function<Flux<String>,Flux<String>> filtermap = name -> name.map(String::toUpperCase)
                .filter(s->s.length()>stringLength);

        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .transform(filtermap)
                .flatMap(s -> splitString(s))
                .defaultIfEmpty("default")
                .log();
    }

    public Flux<String> namesFlux_transform_switchifEmpty(int stringLength) {

        Function<Flux<String>,Flux<String>> filtermap = name -> name.map(String::toUpperCase)
                .filter(s->s.length()>stringLength);

        var defaultFlux = Flux.just("default")
                .transform(filtermap);
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .transform(filtermap)
                .switchIfEmpty(defaultFlux)
                .flatMap(s -> splitString(s))
                .log();
    }

    public Flux<String> explore_concat(){
        var abc = Flux.just("A", "B", "C");
        var def = Flux.just("D","E","F");

        return Flux.concat(abc, def);
    }
    public Flux<String> explore_merge(){
        var abc = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));
        var def = Flux.just("D","E","F")
                .delayElements(Duration.ofMillis(125));

        return Flux.merge(abc, def).log();
    }
    public Flux<String> explore_mergeWith(){
        var abc = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));
        var def = Flux.just("D","E","F")
                .delayElements(Duration.ofMillis(125));

        return abc.mergeWith(def).log();
    }
    public Flux<String> explore_mergeWith_mono(){
        var a = Mono.just("A");
        var b = Mono.just("B");

        return a.mergeWith(b).log();
    }


    public Flux<String> explore_concatWith(){
        var abc = Flux.just("A", "B", "C");
        var def = Flux.just("D","E","F");

        return abc.concatWith(def);
    }

    public Flux<String> explore_concatWith_mono(){
        var a = Mono.just("A");
        var b = Mono.just("B");
        var c = Mono.just("C");
        var def = Flux.just("D","E","F");

        return a.concatWith(b).concatWith(c).concatWith(def);
    }


    public Flux<String> explore_mergeSequencial(){
        var abc = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));
        var def = Flux.just("D","E","F")
                .delayElements(Duration.ofMillis(125));

        return Flux.mergeSequential(abc,def).log();
    }

    public Flux<String> explore_zip(){
        var abc = Flux.just("A", "B", "C", "1");
        var def = Flux.just("D","E","F");

        return Flux.zip(abc, def, (f, s)-> f+s).log();
    }
    public Flux<String> explore_zip_1(){
        var abc = Flux.just("A", "B", "C", "1");
        var def = Flux.just("D","E","F");
        var _123 = Flux.just("1","2","3");
        var _456 = Flux.just("4","5","6");

        return Flux.zip(abc, def,_123, _456 )
                .map(t4 -> t4.getT1() + t4.getT2()+ t4.getT3()+ t4.getT4()).log();
    }
    public Flux<String> explore_zipWith(){
        var abc = Flux.just("A", "B", "C", "1");
        var def = Flux.just("D","E","F");

        return abc.zipWith(def, (f, s)-> f+s).log();
    }

    public Mono<String> explore_zipWith_mono(){
        var a = Mono.just("A");
        var b = Mono.just("B");

        return a.zipWith(b)
                .map(t2 -> t2.getT1() + t2.getT2()).log();
    }

    private Mono<List<String>> splitStringMono(String s){
        var charArray = s.split("");
        var charList = List.of(charArray);
        return Mono.just(charList);
    }

    public Flux<String> splitString(String name){
        var charArray = name.split("");

        return Flux.fromArray(charArray);
    }

    public Flux<String> splitStringDelay(String name){
        var charArray = name.split("");
        var delay = new Random().nextInt(1000);

        return Flux.fromArray(charArray).delayElements(Duration.ofMillis(delay));
    }

    public static void main(String[] args) {
        FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();
        fluxAndMonoGeneratorService.namesFlux()
                .subscribe(name -> System.out.println("Name is: " + name));

        fluxAndMonoGeneratorService.nameMono()
                .subscribe(name -> System.out.printf("Mono name is: " + name));
    }



}
