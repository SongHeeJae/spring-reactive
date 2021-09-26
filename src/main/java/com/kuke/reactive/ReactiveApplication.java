package com.kuke.reactive;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

@Slf4j
@SpringBootApplication
@EnableAsync
public class ReactiveApplication {

	@RestController
	public static class MyController {

		// 굳이 작업을 서블릿 스레드가 계속 물고 있을 필요가 없음

		@GetMapping("/callable")
		public Callable<String> callable() throws InterruptedException {
			log.info("callable");
			return () -> {
				// 서블릿 스레드는 즉시 리턴하고, 작업 스레드가 처리
				log.info("async");
				Thread.sleep(2000);
				return "hello";
			};
		}
//		public String callable() throws InterruptedException {
//			log.info("async");
//			Thread.sleep(2000);
//			return "hello";
//		}
	}

	@Bean
	ThreadPoolTaskExecutor tp() {
		ThreadPoolTaskExecutor e = new ThreadPoolTaskExecutor();
		e.setCorePoolSize(100);
		e.setQueueCapacity(50);
		e.setMaxPoolSize(150);
		e.initialize();
		return e;
	}

	public static void main(String[] args) {
		SpringApplication.run(ReactiveApplication.class, args);
	}

//	---

//	@Component
//	public static class MyService {
//
//		@Async
//		public ListenableFuture<String> hello() throws InterruptedException {
//			log.info("hello()");
////			Thread.sleep(2000);
//			return new AsyncResult<>("Hello");
//		}
//	}
//
//	public static void main(String[] args) {
//		try (ConfigurableApplicationContext c = SpringApplication.run(ReactiveApplication.class, args)) {
//
//		}
//	}
//
//	@Autowired MyService myService;
//
//	@Bean
//	ApplicationRunner run() {
//		// 모든 빈이 다 뜨면 실행
//		return args -> {
//			log.info("run()");
//			ListenableFuture<String> f = myService.hello();
//			f.addCallback(s -> System.out.println("s = " + s),
//					e -> System.out.println("e = " + e.getMessage()));
//			log.info("exit");
//		};
//	}

//	---

//	@RestController
//	public static class Controller {
//
//		@RequestMapping("/hello")
//		public Publisher<String> hello(String name) {
//			return new Publisher<String>() {
//				@Override
//				public void subscribe(Subscriber<? super String> s) {
//					s.onSubscribe(new Subscription() {
//						@Override
//						public void request(long n) {
//							log.info("request = {}", n);
//							s.onNext("Hello " + name);
//							s.onComplete();
//						}
//
//						@Override
//						public void cancel() {
//
//						}
//					});
//				}
//			};
//		}
//	}

}
