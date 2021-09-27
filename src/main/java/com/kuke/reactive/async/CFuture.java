package com.kuke.reactive.async;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class CFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        ExecutorService es = Executors.newFixedThreadPool(10);

        CompletableFuture.supplyAsync(() -> {
            log.info("runAsync");
//            if(1==1) throw new RuntimeException();
            return 1;
        }).thenCompose(s -> { // ex. flatMap
            log.info("thenApply {}", s);
            return CompletableFuture.completedFuture(s + 1);
        }).thenApplyAsync(s -> { // ex. map
            log.info("thenApply {}", s);
            return s * 3;
        }, es).exceptionally(
                e -> -10
        ).thenAcceptAsync(s -> {
            log.info("thenAccept {}", s);
        }, es);
        log.info("exit");

        ForkJoinPool.commonPool().shutdown();
        ForkJoinPool.commonPool().awaitTermination(10, TimeUnit.SECONDS);
    }
}
