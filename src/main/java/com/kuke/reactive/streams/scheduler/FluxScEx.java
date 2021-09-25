package com.kuke.reactive.streams.scheduler;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class FluxScEx {

    public static void main(String[] args) throws InterruptedException {
//        Flux.range(1, 10)
////                .publishOn(Schedulers.newSingle("pub"))
//                .log()
//                .subscribeOn(Schedulers.newSingle("sub"))
////                .log()
//                .subscribe(System.out::println);
//        System.out.println("exit");

        // interval은 user thread가 아니라 daemon thread가 만들어짐
        // user thread가 하나도 남아있지 않고 daemon thread만 남아있으면 그냥 강제종료 해버림
        Flux.interval(Duration.ofMillis(200)) // 어떤 주기를 가지고 무한대로 쏴주는 것
                .take(10) // 10개만 받고 종료해버림
                .subscribe(s -> log.debug("onNext : {}", s));

        log.debug("exit");
        TimeUnit.SECONDS.sleep(10);

//         user thread는 main이 종료되어도 작업 마치기 전에는 종료되지 않음
//        Executors.newSingleThreadExecutor()
//                .execute(() -> {
//                    try {
//                        TimeUnit.SECONDS.sleep(2);
//                    } catch (InterruptedException e) { }
//                    System.out.println("Hello");
//                });
//
//        System.out.println("exit");
    }
}
