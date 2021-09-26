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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Future;

@Slf4j
@SpringBootApplication
@EnableAsync
public class ReactiveApplication {

	@Component
	public static class MyService {

		@Async(value = "tp") // 비동기 작업. value에 스레드풀 빈이름 지정 가능
		public ListenableFuture<String> hello() throws InterruptedException {
			log.info("hello()");
//			Thread.sleep(2000);
			return new AsyncResult<>("Hello");
		}
	}

	@Bean // Async는 별도로 구현한 빈이 있으면 기본적으로 이걸 쓰게 됨
	ThreadPoolTaskExecutor tp() {
		ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
		te.setCorePoolSize(10); // 초기에 기본 스레드풀 개수
		te.setMaxPoolSize(100); // 큐가 꽉차면 그 시점에 맥스 풀사이즈까지 늘려주는 것
		te.setQueueCapacity(200); // 코어 풀 사이즈가 꽉차면 큐가 참
		te.setThreadNamePrefix("mythread");
		te.initialize();
		return te;
	}

	public static void main(String[] args) {
		try (ConfigurableApplicationContext c = SpringApplication.run(ReactiveApplication.class, args)) {

		}
	}

	@Autowired MyService myService;

	@Bean
	ApplicationRunner run() {
		// 모든 빈이 다 뜨면 실행
		return args -> {
			log.info("run()");
			ListenableFuture<String> f = myService.hello();
			f.addCallback(s -> System.out.println("s = " + s),
					e -> System.out.println("e = " + e.getMessage()));
			log.info("exit");
		};
	}

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
