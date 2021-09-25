package com.kuke.reactive.async;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class FutureEx {
    // 비동기 결과를 가져오는 방법은
    // Future 또는 Callback 을 사용

    // Future : 비동기적인 작업을 수행한 결과를 가져올 방법을 나타내는 것

    // newCachedThreadPool : max 제한이 없고 스레드가 만들어져있지 않다가,
    // 요청이 들어오면 만들거나, 만들어져있는 스레드를 사용

    // Callable : Runnable 과 다르게 return, throw exception
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService es = Executors.newCachedThreadPool();

        Future<String> f = es.submit(() -> {
            Thread.sleep(2000);
            log.info("Async");
            return "Hello";
        });

        System.out.println(f.isDone()); // 즉시 리턴. non-blocking
        Thread.sleep(2100);
        log.info("Exit");
        System.out.println(f.isDone());
        System.out.println(f.get()); // blocking, 만약 결과가 끝나지 않았을 때 null같은게 오면 non-blocking
    }
}
