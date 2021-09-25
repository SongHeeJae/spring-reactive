package com.kuke.reactive.streams.scheduler;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class FluxScEx {

    public static void main(String[] args) {
        Flux.range(1, 10)
//                .publishOn(Schedulers.newSingle("pub"))
                .log()
                .subscribeOn(Schedulers.newSingle("sub"))
//                .log()
                .subscribe(System.out::println);
        System.out.println("exit");
    }
}
